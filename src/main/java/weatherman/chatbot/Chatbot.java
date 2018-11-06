package weatherman.chatbot;

import java.io.IOException;
import java.util.Scanner;

import org.apache.http.client.ClientProtocolException;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dao.TiendaDAO;
import com.google.gson.JsonArray;
import weatherman.weather.Weather;

public class Chatbot {

    JsonObject context;
    Weather weather;
    TiendaDAO tienda;

    public static void main(String[] args) {
        Chatbot c = new Chatbot();
        Scanner scanner = new Scanner(System.in);
        String userUtterance;

        do {
            System.out.print("User:");
            userUtterance = scanner.nextLine();

            JsonObject userInput = new JsonObject();
            userInput.add("userUtterance", new JsonPrimitive(userUtterance));
            JsonObject botOutput = c.process(userInput);
            String botUtterance = "";
            if (botOutput != null && botOutput.has("botUtterance")) {
                botUtterance = botOutput.get("botUtterance").getAsString();
            }
            System.out.println("Bot:" + botUtterance);

        } while (!userUtterance.equals("QUIT"));
        scanner.close();
    }

    public Chatbot() {
        context = new JsonObject();
        context.add("currentTask", new JsonPrimitive("none"));
        weather = new Weather();
    }

    public JsonObject process(JsonObject userInput) {

        //step1: process user input
        JsonObject userAction = processUserInput(userInput);

        //step2: update context
        updateContext(userAction);

        //step3: identify bot intent
        identifyBotIntent();

        //step4: structure output
        JsonObject out = getBotOutput();

        return out;
    }

    public String processFB(JsonObject userInput) {
        JsonObject out = process(userInput);
        return out.toString();
    }

    public JsonObject processUserInput(JsonObject userInput) {
        String userUtterance = null;
        JsonObject userAction = new JsonObject();

        //default case
        userAction.add("userIntent", new JsonPrimitive(""));

        if (userInput.has("userUtterance")) {
            userUtterance = userInput.get("userUtterance").getAsString();
            userUtterance = userUtterance.replaceAll("%2C", ",");
        }
        if (userUtterance.matches("(hola|holi|hello|hi|Hola|Hello)( como vas)?")) {
            userAction.add("userIntent", new JsonPrimitive("saludo"));
        } else if (userUtterance.matches("(Gracias|gracias|GRACIAS|thanks)|(thank you)")) {
            userAction.add("userIntent", new JsonPrimitive("agradecimiento"));
        } else {
            String currentTask = context.get("currentTask").getAsString();
            String botIntent = context.get("botIntent").getAsString();
            if (currentTask.equals("request")) {
                if (botIntent.equals("requestTipos")) {
                    //obtener info tipos
                    JsonObject obj = new JsonObject();
                    if (!obj.get("tipos").isJsonNull()) {
                        JsonArray buttons = new JsonArray();
                        userAction.add("userIntent", new JsonPrimitive("informacionTipos"));
                        //recorrer objeto tipos y agregar a botones
                        buttons.add(new JsonPrimitive("pizza"));
                        userAction.add("botones", buttons);
                    }
                } else if (botIntent.equals("requestIngredientes")) {
                    //obtener info ingredientes disponibles
                    JsonObject obj = weather.getCityCode(userUtterance);
                    if (!obj.get("ingredientes").isJsonNull()) {
                        JsonArray buttons = new JsonArray();
                        userAction.add("userIntent", new JsonPrimitive("informacionIngre"));
                        //recorrer objeto ingredientes y agregar a botones
                        buttons.add(new JsonPrimitive("peperoni"));
                        buttons.add(new JsonPrimitive("piña dulce"));
                        userAction.add("botones", buttons);
                    }
                } else if (botIntent.equals("requestTiendas")) {
                    //obtener info Tiendas disponibles
                    JsonObject obj = weather.getCityCode(userUtterance);
                    if (!obj.get("tiendas").isJsonNull()) {
                        JsonArray buttons = new JsonArray();
                        userAction.add("userIntent", new JsonPrimitive("informacionTiendas"));
                        //recorrer objeto tiendas y agregar a botones
                        buttons.add(new JsonPrimitive("Papa johns"));
                        buttons.add(new JsonPrimitive("Jenos Pizza"));
                        userAction.add("botones", buttons);
                    }
                }

            }
        }

        return userAction;
    }

    public void updateContext(JsonObject userAction) {

        //copy userIntent
        context.add("userIntent", userAction.get("userIntent"));

        //
        String userIntent = context.get("userIntent").getAsString();
        if (userIntent.equals("saludo")) {
            context.add("currentTask", new JsonPrimitive("saludoUsuario"));
        } else if (userIntent.equals("informacionTipos")) {
            context.add("currentTask", new JsonPrimitive("request"));
            context.add("tipos", userAction.get("botones"));
        } else if (userIntent.equals("informacionIngre")) {
            context.add("currentTask", new JsonPrimitive("request"));
            context.add("ingredientes", userAction.get("botones"));
        } else if (userIntent.equals("informacionTiendas")) {
            context.add("currentTask", new JsonPrimitive("request"));
            context.add("tiendas", userAction.get("botones"));
        } else if (userIntent.equals("agradecimiento")) {
            context.add("currentTask", new JsonPrimitive("agradecimientoUsuario"));
        }
    }

    public void identifyBotIntent() {
        String currentTask = context.get("currentTask").getAsString();
        if (currentTask.equals("saludoUsuario")) {
            context.add("botIntent", new JsonPrimitive("saludoUsuario"));
        } else if (currentTask.equals("agradecimientoUsuario")) {
            context.add("botIntent", new JsonPrimitive("agradecimientoUsuario"));
        } else if (currentTask.equals("request")) {
            if (context.get("tipos").getAsString().equals("unknown")) {
                context.add("botIntent", new JsonPrimitive("requestTipo"));
            } else if (context.get("ingredientes").getAsString().equals("unknown")) {
                context.add("botIntent", new JsonPrimitive("requestIngredientes"));
            } else if (context.get("tiendas").getAsString().equals("unknown")) {
                context.add("botIntent", new JsonPrimitive("requestTiendas"));
            }
        } else {
            context.add("botIntent", null);
        }
    }

    public JsonObject getBotOutput() {

        JsonObject out = new JsonObject();
        String botIntent = context.get("botIntent").getAsString();
        JsonArray buttons = new JsonArray();
        String botUtterance = "";
        if (botIntent.equals("saludoUsuario")) {
            botUtterance = "hola, que deseas hoy? ";
            buttons=(JsonArray) context.get("tipos");
        } else if (botIntent.equals("agradecimientoUsuario")) {
            botUtterance = "gracias por usar nuestro servicio, que tengas un buen dia!!";
        } else if (botIntent.equals("requestTipo")) {
            botUtterance = " Que deseas hoy? ";
            buttons=(JsonArray) context.get("tipos");
        } else if (botIntent.equals("requestIngredientes")) {
            botUtterance = " Selecciona los ingredientes para tu "+context.get("tipo").getAsString();
            String timeDescription = getTimeDescription(context.get("timeOfWeather").getAsString());
            String placeDescription = getPlaceDescription();
            String weatherReport = context.get("weatherReport").getAsString();
            botUtterance = "Selecciona los ingredientes para tu "+context.get("tipo").getAsString();

        }
        out.add("botIntent", context.get("botIntent"));
        out.add("botUtterance", new JsonPrimitive(botUtterance));
        out.add("buttons", buttons);
        context = new JsonObject();
        return out;
    }

    private String getPlaceDescription() {
        return context.get("placeName").getAsString();
    }

    private String getTimeDescription(String timeOfWeather) {
        if (timeOfWeather.equals("current")) {
            return "now";
        }
        return null;
    }
}

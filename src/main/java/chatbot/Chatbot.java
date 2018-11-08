package chatbot;

import Services.Service;
import java.io.IOException;
import java.util.Scanner;

import org.apache.http.client.ClientProtocolException;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonArray;

public class Chatbot {

    JsonObject context;
    Service service;

    public static void main(String[] args) throws IOException {
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
        service = new Service();
    }

    public JsonObject process(JsonObject userInput) throws IOException {
        System.out.println(userInput.toString());
        //step1: process user input
        JsonObject userAction = processUserInput(userInput);

        //step2: update context
        updateContext(userAction);
        System.out.println("context " + context.toString());
        //step3: identify bot intent
        identifyBotIntent();

        //step4: structure output
        JsonObject out = getBotOutput();
        System.out.println("out " + out.toString());
        return out;
    }

    public String processFB(JsonObject userInput) throws IOException {
        JsonObject out = process(userInput);
        return out.toString();
    }

    public JsonObject processUserInput(JsonObject userInput) throws IOException {
        String userUtterance = null;
        JsonObject userAction = new JsonObject();
        //default case
        userAction.add("userIntent", new JsonPrimitive(""));
        System.out.println("entrooo " + userInput);
        if (userInput.has("userUtterance")) {
            userUtterance = userInput.get("userUtterance").getAsString();
            userUtterance = userUtterance.replaceAll("%2C", ",");
        }
        
        if (userUtterance.matches("(hola|holi|hello|hi|Hola|Hello)( como vas)?")) {
            userAction.add("userIntent", new JsonPrimitive("saludo"));
        } else if (userUtterance.matches("(Gracias|gracias|GRACIAS|thanks)|(thank you)")) {
            userAction.add("userIntent", new JsonPrimitive("agradecimiento"));
        } else {
            System.out.println("usuario : " + userInput);
            String userType = null;
            if (userInput.has("payload")) {
                userType = userInput.get("payload").getAsString();
                userType = userUtterance.replaceAll("%2C", ",");
            }
            String currentTask = context.get("currentTask").getAsString();
            String botIntent = context.get("botIntent").getAsString();
            if (userType != null) {
                if (userType.trim().equals("requestTipos")) {
                    //obtener info tipos
                    JsonObject obj = service.getTipos();
                    if (!obj.isJsonNull()) {
                        JsonArray buttons = obj.getAsJsonArray();
                        userAction.add("userIntent", new JsonPrimitive("informacionTipos"));
                        userAction.add("botones", buttons);
                    }
                } else if (userType.trim().equals("requestIngredientes")) {
                    //obtener info ingredientes disponibles
                    System.out.println(userInput.get("title").getAsString());
                    JsonObject obj = service.getIngredientes(userUtterance);
                    if (!obj.get("ingredientes").isJsonNull()) {
                        userAction.add("userIntent", new JsonPrimitive("informacionIngre"));
                        JsonArray buttons = obj.getAsJsonArray();
                        userAction.add("botones", buttons);
                        context.add("tipo", new JsonPrimitive(userUtterance));
                    }
                } else if (userType.trim().equals("requestTiendas")) {
                    //obtener info Tiendas disponibles
                    JsonObject obj = service.getTienda(context.get("tipo").getAsString(), userUtterance);
                    if (!obj.get("tiendas").isJsonNull()) {
                        userAction.add("userIntent", new JsonPrimitive("informacionTiendas"));
                        //recorrer objeto tiendas y agregar a botones
                        JsonArray buttons = obj.getAsJsonArray();
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

    public JsonObject getBotOutput() throws IOException {

        JsonObject out = new JsonObject();
        String botIntent = context.get("botIntent").getAsString();
        JsonObject buttons = new JsonObject();
        String botUtterance = "";
        String type = "";
        if (botIntent.equals("saludoUsuario")) {
            botUtterance = "hola, que deseas en este instante? ";
            type = "saludar";
            System.out.println(service.getTipos().toString());
            buttons = service.getTipos();
        } else if (botIntent.equals("agradecimientoUsuario")) {
            botUtterance = "gracias por usar nuestro servicio, que tengas un buen dia!!";
            type = "agradecer";
        } else if (botIntent.equals("requestTipo")) {
            botUtterance = " Que deseas en este instante? ";
            type = "ofrecerTipo";
            buttons = service.getTipos();
        } else if (botIntent.equals("requestIngredientes")) {
            type = "ofrecerIngredientes";
            botUtterance = " Selecciona los ingredientes para tu " + context.get("tipo").getAsString();
            buttons = service.getIngredientes(context.get("tipo").toString());
        } else if (botIntent.equals("requestTiendas")) {
            type = "ofrecerTiendas";
            botUtterance = " estas son las tiendas que ofrecen el producto que deseas, espero te haya sido de ayuda ";
            buttons = service.getIngredientes(context.get("ingredientes").toString());
        }

        out.add("botIntent", context.get("botIntent"));
        out.add("botUtterance", new JsonPrimitive(botUtterance));
        out.add("type", new JsonPrimitive(type));
        out.add("buttons", buttons);
        System.out.println("context: " + context.toString());
        System.out.println("salida: " + out.toString());
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

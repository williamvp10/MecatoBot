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
    int estado;

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
        estado=-1;
    }

    public JsonObject process(JsonObject userInput) throws IOException {

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

    public String processFB(JsonObject userInput) throws IOException {
        JsonObject out = process(userInput);
        return out.toString();
    }

    public JsonObject processUserInput(JsonObject userInput) throws IOException {
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
           // estado=0;
        } else if (userUtterance.matches("(Gracias|gracias|GRACIAS|thanks)|(thank you)")) {
            userAction.add("userIntent", new JsonPrimitive("agradecimiento"));
        } else {
            System.out.println("usuario : " + userUtterance);
            String currentTask = context.get("currentTask").getAsString();
            String botIntent = context.get("botIntent").getAsString();
            if (currentTask.equals("request")) {
                if (botIntent.equals("requestTipos")|| estado==0) {
                    //obtener info tipos
                    JsonObject obj = service.getTipos();
                    if (!obj.isJsonNull()) {
                        JsonArray buttons = obj.getAsJsonArray();
                        userAction.add("userIntent", new JsonPrimitive("informacionTipos"));
                        buttons.add(new JsonPrimitive("pizza"));
                        userAction.add("botones", buttons);
                    }
                   // estado=1;
                } else if (botIntent.equals("requestIngredientes")|| estado==1) {
                    //obtener info ingredientes disponibles
                    JsonObject obj = service.getIngredientes(userUtterance);
                    if (!obj.get("ingredientes").isJsonNull()) {
                        userAction.add("userIntent", new JsonPrimitive("informacionIngre"));
                        JsonArray buttons = obj.getAsJsonArray();
                        userAction.add("botones", buttons);
                        context.add("tipo",new JsonPrimitive(userUtterance));
                    }
                   //  estado=2;
                } else if (botIntent.equals("requestTiendas")|| estado==2) {
                    //obtener info Tiendas disponibles
                    JsonObject obj = service.getTienda(context.get("tipo").getAsString(),userUtterance);
                    if (!obj.get("tiendas").isJsonNull()) {
                        userAction.add("userIntent", new JsonPrimitive("informacionTiendas"));
                        //recorrer objeto tiendas y agregar a botones
                        JsonArray buttons = obj.getAsJsonArray();
                        userAction.add("botones", buttons);
                    }
                   // estado=3;
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
            buttons=(JsonArray) context.get("ingredientes");
        } else if (botIntent.equals("requestTiendas")) {
            botUtterance = " estas son las tiendas que ofrecen el producto que deseas, espero te haya sido de ayuda ";
            buttons=(JsonArray) context.get("tiendas");
        }
        buttons.add(new JsonPrimitive("pizza"));
        buttons.add(new JsonPrimitive("hamburguesa"));
        out.add("botIntent", context.get("botIntent"));
        out.add("botUtterance", new JsonPrimitive(botUtterance));
        out.add("buttons", buttons);
        System.out.println("context: "+ context.toString());
        System.out.println("salida: "+out.toString());
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

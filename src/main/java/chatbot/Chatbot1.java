package chatbot;

import Services.Service;
import Services.Service1;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import java.io.IOException;
import java.util.Scanner;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonParser;

public class Chatbot1 {

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

    public Chatbot1() {
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

        //step3: identify bot intent
        identifyBotIntent();
        System.out.println("context " + context.toString());
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
        if (userInput.has("userUtterance")) {
            userUtterance = userInput.get("userUtterance").getAsString();
            userUtterance = userUtterance.replaceAll("%2C", ",");
        }

        if (userUtterance.matches("hola")) {
            userAction.add("userIntent", new JsonPrimitive("intenthola"));
        } else {
            String userType = " n ";
            if (userInput.has("userType")) {
                userType = userInput.get("userType").getAsString();
                userType = userType.replaceAll("%2C", ",");
            }

            if (userType != null) {
                if (userType.trim().equals("requestProducto")) {
                    userAction.add("userIntent", new JsonPrimitive("intentProducto"));
                    context.add("Producto", new JsonPrimitive(userUtterance));

                } else if (userType.trim().equals("requesthi")) {
                    userAction.add("userIntent", new JsonPrimitive("intenthi"));
                    context.add("hi", new JsonPrimitive(userUtterance));

                } else {
                    userAction.add("userIntent", new JsonPrimitive("intenterror"));
                }
            }
        }
        return userAction;
    }

    public void updateContext(JsonObject userAction) {
        //copy userIntent
        context.add("userIntent", userAction.get("userIntent"));
        String userIntent = context.get("userIntent").getAsString();

        if (userIntent.equals("intenterror")) {
            context.add("currentTask", new JsonPrimitive("taskerror"));
        } else if (userIntent.equals("intenthola")) {
            context.add("currentTask", new JsonPrimitive("taskhola"));
        } else if (userIntent.equals("intentProducto")) {
            context.add("currentTask", new JsonPrimitive("taskProducto"));
        } else if (userIntent.equals("intenthi")) {
            context.add("currentTask", new JsonPrimitive("taskhi"));
        }
    }

    public void identifyBotIntent() {
        String currentTask = context.get("currentTask").getAsString();
        if (currentTask.equals("taskerror")) {
            context.add("botIntent", new JsonPrimitive("boterror"));
        } else if (currentTask.equals("taskhola")) {
            context.add("botIntent", new JsonPrimitive("bothola"));
        } else if (currentTask.equals("taskProducto")) {
            context.add("botIntent", new JsonPrimitive("botProducto"));
        } else if (currentTask.equals("taskhi")) {
            context.add("botIntent", new JsonPrimitive("bothi"));
        }
    }

    public JsonObject getBotOutput() throws IOException {

        JsonObject out = new JsonObject();
        String botIntent = context.get("botIntent").getAsString();
        JsonArray buttons = new JsonArray();
        String botUtterance = "";
        String type = "";

        if (botIntent.equals("boterror")) {
            botUtterance = "error! ";
            type = "error";
            JsonObject b = null;
            out.add("buttons", buttons);
        } else if (botIntent.equals("bothola")) {
            botUtterance = "escoja producto ";
            type = "Producto";
            JsonObject b = null;
            out.add("buttons", buttons);
        } else if (botIntent.equals("botProducto")) {
            type = "Producto";
            botUtterance = "escoja producto";
            JsonObject b = null;
            JsonArray b1 = null;
            JsonArray elements = new JsonArray();
            JsonObject e = null;
            JsonObject obj = null;
            JsonObject servicio = service.getTipos();
            System.out.println("servicio"+servicio.toString());
            JsonArray elementosServicio = (JsonArray) servicio.get("product").getAsJsonArray();

            for (int i = 0; i < elementosServicio.size(); i++) {
                e = new JsonObject();
                obj = elementosServicio.get(i).getAsJsonObject();
                e.add("titulo", new JsonPrimitive(obj.get("tipo").getAsString()));
                b = new JsonObject();
                b1 = new JsonArray();
                b.add("titulo", new JsonPrimitive(obj.get("tipo").getAsString()));
                b.add("respuesta", new JsonPrimitive("request" + obj.get("TextoServicio").getAsString()));
                b1.add(b);
                e.add("buttons", b1);
                elements.add(e);
            }
            
            out.add("elements", elements);
            out.add("buttons", buttons);
        } else if (botIntent.equals("bothi")) {
            type = "hi";
            botUtterance = "hola";
            JsonObject b = null;
            out.add("buttons", buttons);
        }
        out.add("botIntent", context.get("botIntent"));
        out.add("botUtterance", new JsonPrimitive(botUtterance));
        out.add("type", new JsonPrimitive(type));
        System.out.println("context: " + context.toString());
        System.out.println("salida: " + out.toString());
        return out;
    }
}

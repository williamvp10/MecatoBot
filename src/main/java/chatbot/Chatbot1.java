package chatbot;

import Services.Service;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import java.io.IOException;
import java.util.Scanner;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonParser;

public class Chatbot1 {

    JsonObject context;

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
                if (userType.trim().equals("requestsaludo")) {
                    userAction.add("userIntent", new JsonPrimitive("intentsaludo"));
                    context.add("saludo", new JsonPrimitive(userUtterance));

                } else if (userType.trim().equals("requestbye")) {
                    userAction.add("userIntent", new JsonPrimitive("intentbye"));
                    context.add("bye", new JsonPrimitive(userUtterance));

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
        } else if (userIntent.equals("intentsaludo")) {
            context.add("currentTask", new JsonPrimitive("tasksaludo"));
        } else if (userIntent.equals("intentbye")) {
            context.add("currentTask", new JsonPrimitive("taskbye"));
        }

    }

    public void identifyBotIntent() {
        String currentTask = context.get("currentTask").getAsString();
        if (currentTask.equals("taskerror")) {
            context.add("botIntent", new JsonPrimitive("boterror"));
        } else if (currentTask.equals("taskhola")) {
            context.add("botIntent", new JsonPrimitive("bothola"));
        } else if (currentTask.equals("tasksaludo")) {
            context.add("botIntent", new JsonPrimitive("botsaludo"));
        } else if (currentTask.equals("taskbye")) {
            context.add("botIntent", new JsonPrimitive("botbye"));
        }
    }

    public JsonObject getBotOutput() throws IOException {

        JsonObject out = new JsonObject();
        String botIntent = context.get("botIntent").getAsString();
        JsonArray buttons = new JsonArray();
        String botUtterance = "";
        String type = "";

        if (botIntent.equals("boterror")) {
            botUtterance = "nmnmnm ";
            type = "error";
            JsonObject b = null;
            out.add("buttons", buttons);
        } else if (botIntent.equals("bothola")) {
            botUtterance = "hola ";
            type = "saludo";
            JsonObject b = null;
            b = new JsonObject();
            b.add("titulo", new JsonPrimitive("continuar"));
            b.add("respuesta", new JsonPrimitive("bye"));
            buttons.add(b);
            b = new JsonObject();
            b.add("titulo", new JsonPrimitive("salir"));
            b.add("respuesta", new JsonPrimitive("error"));
            buttons.add(b);
            out.add("buttons", buttons);
        } else if (botIntent.equals("botsaludo")) {
            type = "saludo";
            botUtterance = "hola";
            JsonObject b = null;
            b = new JsonObject();
            b.add("titulo", new JsonPrimitive("continuar"));
            b.add("respuesta", new JsonPrimitive("requestbye"));
            buttons.add(b);
            b = new JsonObject();
            b.add("titulo", new JsonPrimitive("salir"));
            b.add("respuesta", new JsonPrimitive("requesterror"));
            buttons.add(b);
            out.add("buttons", buttons);
        } else if (botIntent.equals("botbye")) {
            type = "bye";
            botUtterance = "adios";
            JsonObject b = null;
            b = new JsonObject();
            b.add("titulo", new JsonPrimitive("regresar"));
            b.add("respuesta", new JsonPrimitive("requestsaludo"));
            buttons.add(b);
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

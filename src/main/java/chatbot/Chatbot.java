package chatbot;

import Services.Service;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import java.io.IOException;
import java.util.Scanner;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Chatbot {

    JsonObject context;
    Service service;
    HashMap<String, Usuario> Usuarios = new HashMap<String, Usuario>();

    //main test
    public static void main(String[] args) throws IOException {
        Chatbot c = new Chatbot();
        Scanner scanner = new Scanner(System.in);
        String userUtterance;
        do {
            System.out.print("User:");
            String id = scanner.nextLine();
            String name = scanner.nextLine();
            userUtterance = scanner.nextLine();
            String type = scanner.nextLine();

            JsonObject userInput = new JsonObject();
            userInput.add("userId", new JsonPrimitive(id));
            userInput.add("userName", new JsonPrimitive(name));
            userInput.add("userUtterance", new JsonPrimitive(userUtterance));
            userInput.add("userType", new JsonPrimitive(type));
            System.out.println("input:" + userInput);
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

    public String processFB(JsonObject userInput) throws IOException {
        JsonObject out = process(userInput);
        return out.toString();
    }

    public JsonObject process(JsonObject userInput) throws IOException {
        System.out.println(userInput.toString());
        //step1: search user or add
        searchUser(userInput);
        //step2: process user input and identify bot intent
        JsonObject userAction = processUserInput(userInput);

        System.out.println("context " + context.toString());
        //step3: structure output
        JsonObject out = getBotOutput();
        System.out.println("out " + out.toString());
        return out;
    }

    public void searchUser(JsonObject userInput) {
        String userid = "", userName = "";
        //info usuario 
        if (userInput.has("userId")) {
            userid = userInput.get("userId").getAsString();
            userid = userid.replaceAll("%2C", ",");
        }
        if (userInput.has("userName")) {
            userName = userInput.get("userName").getAsString();;
        }

        if (this.Usuarios.get(userid) != null) {
            System.out.println(this.Usuarios.get(userid).getId());
        } else {
            this.Usuarios.put(userid, new Usuario(userid, userName));
        }
        context.add("userId", new JsonPrimitive(userid));
        context.add("userName", new JsonPrimitive(userName));

        for (Usuario usuario : this.Usuarios.values()) {
            System.out.println("usuario " + usuario.getId() + " : " + usuario.getNombre());
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public JsonObject processUserInput(JsonObject userInput) throws IOException {
        String userUtterance ="", userType = "";
        JsonObject userAction = new JsonObject();
        Usuario user = this.Usuarios.get(this.context.get("userId").getAsString());
        //default case
        userAction.add("userIntent", new JsonPrimitive(""));

        if (userInput.has("userUtterance")) {
            userUtterance = userInput.get("userUtterance").getAsString();
            userUtterance = userUtterance.replaceAll("%2C", ",");
        }
        if (userInput.has("userType")) {
            userType = userInput.get("userType").getAsString();
            userType = userType.replaceAll("%2C", ",");
        }

        if (userType.length() != 0 ) {
            System.out.println("userType: " + userType);
            String[] type = userType.split(":");
            Pedido p = null;
            switch (type[0]) {
                case "requestIngredientes":
                    //obtener info ingredientes disponibles
                    context.add("botIntent", new JsonPrimitive("requestIngredientes"));
                    p = user.getPedido();
                    p.setTipo(type[1]);
                    break;
                case "requestTiendas":
                    //obtener info tiendas disponibles
                    context.add("botIntent", new JsonPrimitive("requestTiendas"));
                    p = user.getPedido();
                    String[] ingredientes = type[1].split(",");//guardar ingredientes
                    for (String ing : ingredientes) {
                        p.setIngredientes(ing);
                    }
                    System.out.println(p.getIngredientes().toArray());
                    break;
                case "confirmar pedido":
                    context.add("botIntent", new JsonPrimitive("requestConfirmar"));
                    p = user.getPedido();
                    p.setTienda(type[1]);
                    break;
                case "confirmandoPedido":
                    context.add("botIntent", new JsonPrimitive("confirmandoPedido"));
                    if (userUtterance.equals("Si")) {
                        context.add("botIntent", new JsonPrimitive("finalizarPedido"));
                    } else {
                        context.add("botIntent", new JsonPrimitive("menu"));
                    }
                    break;
            }

        } else if (userUtterance.length() != 0 ) {
            System.out.println("userUtterance: " + userUtterance);
            Pedido p = null;
            JsonObject intent = this.service.getIntent(userUtterance);
            String intent_name = "";
            JsonArray entities = null;
            try {
                intent_name = intent.get("intent").getAsJsonObject().get("name").getAsString();
                entities = intent.get("entities").getAsJsonArray();
            } catch (Exception ex) {
                intent_name = "";
            }
            Double confidence = Double.parseDouble(intent.get("intent").getAsJsonObject().get("confidence").getAsString());
            if (confidence > 0.5) {
                switch (intent_name) {
                    case "saludo":
                        context.add("botIntent", new JsonPrimitive("saludoUsuario"));
                        break;
                    case "menu":
                        context.add("botIntent", new JsonPrimitive("menu"));
                        break;
                    case "agradecimiento":
                        context.add("botIntent", new JsonPrimitive("agradecimientoUsuario"));
                        break;
                    case "confirmar_pedido":
                        context.add("botIntent", new JsonPrimitive("requestFinalizarPedido"));
                        break;
                    case "negar_pedido":
                        context.add("botIntent", new JsonPrimitive("negar"));
                        break;
                    case "resumen_pedido":
                        p = user.getPedido();
                        if (p.getTipo().length() != 0 || p.getTipo().length() != 0) {
                            context.add("botIntent", new JsonPrimitive("requestConfirmar"));
                        } else {
                            context.add("botIntent", new JsonPrimitive("menu"));
                        }
                        break;
                    case "producto_intent":
                        p = user.getPedido();
                        if (entities.size() != 0) {
                            p.setTipo(entities.get(0).getAsJsonObject().get("value").getAsString());
                            System.out.println(" consultar tipo " + user.getPedido().getTipo());
                            context.add("botIntent", new JsonPrimitive("requestIngredientes"));
                        } else {
                            context.add("botIntent", new JsonPrimitive("menu"));
                        }
                        break;
                    case "ingrediente_intent":
                        if (entities.size() != 0) {
                            p = user.getPedido();
                            for (int i = 0; i < entities.size(); i++) {
                                JsonObject entity = entities.get(i).getAsJsonObject();
                                if (entity.get("entity").getAsString().equals("ingrediente")) {
                                    p.addIngredientes(entity.get("value").getAsString());
                                }
                                if (entity.get("entity").getAsString().equals("producto")) {
                                    p.setTipo(entity.get("value").getAsString());
                                }
                            }
                            context.add("botIntent", new JsonPrimitive("requestTiendas"));
                        } else {
                            context.add("botIntent", new JsonPrimitive("menu"));
                        }
                        break;
                    case "tienda_intent":
                        if (entities.size() != 0) {
                            p = user.getPedido();
                            p.setTienda(entities.get(0).getAsJsonObject().get("value").getAsString());
                            System.out.println(" seleccionar tienda " + user.getPedido().getTipo());
                            if (p.getTipo().length() != 0 || p.getTipo().length() != 0) {
                                context.add("botIntent", new JsonPrimitive("requestConfirmar"));
                            } else {
                                context.add("botIntent", new JsonPrimitive("menu"));
                            }
                        } else {
                            context.add("botIntent", new JsonPrimitive("menu"));
                        }
                        break;
                    default:
                        System.out.println("nlp no pudo procesar el texto");
                        context.add("botIntent", new JsonPrimitive("intentError"));
                        break;
                }
            } else {
                context.add("botIntent", new JsonPrimitive("intentError"));
            }
        } else {
            context.add("botIntent", new JsonPrimitive("intentError"));
        }
        return userAction;
    }

    public JsonObject getBotOutput() throws IOException {
        Usuario user = this.Usuarios.get(this.context.get("userId").getAsString());
        JsonObject out = new JsonObject();
        String botIntent = context.get("botIntent").getAsString();
        JsonObject buttons = new JsonObject();
        String botUtterance = "";
        String type = "";
        String tipo = "";
        switch (botIntent) {
            case "saludoUsuario":
                botUtterance = "hola en que te puedo ayudar ";
                type = "saludar";
                out = getbotsaludo();
                break;
            case "menu":
                botUtterance = "selecciona un producto";
                type = "menu";
                out = getbotMenu();
                break;
            case "agradecimientoUsuario":
                botUtterance = "gracias por usar nuestro servicio, que tengas un buen dia!!";
                type = "agradecer";
                out = getbotAgradecimiento();
                break;
            case "requestFinalizarPedido":
                botUtterance = " tu pedido ha sido procesado, estar�mos all� en poco tiempo";
                type = "finalizar";
                out = getbotsaludo();
                break;
            case "requestTipo":
                botUtterance = " Que deseas en este instante? ";
                type = "ofrecerTipo";
                out = getbotMenu();
                break;
            case "requestIngredientes":
                type = "ofrecerIngredientes";
                tipo = user.getPedido().getTipo();
                botUtterance = " Selecciona los ingredientes para tu " + tipo;
                out = getbotIngredientes(user);
                break;
            case "requestTiendas":
                tipo = user.getPedido().getTipo();
                ArrayList<String> ing = user.getPedido().getIngredientes();
                String ingrediente = "";
                for (int i = 0; i < ing.size(); i++) {
                    if (i != 0) {
                        ingrediente += ",";
                    }
                    ingrediente += ing.get(i);
                }
                type = "ofrecerTiendas";
                botUtterance = " estas son las tiendas que ofrecen " + tipo + " con ingredientes " + ingrediente;
                out = getbotTiendas(user);
                break;
            case "requestConfirmar":
                type = "confirmarPedido";
                botUtterance = "Confirme su pedido";
                JsonParser parser = new JsonParser();
                String infoPedido = new Gson().toJson(user.getPedido());
                out.add("Pedido", (JsonObject) parser.parse(infoPedido));
                break;
            case "boterror":
                botUtterance = "gracias por usar nuestro servicio, que tengas un buen dia!!";
                type = "agradecer";
                break;
            default:

                break;
        }
        out.add("botIntent", context.get("botIntent"));
        out.add("botUtterance", new JsonPrimitive(botUtterance));
        out.add("type", new JsonPrimitive(type));
        System.out.println("context: " + context.toString());
        System.out.println("salida: " + out.toString());
        return out;
    }

    public JsonObject getbotsaludo() {
        JsonObject out = new JsonObject();
        JsonArray buttons = new JsonArray();
        out.add("buttons", buttons);
        return out;
    }

    public JsonObject getbotAgradecimiento() {
        JsonObject out = new JsonObject();
        JsonArray buttons = new JsonArray();
        out.add("buttons", buttons);
        return out;
    }

    public JsonObject getbotMenu() {
        JsonObject out = new JsonObject();
        JsonArray buttons = new JsonArray();
        JsonObject b = null;
        JsonArray b1 = null;
        JsonArray elements = new JsonArray();
        JsonObject e = null;
        JsonObject servicio = null;
        try {
            servicio = service.getTipos();
        } catch (Exception ex) {

        }
        JsonArray elementosServicio = (JsonArray) servicio.get("product").getAsJsonArray();

        for (int i = 0; i < elementosServicio.size(); i++) {
            e = new JsonObject();
            JsonObject obj = elementosServicio.get(i).getAsJsonObject();
            e.add("titulo", new JsonPrimitive("" + "" + obj.get("tipo").getAsString()));
            b = new JsonObject();
            b1 = new JsonArray();
            b.add("titulo", new JsonPrimitive(obj.get("tipo").getAsString()));
            b.add("respuesta", new JsonPrimitive("requestIngredientes:" + obj.get("tipo").getAsString()));
            b1.add(b);
            e.add("buttons", b1);
            elements.add(e);
        }
        out.add("elements", elements);
        out.add("buttons", buttons);
        return out;
    }

    public JsonObject getbotIngredientes(Usuario user) {
        JsonObject out = new JsonObject();
        JsonArray buttons = new JsonArray();
        JsonObject b = null;
        JsonArray b1 = null;
        JsonArray elements = new JsonArray();
        JsonObject e = null;
        JsonObject obj = null;
        JsonObject servicio = null;
        try {
            servicio = service.getIngredientes(user.getPedido().getTipo());
            System.out.println(servicio);
            JsonArray elementosServicio = servicio.get("Ingredient").getAsJsonArray();
            for (int i = 0; i < elementosServicio.size(); i++) {
                e = new JsonObject();
                obj = elementosServicio.get(i).getAsJsonObject();
                e.add("titulo", new JsonPrimitive("" + "" + obj.get("nombre").getAsString()));
                b = new JsonObject();
                b1 = new JsonArray();
                b.add("titulo", new JsonPrimitive(obj.get("nombre").getAsString()));
                b.add("respuesta", new JsonPrimitive("addIngredient:" + obj.get("nombre").getAsString()));
                b1.add(b);
                e.add("buttons", b1);
                elements.add(e);
            }
        } catch (IOException ex) {

        }
        b = new JsonObject();
        b.add("titulo", new JsonPrimitive("enviar"));
        b.add("respuesta", new JsonPrimitive("requestTiendas"));
        buttons.add(b);
        out.add("elements", elements);
        out.add("buttons", buttons);
        return out;
    }

    public JsonObject getbotTiendas(Usuario user) {
        JsonObject out = new JsonObject();
        JsonArray buttons = new JsonArray();
        JsonObject b = null;
        JsonArray b1 = null;
        JsonArray elements = new JsonArray();
        JsonObject e = null;
        JsonObject obj = null;
        ArrayList<String> ing = user.getPedido().getIngredientes();
        String ingrediente = "";
        for (int i = 0; i < ing.size(); i++) {
            if (i != 0) {
                ingrediente += ",";
            }
            ingrediente += ing.get(i);
        }
        JsonObject servicio = null;
        try {
            servicio = service.getTienda(user.getPedido().getTipo(), ingrediente);
        } catch (IOException ex) {
            System.out.println(" error servicio tienda");
        }
        JsonArray elementosServicio = (JsonArray) servicio.get("tienda").getAsJsonArray();

        for (int i = 0; i < elementosServicio.size(); i++) {
            e = new JsonObject();
            obj = elementosServicio.get(i).getAsJsonObject();
            e.add("titulo", new JsonPrimitive("" + "" + obj.get("nombre").getAsString()));
            b = new JsonObject();
            b1 = new JsonArray();
            b.add("titulo", new JsonPrimitive(obj.get("nombre").getAsString()));
            b.add("respuesta", new JsonPrimitive("requestResultados:" + obj.get("nombre").getAsString()));
            b1.add(b);
            e.add("buttons", b1);
            elements.add(e);
        }
        out.add("elements", elements);
        out.add("buttons", buttons);
        return out;
    }

}

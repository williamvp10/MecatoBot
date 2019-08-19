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
            userUtterance = scanner.nextLine();
            String type = scanner.nextLine();

            JsonObject userInput = new JsonObject();
            userInput.add("userId", new JsonPrimitive(id));
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
        processUserInput(userInput);
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
        if (this.Usuarios.get(userid) != null) {
            System.out.println(this.Usuarios.get(userid).getId());
        } else {
            JsonObject infouser = null;
            try {
                infouser = service.getUserFB(userid);
            } catch (IOException ex) {
                System.out.println("error al buscar usuario ");
            }
            if (infouser != null) {
                System.out.println();
                userName = infouser.get("first_name").getAsString();
                this.Usuarios.put(userid, new Usuario(userid, userName));
            }
        }
        context.add("userId", new JsonPrimitive(userid));
        context.add("userName", new JsonPrimitive(userName));

        for (Usuario usuario : this.Usuarios.values()) {
            System.out.println("usuario " + usuario.getId() + " : " + usuario.getNombre());
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void processUserInput(JsonObject userInput) throws IOException {
        String userUtterance = "", userType = "";
        Usuario user = this.Usuarios.get(this.context.get("userId").getAsString());
        //default case
        if (userInput.has("userUtterance")) {
            userUtterance = userInput.get("userUtterance").getAsString();
            userUtterance = userUtterance.replaceAll("%2C", ",");
        }
        if (userInput.has("userType")) {
            userType = userInput.get("userType").getAsString();
            userType = userType.replaceAll("%2C", ",");
        }
        Pedido p = user.getPedido();
        if (userType.length() != 0) {
            System.out.println("userType: " + userType);
            String[] type = userType.split(":");
            switch (type[0]) {
                case "addIngredient":
                    //obtener info ingredientes disponibles
                    if (findPedidoTipo(p)) {
                        p.addIngredientes(type[1]);
                        context.add("botIntent", new JsonPrimitive("addIngredient"));
                    }
                    break;
                case "requestIngredientes":
                    p.setTipo(type[1]);
                    if (findPedidoTipo(p)) {
                        //obtener info ingredientes disponibles
                        context.add("botIntent", new JsonPrimitive("requestIngredientes"));

                    }
                    break;
                case "requestTiendas":
                    //obtener info tiendas disponibles
                    if (findPedidoTipo(p) && findPedidoIngredientes(p)) {
                        context.add("botIntent", new JsonPrimitive("requestTiendas"));
                    }
                    break;
                case "requestResultados":
                    p.setTienda(type[1]);
                    if (findPedidoTipo(p) && findPedidoIngredientes(p) && findPedidoTienda(p)) {
                        context.add("botIntent", new JsonPrimitive("requestConfirmar"));
                    }
                    break;
                case "confirmandoPedido":
                    if (findPedidoTipo(p) && findPedidoIngredientes(p) && findPedidoTienda(p)) {
                        context.add("botIntent", new JsonPrimitive("confirmandoPedido"));
                        if (userUtterance.equals("Si")) {
                            context.add("botIntent", new JsonPrimitive("requestFinalizarPedido"));
                        } else {
                            p.ClearPedido();
                            context.add("botIntent", new JsonPrimitive("menu"));
                        }
                    }
                    break;
            }

        } else if (userUtterance.length() != 0) {
            System.out.println("userUtterance: " + userUtterance);
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
                        p.ClearPedido();
                        context.add("botIntent", new JsonPrimitive("menu"));
                        break;
                    case "agradecimiento":
                        context.add("botIntent", new JsonPrimitive("agradecimientoUsuario"));
                        break;
                    case "confirmar_pedido":
                        if (findPedidoTipo(p) && findPedidoIngredientes(p) && findPedidoTienda(p)) {
                            context.add("botIntent", new JsonPrimitive("requestFinalizarPedido"));
                        }
                        break;
                    case "negar_pedido":
                        if (findPedidoTipo(p) && findPedidoIngredientes(p) && findPedidoTienda(p)) {
                            context.add("botIntent", new JsonPrimitive("negar"));
                        }
                        break;
                    case "resumen_pedido":
                        p = user.getPedido();
                        if (findPedidoTipo(p) && findPedidoIngredientes(p) && findPedidoTienda(p)) {
                            context.add("botIntent", new JsonPrimitive("requestConfirmar"));
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
                            if (findPedidoTipo(p) && findPedidoIngredientes(p)) {
                                context.add("botIntent", new JsonPrimitive("requestTiendas"));
                            }
                        } else {
                            context.add("botIntent", new JsonPrimitive("menu"));
                        }
                        break;
                    case "tienda_intent":
                        if (entities.size() != 0) {
                            p = user.getPedido();
                            p.setTienda(entities.get(0).getAsJsonObject().get("value").getAsString());
                            System.out.println(" seleccionar tienda " + user.getPedido().getTipo());
                            if (findPedidoTipo(p) && findPedidoIngredientes(p)) {
                                context.add("botIntent", new JsonPrimitive("requestConfirmar"));
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
    }

    public boolean findPedidoTipo(Pedido p) {
        boolean res = false;
        if (p.getTipo().length() != 0) {
            res = true;
        } else {
            context.add("botIntent", new JsonPrimitive("menu"));
        }
        System.out.println("restipo :" + res);
        return res;
    }

    public boolean findPedidoIngredientes(Pedido p) {
        boolean res = false;
        if (p.getIngredientes().size() != 0) {
            res = true;
        } else {
            context.add("botIntent", new JsonPrimitive("requestIngredientes"));
        }
        System.out.println("resingre :" + res);
        return res;
    }

    public boolean findPedidoTienda(Pedido p) {
        boolean res = false;
        if (p.getTienda().length() != 0) {
            res = true;
        } else {
            context.add("botIntent", new JsonPrimitive("requestTiendas"));
        }
        System.out.println("restienda :" + res);
        return res;
    }

    public JsonObject getBotOutput() throws IOException {
        Usuario user = this.Usuarios.get(this.context.get("userId").getAsString());
        JsonObject out = new JsonObject();
        String botIntent = context.get("botIntent").getAsString();
        String botUtterance = "";
        String type = "";
        String tipo = "";
        switch (botIntent) {
            case "saludoUsuario":
                botUtterance = "hola " + user.getNombre() + " en que te puedo ayudar ";
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
                botUtterance = " tu pedido ha sido procesado, estarémos allá en poco tiempo";
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
                out.add("msg", (JsonArray) getimagenes(user.getPedido().getIngredientes()));
                out.add("username", new JsonPrimitive(user.getNombre()));
                break;
            case "boterror":
                botUtterance = "gracias por usar nuestro servicio, que tengas un buen dia!!";
                type = "error";
                break;
            case "addIngredient":
                botUtterance = "ingrediente agregado";
                type = "addIngredient";
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
            e.add("subtitulo", new JsonPrimitive("" + "" + obj.get("tipo").getAsString()));
            e.add("url", new JsonPrimitive(getImagen(obj.get("tipo").getAsString())));
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
                e.add("subtitulo", new JsonPrimitive("" + "" + obj.get("nombre").getAsString()));
                e.add("url", new JsonPrimitive(getImagen(obj.get("nombre").getAsString())));
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
            e.add("subtitulo", new JsonPrimitive("" + "" + obj.get("nombre").getAsString()));
            e.add("url", new JsonPrimitive(getImagen(obj.get("nombre").getAsString())));
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
    
    public JsonArray getimagenes(ArrayList<String> ingredientes) {
        JsonArray out = new JsonArray();
        for (int i = 0; i < ingredientes.size(); i++) {
            out.add(new JsonPrimitive(getImagen(ingredientes.get(i))));
        }
        return out ;
    }
    
    public String getImagen(String tipo) {
        String res = "";

        switch (tipo) {
            case "pizza":
                res = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSW0vzWNFZ7XKFDLj3KNU2XH5GmTyXqo34hcofaGXv-2-7pH_17";
                break;
            case "hamburguesa":
                res = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQPbiKXDww3egt_sXWXEKFRo8ThQxQ8C1pnZ4KcEx6NJENh_2UM";
                break;
            case "jamon":
                res = "https://www.consumer.es/wp-content/uploads/2019/07/img_jamon-york-bebe-hd.jpg";
                break;
            case "queso":
                res = "https://previews.123rf.com/images/peterhermesfurian/peterhermesfurian1611/peterhermesfurian161100063/66300148-rallado-queso-para-pizza-de-mozzarella-en-un-taz%C3%B3n-de-madera-sobre-blanco-cheddar-como-el-queso-italian.jpg";
                break;
            case "champiñones":
                res = "https://www.hogarmania.com/archivos/201202/champinones-668x400x80xX.jpg";
                break;
            case "pollo":
                res = "https://previews.123rf.com/images/duplass/duplass0805/duplass080500061/3054741-bowl-de-pechuga-de-pollo-desmenuzado-en-un-taz%C3%B3n-en-la-cocina-o-restaurante-.jpg";
                break;
            case "piña":
                res = "https://cdn.chapintv.com/files/2018/06/29/rodajas-de-pi%C3%B1a.jpg";
                break;
            case "tomates":
                res = "https://www.webconsultas.com/sites/default/files/styles/encabezado_articulo/public/migrated/tomate.jpg";
                break;
            case "oregano":
                res = "https://img.vixdata.io/pd/webp-large/es/sites/default/files/imj/elgranchef/t/trucos-para-cocinar-con-oregano-1.jpg";
                break;
            case "bocadillo":
                res = "https://upload.wikimedia.org/wikipedia/commons/6/60/Bocadillo.jpg";
                break;
            case "carne":
                res = "https://www.chefzeecooks.com/wp-content/uploads/2018/08/Carne_Asada_web.jpg";
                break;
            case "salchichon":
                res = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQGotIyRSsRUpgjpZob2ZgO4zC6Zy7ZPB9CJ4ceewwgWFNX1CuFmg";
                break;
            case "cebolla-salsa-especial":
                res = "https://media-cdn.tripadvisor.com/media/photo-s/11/cc/71/1f/tartin-salsa-especial.jpg";
                break;
            case "tocino":
                res = "https://cdn2.cocinadelirante.com/sites/default/files/styles/gallerie/public/images/2018/10/como-caramelizar-tocino-receta.jpg";
                break;
            case "aros-de-cebolla":
                res = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQa7aCjpDrAtEEEOig9WcqCy3zGFRgRETsKY_jYJvzo8t4DgJse";
                break;
            case "salsa-de-la casa":
                res = "https://f4d5s4a5.stackpathcdn.com/wp-content/uploads/2018/05/The_Best_Restaurant_Style_Salsa_Sweet_Simple_Vegan9-copy.jpg";
                break;
            case "lechuga":
                res = "https://biotrendies.com/wp-content/uploads/2015/07/lechuga-1000x600.jpg";
                break;
            case "salsa-de-tomate":
                res = "https://hogarmania.global.ssl.fastly.net/hogarmania/images/images01/2017/03/30/5c00ff665a2c1100017758a5/1239x697.jpg";
                break;
            case "Dominos Pizza":
                res = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQmEyuTGop8eujX5KwCiw2IV6SGZnAkauBArEYCEEAdHjrlD2HbeQ";
                break;
            case "Papa johns":
                res = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ0rt1DX1By-siqqhUB9zuJ-EWp9wL4BEtr2a1LWsU1fZe-V5Ah";
                break;
            case "Corral":
                res = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTY4Zt2ZhwhEPQXt98GlfRX4fPeUcy1lnjr0IwqgTP4RhMn45hH";
                break;
            case "Rodeo":
                res = "https://www.hamburguesasdelrodeo.com/bundles/app/img/home/logo.png";
                break;
            default:
                res = "";
                break;
        }

        return res;
    }
    
}

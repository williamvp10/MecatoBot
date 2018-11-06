package Services;

import java.io.IOException;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import jdk.nashorn.internal.parser.JSONParser;

public class Service {

    public Service() {
    }

    public JsonObject getTipos()
            throws ClientProtocolException, IOException {
        System.out.println("entroooo");
        String url1 = "https://servicemecatobot.herokuapp.com/myApp/rest/products";

        URL url = new URL(url1);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        System.out.println("mensaje: "+content);
        in.close();
        con.disconnect();
        JsonParser parser = new JsonParser();

        JsonObject json= (JsonObject) parser.parse(content.toString());
        return json;
    }

    public JsonObject getIngredientes(String tipo)
            throws ClientProtocolException, IOException {

        //step 1: Prepare the url
        String url1 = "https://servicemecatobot.herokuapp.com/myApp/rest/products/" + tipo;

         URL url = new URL(url1);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        System.out.println(content);
        in.close();
        con.disconnect();
        JsonParser parser = new JsonParser();

        JsonObject json= (JsonObject) parser.parse(content.toString());
        return json;
    }

    public JsonObject getTienda(String tipo, String ing)
            throws ClientProtocolException, IOException {

        //step 1: Prepare the url
        String url1 = "https://servicemecatobot.herokuapp.com/myApp/rest/tienda/" + tipo + "/" + ing;

         URL url = new URL(url1);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        System.out.println(content);
        in.close();
        con.disconnect();
        JsonParser parser = new JsonParser();

        JsonObject json= (JsonObject) parser.parse(content.toString());
        return json;
    }

}

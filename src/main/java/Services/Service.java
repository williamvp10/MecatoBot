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
import com.google.gson.GsonBuilder;
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
        String url = "https://servicemecatobot.herokuapp.com/myApp/rest/products";

         //step 2: Create a HTTP client
        HttpClient httpclient = HttpClientBuilder.create().build();

        //step 3: Create a HTTPGet object and execute the url
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = httpclient.execute(httpGet);

        //step 4: Process the result
        JsonObject json = null;
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 200) {
            String response_string = EntityUtils.toString(response.getEntity());
            json = (new JsonParser()).parse(response_string)
                    .getAsJsonObject();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String prettyJson = gson.toJson(json);
            System.out.println(prettyJson);
        }
        return json;
    }

    public JsonObject getIngredientes(String tipo)
            throws ClientProtocolException, IOException {

        //step 1: Prepare the url
        String url = "https://servicemecatobot.herokuapp.com/myApp/rest/products/" + tipo;

        //step 2: Create a HTTP client
        HttpClient httpclient = HttpClientBuilder.create().build();

        //step 3: Create a HTTPGet object and execute the url
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = httpclient.execute(httpGet);

        //step 4: Process the result
        JsonObject json = null;
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 200) {
            String response_string = EntityUtils.toString(response.getEntity());
            json = (new JsonParser()).parse(response_string)
                    .getAsJsonObject();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String prettyJson = gson.toJson(json);
            System.out.println(prettyJson);
        }
        return json;
    }

    public JsonObject getTienda(String tipo, String ing)
            throws ClientProtocolException, IOException {

        //step 1: Prepare the url
        String url = "https://servicemecatobot.herokuapp.com/myApp/rest/tienda/" + tipo + "/" + ing;

        //step 2: Create a HTTP client
        HttpClient httpclient = HttpClientBuilder.create().build();

        //step 3: Create a HTTPGet object and execute the url
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = httpclient.execute(httpGet);

        //step 4: Process the result
        JsonObject json = null;
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 200) {
            String response_string = EntityUtils.toString(response.getEntity());
            json = (new JsonParser()).parse(response_string)
                    .getAsJsonObject();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String prettyJson = gson.toJson(json);
            System.out.println(prettyJson);
        }
        return json;
    }

}

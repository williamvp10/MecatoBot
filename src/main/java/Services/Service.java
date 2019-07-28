package Services;

import java.io.IOException;

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
import com.google.gson.JsonPrimitive;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;

public class Service {

    public Service() {
    }

    public JsonObject getUserFB(String id)
            throws ClientProtocolException, IOException {
        String token = "EAADiQpmWQRgBAPglvHwxHZCMaXlZBHHjADrALySMQvlwR4wl5MbnhW5ZA3JDaKqOagA6ZC32lZBoDAv0mYO3rwgJtlihDcGAnfmb3xgj5YTen2ZBPA4a3zsSot4TVB7W0xdjnrmh4ZAt4NVvmBoZAzONDTmWNh119KA1f4YQZA18towZDZD";
        String url = "https://graph.facebook.com/" + id + "?fields=first_name,last_name&access_token=" + token;
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

    public JsonObject getIntent(String text)
            throws ClientProtocolException, IOException {
        String url = "" + "https://nlpmecatobot.herokuapp.com/intent"; //step 2: Create a HTTP client
        HttpClient httpclient = HttpClientBuilder.create().build();

        //step 3: Create a HTTPPost object and execute the url
        HttpPost httpPost = new HttpPost(url);
        JsonObject json2 = new JsonObject();
        json2.add("text", new JsonPrimitive(text));
        StringEntity params = new StringEntity(json2.toString());
        System.out.println("json post:" + json2.toString());
        httpPost.setEntity(params);
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        System.out.println("post:" + httpPost);
        JsonObject json = null;
        HttpResponse response = null;
        try {
            response = httpclient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            System.out.println("status:" + statusCode);
            if (statusCode == 200) {
                String response_string = EntityUtils.toString(response.getEntity());
                json = (new JsonParser()).parse(response_string)
                        .getAsJsonObject();
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String prettyJson = gson.toJson(json);
                System.out.println(prettyJson);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.out.println("error");
        }
        return json;
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
        String url = "https://servicemecatobot.herokuapp.com/myApp/rest/products/" + tipo.trim();

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
        System.out.println("tipo prodcto: " + tipo);
        System.out.println("ing: " + ing);
        //step 1: Prepare the url
        String url = "https://servicemecatobot.herokuapp.com/myApp/rest/tiendas/" + tipo.trim() + "/" + ing.trim();

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

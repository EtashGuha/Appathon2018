package com.example.etashguha.etude;

import android.os.Message;
import android.os.StrictMode;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.os.StrictMode.setThreadPolicy;

public class OCR extends Thread{

    public static String API_KEY = "AIzaSyAKtutaW6bmH036oB1t8ViQagm_-OItNLc";
    public static String targetURL = "https://vision.googleapis.com/v1/images:annotate?key=" + API_KEY;
    public static Reader.MyHandler myHandler;
    String encodedImage;

    public OCR(Reader.MyHandler myHandler, String encodedImage){
        super();
        this.myHandler = myHandler;
        this.encodedImage = encodedImage;
    }

    @Override
    public void run(){
        Message msg = new Message();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        setThreadPolicy(policy);
        String body = "{\n" +
                "  \"requests\":[\n" +
                "    {\n" +
                "      \"image\":{\n" +
                "        \"content\":\""+encodedImage+"\""+"\n" +
                "      },\n" +
                "      \"features\":[\n" +
                "        {\n" +
                "          \"type\":\"DOCUMENT_TEXT_DETECTION\",\n" +
                "          \"maxResults\":4\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        HttpURLConnection connection = null;
        try {
            URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Content-Length", Integer.toString(body.getBytes().length));

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            wr.writeBytes(body);
            wr.close();


            //getting response

            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\n');
            }
            rd.close();

            JsonElement jelement = new JsonParser().parse(response.toString());
            JsonObject jsonObject = jelement.getAsJsonObject();
            JsonArray jarray = jsonObject.getAsJsonArray("responses");
            JsonObject jsonObjectTwo = jarray.get(0).getAsJsonObject();
            JsonArray jsonArrayTwo = jsonObjectTwo.getAsJsonArray("textAnnotations");
            JsonObject jsonObjectThree = jsonArrayTwo.get(0).getAsJsonObject();
            String result = jsonObjectThree.get("description").getAsString();
            // decode json
            // decode base64
            // output is a wave file
            msg.obj = result;
            myHandler.sendMessage(msg);

        } catch (Exception e) {
            e.printStackTrace();
            msg.obj = "fail";
            myHandler.sendMessage(msg);

        } finally {
            if (connection != null) {
                connection.disconnect();
                return;
            }
        }
    }
}

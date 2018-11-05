package com.example.etashguha.etude;

import android.os.StrictMode;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TTS {
    public static String API_KEY = "AIzaSyBUn126qSoVu_eGMiK8P_FFVrR0bFdPJ0E";
    //"AIzaSyCSomc9QfAYDy4UvLl_i_l36XPazS0jNro";
    public static String targetURL = "https://texttospeech.googleapis.com/v1/text:synthesize?key=" + API_KEY;

    public static String executePost(String text) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        String newtext = text.replaceAll("–","");
        //System.out.println(newtext);
        String body = "{\n" +
                "\t\"input\": {\n" +
                "\t\t\"text\": \"" + newtext + "\"\n" +
                "\t},\n" +
                "\t\"voice\": {\n" +
                "\t\t\"languageCode\": \"en-US\"\n" +
                "\t},\n" +
                "\t\"audioConfig\":  {\n" +
                "\t\t\"audioEncoding\": \"MP3\"\n" +
                "\t}\n" +
                "}";


        HttpURLConnection connection = null;
        try {
            URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Content-Length", Integer.toString(body.getBytes("UTF-8").length));

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

            String jsonLine = response.toString();
            JsonElement jelement = new JsonParser().parse(jsonLine);
            JsonObject jobject = jelement.getAsJsonObject();
            //jobject = jobject.getAsJsonObject("data");
            //JsonArray jarray = jobject.getAsJsonArray("translations");
            //jobject = jarray.get(0).getAsJsonObject();
            String result = jobject.get("audioContent").getAsString();
            return result;

            //System.out.println("");
            //System.out.println("!!" + response.toString() + "==");

            // decode json
            // decode base64
            // output is a wave file
//            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Fail";

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }


    }
}

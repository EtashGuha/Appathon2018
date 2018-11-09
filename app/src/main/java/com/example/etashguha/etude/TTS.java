package com.example.etashguha.etude;

import android.os.Message;
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

public class TTS extends Thread{
    final private static String API_KEY = "AIzaSyBUn126qSoVu_eGMiK8P_FFVrR0bFdPJ0E";
    final private static String targetURL = "https://texttospeech.googleapis.com/v1/text:synthesize?key=" + API_KEY;
    Reader.TTSHandler ttsHandler;
    String text;

    public TTS(Reader.TTSHandler ttsHandler, String text){
        super();
        this.ttsHandler = ttsHandler;
        this.text = text;
    }

    public void run() {
        Message msg = new Message();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        String newtext = text.replaceAll("[^a-zA-Z0-9 .,\n]", " ");
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
            String result = jobject.get("audioContent").getAsString();
            msg.obj = result;
            ttsHandler.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
            msg.obj = "fail";
            ttsHandler.sendMessage(msg);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}

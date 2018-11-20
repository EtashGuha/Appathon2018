package com.example.etashguha.etude;

import android.app.Activity;
import android.net.Uri;
import android.os.Message;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class Dictionary extends Thread {

    Activity currentActivity;
    Reader.DictionaryHandler handler;
    String word;
    public Dictionary(Activity activity, Reader.DictionaryHandler dictionaryHandler, String word){
        currentActivity = activity;
        handler = dictionaryHandler;
        this.word = word;
    }

    public void run(){
        RequestQueue requestQueue = Volley.newRequestQueue(currentActivity);
        String toParse = "https://wordsapiv1.p.mashape.com/words/" + word + "/definitions";
        String uri = Uri.parse(toParse)
                .buildUpon()
                .build().toString();

        StringRequest stringRequest = new StringRequest(
                Request.Method.GET, uri, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Message message = new Message();
                message.obj = response;
                handler.handleMessage(message);
            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VolleyError", error.toString());
            }

        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<>();
                params.put("X-Mashape-Key", "BNrk40KgrTmshu76EmLWGokuzqvXp1JgmKbjsnrYxFk7id8pWk");
                params.put("Accept", "text/plain");
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }
}

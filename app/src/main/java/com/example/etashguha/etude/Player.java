package com.example.etashguha.etude;

import android.media.MediaPlayer;

public class Player {
    String url;
    MediaPlayer player;

    public Player(String data){
        url = "data:audio/wave;base64," + data;
        player = new MediaPlayer();
    }

    public void startSpeaking(){
        try {
            player.setDataSource(url);
            player.prepare();
            player.start();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void pauseSpeaking(){
        player.pause();
    }

    public void resumeSpeaking(){
        player.start();
    }

    public void stopTalking(){
        player.stop();
    }
}

package com.example.etashguha.etude;

import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.barteksc.pdfviewer.PDFView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.os.StrictMode.setThreadPolicy;

public class Reader extends AppCompatActivity {

    private TextView mTextMessage;
    PDFView pdfView;
    PausePlay pausePlayState = PausePlay.PLAYING;
    int pageNumber = 0;
    Screenshot screenshot;
    boolean firstTimePlaying;
    MyHandler myHandler;
    OCR ocr;
    Player player;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reader);

        screenshot = new Screenshot(this);
        final Uri uri = getIntent().getData();
        myHandler = new MyHandler(this);
        pdfView = findViewById(R.id.pdfView);
        firstTimePlaying = true;


        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        pdfView.fromUri(uri).pages(pageNumber).load();
        final BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.back_button:
                        if(pageNumber != 0){
                            pageNumber--;
                            pdfView.fromUri(uri).pages(pageNumber).load();
                        }
                        player.stopTalking();
                        firstTimePlaying = true;
                        return true;
                    case R.id.play_pause_button:
                        System.out.println("jo");
                        if(pausePlayState == PausePlay.PLAYING && firstTimePlaying){
                            item.setIcon(R.drawable.pause_image);
                            firstTimePlaying = false;
                            pausePlayState = PausePlay.PAUSED;
                            String encodedImage = screenshot.getBase64String();
                            ocr = new OCR(myHandler, encodedImage);
                            ocr.start();
                            //text = text.replaceAll("[^a-zA-Z0-9 .,\n]", " ");
                            //String readyForMediaPlayer = TTS.executePost(text);
                           // player = new Player(readyForMediaPlayer);
                           // player.startSpeaking();
                        } else if(pausePlayState == PausePlay.PAUSED) {
                            item.setIcon(R.drawable.playbutton);
                            pausePlayState = PausePlay.PLAYING;
                            player.pauseSpeaking();
                        } else {
                            item.setIcon(R.drawable.pause_image);
                            pausePlayState = PausePlay.PAUSED;
                            player.resumeSpeaking();
                        }
                        return true;
                    case R.id.next_arrow_button:
                        player.stopTalking();
                        firstTimePlaying = true;
                        pageNumber++;
                        pdfView.fromUri(uri).pages(pageNumber).load();
                        return true;
                }
                return false;
            }
        });
    }

    public static class MyHandler extends Handler {

        private Reader parent;

        public MyHandler(Reader parent) {
            super();
            this.parent = parent;
        }

        @Override
        public void handleMessage(Message msg) {
            String m = (String)msg.obj;
            Log.d("OCR", m);
        }
    }


    public enum PausePlay{
        PAUSED, PLAYING;
    }
}

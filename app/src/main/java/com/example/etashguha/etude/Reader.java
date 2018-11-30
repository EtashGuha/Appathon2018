package com.example.etashguha.etude;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.PointF;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Reader extends AppCompatActivity {

    PDFView pdfView;
    PausePlay pausePlayState = PausePlay.PAUSED;
    int pageNumber = 0;
    Screenshot screenshot;
    boolean readyToDefine;
    boolean coordinatesUpdated;
    boolean coordinatesToWordUpdated;
    boolean firstTimePlaying;
    Reader.SSHandler ssHandler;
    Player player;
    BottomNavigationView bottomNavigationView;
    ProgressBar progBar;
    public HashMap<String,String> coordinatesToWord;
    public KDTree coordinates;
    TextView txt;
    Activity baseActivity;
    int yOffset;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reader);
        yOffset = getStatusBarHeight();
        readyToDefine = false;
        baseActivity = this;
        coordinatesUpdated = false;
        coordinatesToWordUpdated = false;
        player = new Player("");
        final Uri uri = getIntent().getData();
        pdfView = findViewById(R.id.pdfView);
        progBar = findViewById(R.id.progressBar);
        progBar.setVisibility(View.INVISIBLE);
        firstTimePlaying = true;
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        pdfView.fromUri(uri).pages(pageNumber).load();
        txt = findViewById(R.id.textView);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.back_button:
                        if(pageNumber != 0){
                            pageNumber--;
                            pdfView.fromUri(uri).pages(pageNumber).load();
                        }
                        sideButtonReset();
                        return true;
                    case R.id.play_pause_button:
                        if(pausePlayState == PausePlay.PAUSED && firstTimePlaying){
                            progBar.setVisibility(View.VISIBLE);
                            item.setEnabled(false);
                            item.setIcon(R.drawable.pause_image);
                            firstTimePlaying = false;
                            pausePlayState = PausePlay.PLAYING;
                            ssHandler = new SSHandler(Purpose.PLAY);
                            screenshot = new Screenshot(baseActivity, ssHandler, pageNumber);
                            screenshot.run();
                        } else if(pausePlayState == PausePlay.PLAYING) {
                            item.setIcon(R.drawable.playbutton);
                            pausePlayState = PausePlay.PAUSED;
                            player.pauseSpeaking();
                        } else {
                            item.setIcon(R.drawable.pause_image);
                            pausePlayState = PausePlay.PLAYING;
                            player.resumeSpeaking();
                        }
                        return true;
                    case R.id.next_arrow_button:
                        pageNumber++;
                        pdfView.fromUri(uri).pages(pageNumber).load();
                        sideButtonReset();
                }
                return false;
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(ev.getAction() == MotionEvent.ACTION_DOWN && readyToDefine) {
            KDNode nearest = coordinates.find_nearest(new double[]{ev.getRawX(), ev.getRawY() - getStatusBarHeight()});
            String key = (int)nearest.x[0] + " " + (int)nearest.x[1];
            txt.setText(coordinatesToWord.get(key));
        }
        return super.dispatchTouchEvent(ev);
    }

    public void sideButtonReset(){
        player.stopTalking();
        bottomNavigationView.getMenu().findItem(R.id.play_pause_button).setIcon(R.drawable.playbutton);
        firstTimePlaying = true;
        pausePlayState = PausePlay.PAUSED;
        progBar.setVisibility(View.INVISIBLE);
        bottomNavigationView.getMenu().findItem(R.id.play_pause_button).setEnabled(true);
    }

    public enum PausePlay{
        PAUSED, PLAYING
    }

    public enum Purpose{
        DEFINE, PLAY;
    }

    public void createPlayer(String outputstring){
        player = new Player(outputstring);
        player.startSpeaking();
    }

    public class SSHandler extends Handler {

        Reader.OCRHandler ocrHandler;
        OCR ocr;
        public SSHandler(Purpose purpose){
            super();
            ocrHandler = new Reader.OCRHandler(purpose);
        }

        @Override
        public void handleMessage(Message msg){
            if(msg.what == pageNumber) {
                ocr = new OCR(ocrHandler, (Bitmap) msg.obj, msg.what);
                ocr.start();
            }
        }
    }

    public class OCRHandler extends Handler {

        ReadyTTSHandler readyTTSHandler;
        MapScreenHandler mapScreenHandler;
        Purpose purpose;

        public OCRHandler(Purpose purpose) {
            super();
            this.purpose = purpose;
            readyTTSHandler = new ReadyTTSHandler();
            mapScreenHandler = new MapScreenHandler();
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == pageNumber) {
                switch (purpose) {
                    case PLAY:
                        ReadyTTS readyTTS = new ReadyTTS((FirebaseVisionDocumentText) msg.obj, readyTTSHandler, msg.what);
                        readyTTS.start();
                        break;
                    case DEFINE:
                        MapScreen mapScreen = new MapScreen((FirebaseVisionDocumentText) msg.obj, mapScreenHandler, msg.what);
                        mapScreen.start();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public class ReadyTTSHandler extends Handler{

        TTSHandler ttsHandler;

        public ReadyTTSHandler(){
            super();
            ttsHandler = new TTSHandler();
        }

        @Override
        public void handleMessage(Message msg) {
            if(msg.what == pageNumber){
                TTS tts = new TTS(ttsHandler, (String)msg.obj, msg.what);
                tts.start();
            }
        }
    }

    public class TTSHandler extends Handler{

        public TTSHandler(){
            super();
        }

        @Override
        public void handleMessage(Message msg){
            if(msg.what == pageNumber) {
                progBar.setVisibility(View.INVISIBLE);
                bottomNavigationView.getMenu().findItem(R.id.play_pause_button).setEnabled(true);
                createPlayer((String)msg.obj);
            }
        }
    }

    public class MapScreenHandler extends Handler{

        public MapScreenHandler(){
            super();
        }

        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0){
                coordinates = (KDTree)msg.obj;
                coordinatesUpdated = true;
            } else {
                coordinatesToWord = (HashMap<String,String>)(msg.obj);
                coordinatesToWordUpdated = true;
            }

            if(coordinatesUpdated && coordinatesToWordUpdated){
                readyToDefine = true;
            }
        }
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}

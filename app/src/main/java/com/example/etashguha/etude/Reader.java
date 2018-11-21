package com.example.etashguha.etude;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.barteksc.pdfviewer.PDFView;


public class Reader extends AppCompatActivity {

    PDFView pdfView;
    PausePlay pausePlayState;
    int pageNumber = 0;
    Dictionary dictionary;
    Screenshot screenshot;
    boolean firstTimePlaying;
    SSHandler ssHandler;
    DictionaryHandler dictionaryHandler;
    Player player;
    BottomNavigationView bottomNavigationView;
    ProgressBar progBar;
    Activity baseActivity;
    TextView definition;
    BottomSheetBehavior behavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reader);

        pdfView = findViewById(R.id.pdfView);
        progBar = findViewById(R.id.progressBar);
        definition = findViewById(R.id.definition);
        progBar.setVisibility(View.INVISIBLE);
        CoordinatorLayout coordinatorLayout = findViewById(R.id.main_content);
        View bottomSheet = coordinatorLayout.findViewById(R.id.bottom_sheet);

        baseActivity = this;
        player = new Player("");
        final Uri uri = getIntent().getData();
        firstTimePlaying = true;
        dictionaryHandler = new DictionaryHandler();
        pausePlayState = PausePlay.PAUSED;
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

         behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // React to state change
                Log.e("onStateChanged", "onStateChanged:" + newState);
            }
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // React to dragging events
                Log.e("onSlide", "onSlide");
            }
        });

        behavior.setPeekHeight(0);
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        pdfView.fromUri(uri).pages(pageNumber).load();
        dictionaryHandler = new DictionaryHandler();
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
                            ssHandler = new SSHandler();
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
                        dictionary = new Dictionary(baseActivity, dictionaryHandler, "Chicken");
                        dictionary.run();
                }
                return false;
            }
        });
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

    public void createPlayer(String readyForMediaPlayer){
        player = new Player(readyForMediaPlayer);
        player.startSpeaking();
    }

    public class SSHandler extends Handler {

        Reader.OCRHandler ocrHandler;
        OCR ocr;

        public SSHandler(){
            super();
            ocrHandler = new Reader.OCRHandler();
        }

        @Override
        public void handleMessage(Message msg){
            if(msg.what == pageNumber) {
                ocr = new OCR(ocrHandler, (String) msg.obj, msg.what);
                ocr.start();
            }
        }
    }

    public class OCRHandler extends Handler{

        TTS tts;
        Reader.TTSHandler ttsHandler;

        public OCRHandler() {
            super();
            ttsHandler = new Reader.TTSHandler();
        }

        @Override
        public void handleMessage(Message msg) {
            if(msg.what == pageNumber) {
                String ocrOutput = (String) msg.obj;
                tts = new TTS(ttsHandler, ocrOutput, msg.what);
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
                createPlayer((String) msg.obj);
            }
        }
    }

    public class DictionaryHandler extends Handler{

        public DictionaryHandler(){
            super();
        }

        @Override
        public void handleMessage(Message msg) {
            String response = (String)msg.obj;
            definition.setText(response.substring(response.indexOf("\"definition\":\"") + 14, response.indexOf("\",\"partOfSpeech")));

            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }
}

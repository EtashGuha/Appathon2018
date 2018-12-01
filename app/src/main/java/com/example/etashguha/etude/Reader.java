package com.example.etashguha.etude;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText;

import java.util.HashMap;

public class Reader extends AppCompatActivity {

    double x,y;
    boolean coordinatesUpdated, coordinatesToWordUpdated, firstTimePlaying, timeToDefine;
    public HashMap<String,String> coordinatesToWord;
    public KDTree coordinates;
    PDFView pdfView;
    PausePlay pausePlayState = PausePlay.PAUSED;
    Screenshot screenshot;
    Reader.SSHandler ssHandler;
    int pageNumber;
    DictionaryHandler dictionaryHandler;
    Player player;
    BottomNavigationView bottomNavigationView;
    ProgressBar progBar;
    FloatingActionButton defineWord;
    Activity baseActivity;
    TextView definition;
    ConstraintLayout baseLayout;
    BottomSheetBehavior behavior;
    CoordinatorLayout coordinatorLayout;
    View bottomSheet;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reader);


        pageNumber = 0;
        baseActivity = this;
        coordinatesUpdated = false;
        coordinatesToWordUpdated = false;
        timeToDefine = false;
        player = new Player("");

        pdfView = findViewById(R.id.pdfView);
        defineWord = findViewById(R.id.defineButton);
        progBar = findViewById(R.id.progressBar);
        definition = findViewById(R.id.definition);
        progBar.setVisibility(View.INVISIBLE);
        coordinatorLayout = findViewById(R.id.main_content);
        bottomSheet = coordinatorLayout.findViewById(R.id.bottom_sheet);
        baseLayout = findViewById(R.id.container);

        baseActivity = this;
        player = new Player("");
        firstTimePlaying = true;

        final Uri uri = getIntent().getData();

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

        baseLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("coordinates", event.getX() + " " + event.getY());
                return false;
            }
        });

        behavior.setPeekHeight(0);
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        pdfView.fromUri(uri).pages(pageNumber).load();

        defineWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeToDefine = true;
            }
        });

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
        if(ev.getRawY() < bottomNavigationView.getY() && timeToDefine && (ev.getRawY() < defineWord.getY() || ev.getRawX() < defineWord.getX())){
            timeToDefine = false;
            y = ev.getRawY() - getStatusBarHeight();
            x = ev.getRawX();
            ssHandler = new SSHandler(Purpose.DEFINE);
            screenshot = new Screenshot(baseActivity, ssHandler, pageNumber);
            screenshot.run();
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

    public void createPlayer(String outputString){
        player = new Player(outputString);
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
        CoordinatesHandler coordinatesHandler;
        CoordinatesToWordHandler coordinatesToWordHandler;
        Purpose purpose;

        public OCRHandler(Purpose purpose) {
            super();
            this.purpose = purpose;
            readyTTSHandler = new ReadyTTSHandler();
            coordinatesHandler = new CoordinatesHandler();
            coordinatesToWordHandler = new CoordinatesToWordHandler();
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
                        MapScreen mapScreen = new MapScreen((FirebaseVisionDocumentText) msg.obj, coordinatesHandler, coordinatesToWordHandler, msg.what);
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


    public class CoordinatesToWordHandler extends Handler {

        public CoordinatesToWordHandler() {
            super();
        }

        @Override
        public void handleMessage(Message msg) {
            coordinatesToWord = (HashMap<String, String>) (msg.obj);
            coordinatesToWordUpdated = true;

            if (coordinatesUpdated && coordinatesToWordUpdated) {
                findWord();
            }
        }
    }


    public class CoordinatesHandler extends Handler{

        public CoordinatesHandler(){
            super();
        }

        @Override
        public void handleMessage(Message msg) {
            coordinates = (KDTree)msg.obj;
            coordinatesUpdated = true;

            if (coordinatesUpdated && coordinatesToWordUpdated) {
                findWord();
            }
        }
    }

    public void findWord(){
        KDNode nearest = coordinates.find_nearest(new double[]{x, y});
        String key = (int) nearest.x[0] + " " + (int) nearest.x[1];
        Dictionary dictionary = new Dictionary(baseActivity, dictionaryHandler,coordinatesToWord.get(key));
        dictionary.start();
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public class DictionaryHandler extends Handler{

        public DictionaryHandler() {
            super();
        }
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            String response = (String)msg.obj;
            String word = response.substring(response.indexOf("\"word\":\"") + 8 , response.indexOf("\"definition\":\"") - 18);
            String definitionString = response.substring(response.indexOf("\"definition\":\"") + 14, response.indexOf("\",\"partOfSpeech"));
            definition.setText(capitalize(word) + " - " + definitionString);

            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    public String capitalize(String str){
        String cap = str.substring(0, 1).toUpperCase() + str.substring(1);
        return cap;
    }
}

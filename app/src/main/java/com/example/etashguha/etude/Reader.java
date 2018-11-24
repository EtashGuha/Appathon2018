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
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;

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
    boolean firstTimePlaying;
    Reader.SSHandler ssHandler;
    Player player;
    BottomNavigationView bottomNavigationView;
    ProgressBar progBar;
    HashMap<String,String> coodinatesToWord;
    KDTree coordinates;
    Activity baseActivity;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reader);

        baseActivity = this;
        player = new Player("");
        final Uri uri = getIntent().getData();
        pdfView = findViewById(R.id.pdfView);
        progBar = findViewById(R.id.progressBar);
        progBar.setVisibility(View.INVISIBLE);
        firstTimePlaying = true;
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        pdfView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int x = (int)event.getX();
                int y = (int)event.getY();
                double [] coordinate = new double [2];
                coordinate[0] = x;
                coordinate[1] = y;
                KDNode nearestWord = coordinates.find_nearest(coordinate);
                String key = (int)nearestWord.x[0] + " " + (int)nearestWord.x[1];
                return false;
            }
        });
        pdfView.fromUri(uri).pages(pageNumber).load();
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

    public void createPlayer(String outputstring){
        player = new Player(outputstring);
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
                ocr = new OCR(ocrHandler, (Bitmap) msg.obj, msg.what);
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
                coodinatesToWord = new HashMap<>();
                List<FirebaseVisionDocumentText.Block> blocks = ((FirebaseVisionDocumentText)msg.obj).getBlocks();
                ArrayList<FirebaseVisionDocumentText.Word> words = new ArrayList<FirebaseVisionDocumentText.Word>();
                ArrayList<Integer> xValues = new ArrayList<>();
                ArrayList<Integer> yValues = new ArrayList<>();
                int maxHeight = 0;
                FirebaseVisionDocumentText.Block maxBlock = null;
                for(FirebaseVisionDocumentText.Block block: blocks) {
                    if (block.getBoundingBox().height() > maxHeight) {
                        maxBlock = block;
                        maxHeight = block.getBoundingBox().height();
                    }
                }
                String outputstring = maxBlock.getText();
                tts = new TTS(ttsHandler, outputstring, msg.what);
                tts.start();

                for(int blockIndex = 0; blockIndex < blocks.size(); blockIndex++){
                    for(int paragraphIndex = 0; paragraphIndex < blocks.get(blockIndex).getParagraphs().size(); paragraphIndex++){
                        for(int wordIndex = 0; wordIndex < blocks.get(blockIndex).getParagraphs().get(paragraphIndex).getWords().size(); wordIndex++){
                            xValues.add(blocks.get(blockIndex).getParagraphs().get(paragraphIndex).getWords().get(wordIndex).getBoundingBox().centerX());
                            yValues.add(blocks.get(blockIndex).getParagraphs().get(paragraphIndex).getWords().get(wordIndex).getBoundingBox().centerY());
                            words.add(blocks.get(blockIndex).getParagraphs().get(paragraphIndex).getWords().get(wordIndex));
                        }
                    }
                }
                coordinates = new KDTree(words.size());
                for(int i = 0; i < words.size(); i++){
                    double [] coordinate = new double[2];
                    coordinate[0] = xValues.get(i);
                    coordinate[1] = yValues.get(i);
                    String key = xValues.get(i) + " " + yValues.get(i);
                    coodinatesToWord.put(key, words.get(i).getText());
                    coordinates.add(coordinate);
                    Log.d("banana", key + " " + words.get(i).getText());
                }

                progBar.setVisibility(View.INVISIBLE);
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
}

package com.example.etashguha.etude;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import com.github.barteksc.pdfviewer.PDFView;


public class Reader extends AppCompatActivity {

    PDFView pdfView;
    PausePlay pausePlayState = PausePlay.PLAYING;
    int pageNumber = 0;
    Screenshot screenshot;
    boolean firstTimePlaying;
    Reader.SSHandler ssHandler;
    Player player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reader);

        ssHandler = new SSHandler();
        screenshot = new Screenshot(this, ssHandler);
        final Uri uri = getIntent().getData();
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
                        if(pausePlayState == PausePlay.PLAYING && firstTimePlaying){
                            item.setIcon(R.drawable.pause_image);
                            firstTimePlaying = false;
                            pausePlayState = PausePlay.PAUSED;
                            screenshot.run();
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
            ocr = new OCR(ocrHandler, (String)msg.obj);
            ocr.start();
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
            String ocrOutput = (String)msg.obj;
            tts = new TTS(ttsHandler, ocrOutput);
            tts.start();
        }
    }

    public class TTSHandler extends Handler{

        public TTSHandler(){
            super();
        }

        @Override
        public void handleMessage(Message msg){
            createPlayer((String)msg.obj);
        }
    }

}

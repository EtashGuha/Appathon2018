package com.example.etashguha.etude;

import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.barteksc.pdfviewer.PDFView;

public class Reader extends AppCompatActivity {

    private TextView mTextMessage;
    PDFView pdfView;
    PausePlay pausePlayState = PausePlay.PLAYING;
    int pageNumber = 0;
    Screenshot screenshot;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reader);

        screenshot = new Screenshot(this);

        final Uri uri = getIntent().getData();

        pdfView = findViewById(R.id.pdfView);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        pdfView.fromUri(uri).pages(pageNumber).load();
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.back_button:
                        if(pageNumber != 0){
                            pageNumber--;
                            pdfView.fromUri(uri).pages(pageNumber).load();
                        }
                        return true;
                    case R.id.play_pause_button:
                        if(pausePlayState == PausePlay.PLAYING){
                            item.setIcon(R.drawable.pause_image);
                            pausePlayState = PausePlay.PAUSED;
                        } else {
                            item.setIcon(R.drawable.playbutton);
                            pausePlayState = PausePlay.PLAYING;
                        }
                        String s = screenshot.getBase64String();
                        return true;
                    case R.id.next_arrow_button:
                        pageNumber++;
                        pdfView.fromUri(uri).pages(pageNumber).load();
                        return true;
                }
                return false;
            }
        });
    }

    public enum PausePlay{
        PAUSED, PLAYING;
    }

}

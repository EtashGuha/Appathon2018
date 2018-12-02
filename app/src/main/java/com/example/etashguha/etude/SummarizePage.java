package com.example.etashguha.etude;

import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;

public class SummarizePage extends Activity {

    TextView text;
    String textToSummarize;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.summarize_page);

        text = findViewById(R.id.summarizedText);

        if(savedInstanceState == null){
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                textToSummarize = null;
            } else {
                textToSummarize = extras.getString("text");
            }
        } else {
            textToSummarize = (String) savedInstanceState.getSerializable("text");
        }
        TextSummarizer summarizer = new TextSummarizer();
        textToSummarize = textToSummarize.replaceAll("\\n", " ");
        String newS = summarizer.Summarize(textToSummarize, 5);
        text.setText(newS);
    }

}

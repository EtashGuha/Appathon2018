package com.example.etashguha.etude;

import android.os.Message;

import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapScreen extends Thread {

    FirebaseVisionDocumentText text;
    Reader.MapScreenHandler mapScreenHandler;
    int pageNumber;

    public MapScreen(FirebaseVisionDocumentText text, Reader.MapScreenHandler mapScreenHandler, int pageNumber){
        this.text = text;
        this.mapScreenHandler = mapScreenHandler;
        this.pageNumber = pageNumber;
    }

    @Override
    public void run() {
        Message msg = new Message();
        ArrayList<FirebaseVisionDocumentText.Word> words = new ArrayList<>();
        ArrayList<Integer> xValues = new ArrayList<>();
        ArrayList<Integer> yValues = new ArrayList<>();
        List<FirebaseVisionDocumentText.Block> blocks = text.getBlocks();
        for(int blockIndex = 0; blockIndex < blocks.size(); blockIndex++){
            for(int paragraphIndex = 0; paragraphIndex < blocks.get(blockIndex).getParagraphs().size(); paragraphIndex++){
                for(int wordIndex = 0; wordIndex < blocks.get(blockIndex).getParagraphs().get(paragraphIndex).getWords().size(); wordIndex++){
                    xValues.add(blocks.get(blockIndex).getParagraphs().get(paragraphIndex).getWords().get(wordIndex).getBoundingBox().centerX());
                    yValues.add(blocks.get(blockIndex).getParagraphs().get(paragraphIndex).getWords().get(wordIndex).getBoundingBox().centerY());
                    words.add(blocks.get(blockIndex).getParagraphs().get(paragraphIndex).getWords().get(wordIndex));
                }
            }
        }

        HashMap<String,String> coordinatesToWord = new HashMap<>();
        KDTree coordinates = new KDTree(words.size());
        for(int i = 0; i < words.size(); i++){
            double [] coordinate = new double[2];
            coordinate[0] = xValues.get(i);
            coordinate[1] = yValues.get(i);
            String key = xValues.get(i) + " " + yValues.get(i);
            coordinatesToWord.put(key, words.get(i).getText());
            coordinates.add(coordinate);
        }

        msg.obj = coordinates;
        msg.what = 0;
        mapScreenHandler.sendMessage(msg);
        msg.obj = coordinatesToWord;
        msg.what = 1;
        mapScreenHandler.sendMessage(msg);
    }
}

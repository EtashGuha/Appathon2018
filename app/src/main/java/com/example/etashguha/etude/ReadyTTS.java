package com.example.etashguha.etude;


import android.os.Message;

import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText;

import java.util.List;

public class ReadyTTS extends Thread {

    FirebaseVisionDocumentText text;
    Reader.ReadyTTSHandler readyTTSHandler;
    int pageNumber;

    public ReadyTTS(FirebaseVisionDocumentText text, Reader.ReadyTTSHandler readyTTSHandler, int pageNumber){
        this.text = text;
        this.readyTTSHandler = readyTTSHandler;
        this.pageNumber = pageNumber;
    }

    @Override
    public void run() {
        Message msg = new Message();
        List<FirebaseVisionDocumentText.Block> blocks = text.getBlocks();
        int maxHeight = 0;
        FirebaseVisionDocumentText.Block maxBlock = null;
        for(FirebaseVisionDocumentText.Block block: blocks) {
            if (block.getBoundingBox().height() > maxHeight) {
                maxBlock = block;
                maxHeight = block.getBoundingBox().height();
            }
        }
        msg.obj = maxBlock.getText();
        msg.what = pageNumber;
        readyTTSHandler.sendMessage(msg);
    }
}

package com.example.etashguha.etude;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Message;
import android.os.StrictMode;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentTextRecognizer;
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static android.os.StrictMode.setThreadPolicy;

public class OCR extends Thread{

    public static Reader.OCRHandler ocrHandler;
    Bitmap encodedImage;
    int pageNumber;

    public OCR(Reader.OCRHandler ocrHandler, Bitmap encodedImage, int pageNumber){
        super();
        OCR.ocrHandler = ocrHandler;
        this.encodedImage = encodedImage;
        this.pageNumber = pageNumber;
    }

    private void detectTextFire()  {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(encodedImage);
        FirebaseVisionDocumentTextRecognizer textRecognizer = FirebaseVision.getInstance().getCloudDocumentTextRecognizer();
        textRecognizer.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionDocumentText>() {
            @Override
            public void onSuccess(FirebaseVisionDocumentText firebaseVisionDocumentText) {
                processText(firebaseVisionDocumentText); //dont worry about rest of this method. if it succeeds, it will call this method
            }
        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                e.printStackTrace();
                            }
                        });
    }
    //here we can process the text however we want!!!
    private void processText(FirebaseVisionDocumentText text) {
        //this method is currently setup to find the tallest block of detected text and read that block
        //this is where we can do all our manipulations. To start on the highlighting stuff, the key
        //method to use is getBoundingBox() on a block, line, or word, however we want to do it.
        //that returns a rectangle that you could just fill or something.
//        List<FirebaseVisionDocumentText.Block> blocks = text.getBlocks();
//        int maxHeight = 0;
//        FirebaseVisionDocumentText.Block maxBlock = null;
//        for(FirebaseVisionDocumentText.Block block: blocks) {
//            if (block.getBoundingBox().height() > maxHeight) {
//                maxBlock = block;
//                maxHeight = block.getBoundingBox().height();
//            }
        //}
        //here we get the text from the block (could have also been paragraph, line, or word) and set
        //it as a message and send it to the TTS
        //String outputstring = maxBlock.getText();
        Message msg = new Message();
        msg.what = pageNumber;

        msg.obj = text;
        ocrHandler.sendMessage(msg);
    }
    @Override
    public void run(){
        detectTextFire(); //this method currently uses the document online feature, not local text recognition
    }

}

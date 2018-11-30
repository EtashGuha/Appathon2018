package com.example.etashguha.etude;

import android.graphics.Bitmap;
import android.os.Message;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentTextRecognizer;


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

    private void processText(FirebaseVisionDocumentText text) {
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

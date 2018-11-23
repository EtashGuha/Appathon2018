package com.example.etashguha.etude;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Message;
import android.util.Base64;
import android.view.View;
import java.io.ByteArrayOutputStream;

public class Screenshot extends Thread {

    Activity activity;
    Reader.SSHandler ssHandler;
    Message msg;
    int pageNumber;

    public Screenshot(Activity activity, Reader.SSHandler ssHandler, int pageNumber){
        this.activity = activity;
        this.ssHandler = ssHandler;
        this.pageNumber = pageNumber;
    }

    private static Bitmap takeScreenShot(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap b1 = view.getDrawingCache();
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;

        //Find the screen dimensions to create bitmap in the same size.
        int width = activity.getWindowManager().getDefaultDisplay().getWidth();
        int height = activity.getWindowManager().getDefaultDisplay().getHeight();
        Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height - statusBarHeight);
        view.destroyDrawingCache();
        return b;
    }
//    private String bitmapToString(Bitmap bitmap){
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
//        byte[] byteArray = byteArrayOutputStream .toByteArray();
//        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
//        return encoded;
//    }
//
//    private byte[] bitmapToByteArray (Bitmap bitmap) {
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
//        byte[] byteArray = byteArrayOutputStream .toByteArray();
//        return byteArray;
//    }

    @Override
    public void run(){
        msg = new Message();
        Bitmap b = takeScreenShot(activity);
        msg.obj = b;
        msg.what = pageNumber;
        ssHandler.sendMessage(msg); //passes the bitmap to the reader
        return;
    }
}

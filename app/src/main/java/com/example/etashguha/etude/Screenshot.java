package com.example.etashguha.etude;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Message;
import android.util.Base64;
import android.view.View;
import java.io.ByteArrayOutputStream;

public class Screenshot extends Thread {

    Activity activity;
    Reader.SSHandler ssHandler;
    Message msg;

    public Screenshot(Activity activity, Reader.SSHandler ssHandler){
        this.activity = activity;
        this.ssHandler = ssHandler;
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

    private String bitmapToString(Bitmap bitmap){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return encoded;
    }

    @Override
    public void run(){
        msg = new Message();
        msg.obj = bitmapToString(takeScreenShot(activity));
        ssHandler.sendMessage(msg);
        return;
    }



}

package com.example.sima.imgblurexplpowerm;

import android.graphics.Bitmap;
import android.util.Log;

import java.net.URL;

/**
 * Created by daehyeok on 2016. 3. 22..
 */
class Explicit extends Thread {

    private MainActivity mainActivity;

    public Explicit(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void run() {
        try {
            mainActivity.bmpArray = new Bitmap[mainActivity.numThreads];
            mainActivity.startTime = System.currentTimeMillis();

            JobHandler hndlr = new JobHandler(mainActivity);
            hndlr.start();
            hndlr.join();

        } catch (Exception e) {
            Log.e("IBench", "exception", e);
        }

        mainActivity.duration = System.currentTimeMillis() - mainActivity.startTime;
        mainActivity.view.setText(String.valueOf(mainActivity.duration));
    }
}

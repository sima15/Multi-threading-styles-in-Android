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
        Log.i("INFO", String.format("Start Test with Explicit Style with Thrad %d ", mainActivity.numThreads));
        URL startMonsoon = null;


        try {
            mainActivity.bmpArray = new Bitmap[mainActivity.numThreads];
            mainActivity.startTime = System.currentTimeMillis();

            JobHandler hndlr = new JobHandler(mainActivity);
            hndlr.start();
            hndlr.join();

        } catch (Exception e) {
            e.printStackTrace();
        }

        mainActivity.duration = System.currentTimeMillis() - mainActivity.startTime;
        mainActivity.view.setText(String.valueOf(mainActivity.duration));

        Log.i("INFO", "Test finished");
    }
}

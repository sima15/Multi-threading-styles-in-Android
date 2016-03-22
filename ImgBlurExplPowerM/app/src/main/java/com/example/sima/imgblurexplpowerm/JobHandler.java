package com.example.sima.imgblurexplpowerm;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Environment;
import android.util.Log;

/**
 * Created by daehyeok on 2016. 3. 22..
 */
public class JobHandler extends Thread {
    private MainActivity mainActivity;

    public JobHandler(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void run() {
        Log.i("INFO", "No of threads: " + mainActivity.numThreads);

        for (int j = 1; j <= 35; j++) {
            mainActivity.splitImage();
            mainActivity.pool = new Worker[mainActivity.numThreads];

            Worker.mainActivity = (mainActivity);

            for (int i = 0; i < mainActivity.numThreads; i++) {
                mainActivity.pool[i] = new Worker(mainActivity.pieceWidth, mainActivity.h, i, mainActivity.bmpArray[i]);
                mainActivity.pool[i].start();
            }

            synchronized (mainActivity.lock1) {
                while (!mainActivity.checkDone()) {
                    try {
                        mainActivity.lock1.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            mainActivity.placeholder = mainActivity.createPlaceholder();
            mainActivity.canvas = new Canvas(mainActivity.placeholder);
            for (int i = 0; i < mainActivity.numThreads; i++) {
                mainActivity.copyPartToPlaceholder(mainActivity.bmpArray[i], i);
            }
        }
//            view.setText(String.valueOf(duration));
    }
}



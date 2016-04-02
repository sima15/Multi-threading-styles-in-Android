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
    Worker[] pool;

    public JobHandler(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void run() {
        mainActivity.splitImage();
        pool = new Worker[mainActivity.numThreads];


        for (int i = 0; i < mainActivity.numThreads; i++) {
            pool[i] = new Worker(mainActivity, mainActivity.pieceWidth, mainActivity.h, i, mainActivity.bmpArray[i]);
            pool[i].start();
        }

        synchronized (mainActivity.lock1) {
            while (!checkDone()) {
                try {
                    mainActivity.lock1.wait();
                } catch (InterruptedException e) {
                    Log.e("IBench", "exception", e);
                }
            }
        }
        mainActivity.placeholder = mainActivity.createPlaceholder();
        mainActivity.canvas = new Canvas(mainActivity.placeholder);
        for (int i = 0; i < mainActivity.numThreads; i++) {
            mainActivity.copyPartToPlaceholder(mainActivity.bmpArray[i], i);
        }

//            view.setText(String.valueOf(duration));
    }

    boolean checkDone() {
        for (Worker a : pool) {
            if (!a.bluer.done) return false;
        }
        return true;
    }
}



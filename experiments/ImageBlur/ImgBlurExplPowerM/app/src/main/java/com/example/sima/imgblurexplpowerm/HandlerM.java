package com.example.sima.imgblurexplpowerm;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

/**
 * Created by daehyeok on 2016. 3. 23..
 */
public class HandlerM implements Runnable {

    Handler handler = new Handler();
    MainActivity mainActivity;
    Worker[] pool;
    HandlerM(MainActivity mainActivity, Handler handler) {
        super();
        this.mainActivity = mainActivity;
        this.handler = handler;
    }



    @Override
    public void run() {

        mainActivity.bmpArray = new Bitmap[mainActivity.numThreads];
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
    }



    boolean checkDone() {
        for (Worker a : pool) {
            if (!a.bluer.done) return false;
        }
        return true;
    }
}
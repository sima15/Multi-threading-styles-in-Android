package com.example.sima.imgblurexplpowerm;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by daehyeok on 2016. 3. 22..
 */
class ExecutorBlur implements Runnable {
    ExecutorService pool;
    MainActivity mainActivity;
    int numThreads;

    ExecutorBlur(MainActivity mainActivity, ExecutorService pool) {
        super();
        this.mainActivity = mainActivity;
        this.pool = pool;
        this.numThreads = mainActivity.numThreads;
    }

    @Override
    public void run() {
        mainActivity.bmpArray = new Bitmap[mainActivity.numThreads];
        mainActivity.splitImage();

        for (int j = 0; j < numThreads; j++) {
            pool.submit(new Worker(mainActivity, mainActivity.pieceWidth, mainActivity.h, j, mainActivity.bmpArray[j]));
        }

        mainActivity.placeholder = mainActivity.createPlaceholder();
        mainActivity.canvas = new Canvas(mainActivity.placeholder);
        pool.shutdown();
        try {
            pool.awaitTermination(500, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e("IBench", "exception", e);
        }
        for (int j = 0; j < numThreads; j++) {
            mainActivity.copyPartToPlaceholder(mainActivity.bmpArray[j], j);
        }

//            pool.shutdown();

    }

}
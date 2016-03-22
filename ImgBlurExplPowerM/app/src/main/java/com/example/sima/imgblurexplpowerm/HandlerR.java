package com.example.sima.imgblurexplpowerm;

import android.graphics.Canvas;

/**
 * Created by daehyeok on 2016. 3. 22..
 */
public class HandlerR implements Runnable {
    MainActivity mainActivity;
    Worker[] pool;

    HandlerR(MainActivity mainActivity) {
        super();
        this.mainActivity = mainActivity;
    }

    @Override
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

    boolean checkDone() {
        for (Worker a : pool) {
            if (!a.done) return false;
        }
        return true;
    }
}
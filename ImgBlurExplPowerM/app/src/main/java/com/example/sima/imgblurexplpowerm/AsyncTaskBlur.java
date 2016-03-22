package com.example.sima.imgblurexplpowerm;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;

/**
 * Created by daehyeok on 2016. 3. 22..
 */
class AsyncTaskBlur extends AsyncTask<Void, Void, Void> {


    public MainActivity mainActivity;
    Worker[] pool;

    AsyncTaskBlur(MainActivity mainActivity) {
        super();
        this.mainActivity = mainActivity;
    }

    public void copyPartToPlaceholder(Bitmap smallBitmap, int index) {
        mainActivity.canvas.drawBitmap(smallBitmap, index * MainActivity.pieceWidth, 0, null);
    }


    @Override
    protected Void doInBackground(Void... v) {
        mainActivity.bmpArray = new Bitmap[mainActivity.numThreads];
        mainActivity.splitImage();
        pool = new Worker[mainActivity.numThreads];
        for (int j = 0; j < mainActivity.numThreads; j++) {
            pool[j] = new Worker(mainActivity, mainActivity.pieceWidth, mainActivity.h, j, mainActivity.bmpArray[j]);
            pool[j].start();
        }

        return null;
    }


    @Override
    protected void onPostExecute(Void v) {
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

        for (int j = 0; j < mainActivity.numThreads; j++) {
            copyPartToPlaceholder(mainActivity.bmpArray[j], j);
        }

    }

    boolean checkDone() {
        for (Worker a : pool) {
            if (!a.done) return false;
        }
        return true;
    }

}
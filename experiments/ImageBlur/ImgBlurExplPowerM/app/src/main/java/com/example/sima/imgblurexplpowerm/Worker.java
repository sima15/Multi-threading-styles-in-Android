package com.example.sima.imgblurexplpowerm;

import android.os.Handler;

/**
 * Created by daehyeok on 2016. 3. 22..
 */
public class Worker extends Thread {
    //static public Bitmap[] bmpArray;
    Bluer bluer;
    Worker(MainActivity mainActivity,int[] src, int start, int length, int[] dst) {
        bluer = new Bluer(mainActivity,  src,  start,  length,  dst);
    }

    Worker(MainActivity mainActivity,int[] src, int start, int length, int[] dst, Handler handler) {
        bluer = new Bluer(mainActivity,  src,  start,  length,  dst);
    }

    @Override
    public void run() {
        bluer.doTask();
    }
}
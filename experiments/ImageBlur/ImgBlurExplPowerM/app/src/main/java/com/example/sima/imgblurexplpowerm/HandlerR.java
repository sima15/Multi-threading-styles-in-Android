package com.example.sima.imgblurexplpowerm;

/**
 * Created by daehyeok on 2016. 3. 22..
 */
public class HandlerR implements Runnable {
    MainActivity mainActivity;

    Bluer bluer;

    HandlerR(MainActivity mainActivity, int[] src, int start, int length, int[] dst) {
        super();
        bluer = new Bluer(mainActivity,  src,  start,  length,  dst);
    }


    @Override
    public void run() {
        bluer.doTask();
    }
}
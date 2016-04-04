package com.example.sima.imgblurexplpowerm;

import android.os.Handler;

/**
 * Created by daehyeok on 2016. 3. 23..
 */
public class HandlerM implements Runnable {
    Handler handler = new Handler();
    MainActivity mainActivity;
    Bluer bluer;

    HandlerM(MainActivity mainActivity, int[] src, int start, int length, int[] dst, Handler han) {
        super();
        bluer = new Bluer(mainActivity,  src,  start,  length,  dst);
        handler = han;
    }



    @Override
    public void run() {
        bluer.doTask();
    }
}
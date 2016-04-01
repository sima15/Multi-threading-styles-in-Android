package com.example.sima.imgblurexplpowerm;

import android.graphics.Bitmap;

/**
 * Created by daehyeok on 2016. 3. 22..
 */
public class Worker extends Thread {
    //static public Bitmap[] bmpArray;
    Bluer bluer;
    Worker(MainActivity mainActivity, int w, int h, int index, Bitmap orgBmp) {
        bluer = new Bluer(mainActivity,  w,  h,  index,  orgBmp);
    }


    @Override
    public void run() {
        bluer.doTask();
    }
}
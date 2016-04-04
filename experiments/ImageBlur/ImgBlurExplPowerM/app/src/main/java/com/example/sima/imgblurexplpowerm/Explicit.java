package com.example.sima.imgblurexplpowerm;

/**
 * Created by daehyeok on 2016. 3. 22..
 */
class Explicit extends Thread {

    MainActivity mainActivity;
    Bluer bluer;

    Explicit(MainActivity mainActivity, int[] src, int start, int length, int[] dst){
        super();
        bluer = new Bluer(mainActivity, src, start, length, dst);
    }
    public void run() {
        bluer.doTask();
    }
}

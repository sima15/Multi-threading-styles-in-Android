package com.example.sima.imgblurexplpowerm;

import java.util.concurrent.RecursiveAction;

/**
 * Created by daehyeok on 2016. 3. 22..
 */
public class ForkBlur extends RecursiveAction {
    MainActivity mainActivity;
    Bluer bluer;

    ForkBlur(MainActivity mainActivity, int[] src, int start, int length, int[] dst){
        super();
        bluer = new Bluer(mainActivity, src, start, length, dst);
        this.mainActivity = mainActivity;
    }


    protected int sThreshold = 100000;

    @Override
    protected void compute() {
        if (bluer.mLength < sThreshold) {
            bluer.doTask();
            return;
        }

        int split = bluer.mLength / 2;

        invokeAll(new ForkBlur(mainActivity, bluer.mSource, bluer.mStart, split, bluer.mDestination),
                new ForkBlur(mainActivity, bluer.mSource, bluer.mStart + split, bluer.mLength - split,
                        bluer.mDestination));
    }
}
package com.example.sima.imgblurexplpowerm;

import android.os.AsyncTask;

/**
 * Created by daehyeok on 2016. 3. 22..
 */
class AsyncTaskBlur extends AsyncTask<Void, Void, Void> {

    MainActivity mainActivity;
    Bluer bluer;

    AsyncTaskBlur(MainActivity mainActivity, int[] src, int start, int length, int[] dst) {
        super();
        bluer = new Bluer(mainActivity,  src,  start,  length,  dst);
    }



    @Override
    protected Void doInBackground(Void... v) {
        bluer.doTask();
        return null;
    }

}
package com.example.sima.imgblurexplpowerm;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by daehyeok on 2016. 3. 22..
 */
class AsyncTaskBlur extends AsyncTask<Void, Void, Void> {


    Bluer bluer;

    AsyncTaskBlur(MainActivity mainActivity, int w, int h, int index, Bitmap orgBmp) {
        super();
        bluer = new Bluer(mainActivity,  w,  h,  index,  orgBmp);
    }



    @Override
    protected Void doInBackground(Void... v) {
        bluer.doTask();
        return null;
    }


    @Override
    protected void onPostExecute(Void v) {

    }
}
package com.example.assl_blue.dacapo_benchmark;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicInteger;

public class MandelbrotAsynctask extends MBase {

    MandelbrotAsynctask(int numThreads) {
        super(numThreads);
    }

    @Override
    protected void doJob() {
        new MandelAsyncTask().execute();
    }


    class MandelAsyncTask extends AsyncTask<Void, Void, String> {


        @Override
        protected String doInBackground(Void... params) {

            threads = new boolean[numThread];

            pool = new Thread[numThread];
            for (int i = 0; i < pool.length; i++) {
                pool[i] = new workerThread();
                pool[i].start();
            }

            for (int k = 0; k < numThread; k++) {
                try {
                    if (pool[k].isAlive()) pool[k].join();
                } catch (InterruptedException e) {
                    Log.e("Mbench", "exception", e);
                }
            }

            pool.clone();
            return "";
        }

        class workerThread extends Thread {
            int temp = 0;

            @Override
            public void run() {
                doTask();
                threads[temp] = true;

                synchronized (this) {
                    notify();
                }
            }
        }


        @Override
        protected void onPostExecute(String v) {
            //resultText.setText(String.valueOf(totalTime));
        }
    }

}


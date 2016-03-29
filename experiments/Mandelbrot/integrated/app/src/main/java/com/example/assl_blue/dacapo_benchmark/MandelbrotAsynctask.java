package com.example.assl_blue.dacapo_benchmark;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicInteger;

public class MandelbrotAsynctask extends MBase {

    MandelbrotAsynctask(int numThreads){
        super(numThreads);
    }

    @Override
    protected  void doJob(){
        Crb = new double[N + 7];
        Cib = new double[N + 7];
        double invN = 2.0 / N;
        for (int i = 0; i < N; i++) {
            Cib[i] = i * invN - 1.0;
            Crb[i] = i * invN - 1.5;
        }
        yCt = new AtomicInteger();
        out = new byte[N][(N + 7) / 8];
        new MandelAsyncTask().execute();
    }
    
    class MandelAsyncTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {

            threads = new boolean[numThread];
            for (int loopCount = 0; loopCount < 8000; loopCount++) {
                pool = new Thread[numThread];
                for (int i = 0; i < pool.length; i++) {
                    pool[i] = new Thread() {
                        int temp =0;
                        public void run() {
                            int y;
                            while ((y = yCt.getAndIncrement()) < out.length) {
                                putLine(y, out[y]);
                            }
                            threads[temp] = true;
                            if(System.currentTimeMillis()>endTime) endTime= System.currentTimeMillis();
                            synchronized (this) {
                                notify();
                            }
                        }
                    };
                }
            }
            for (int k=0; k<numThread; k++) {
                pool[k].start();
            }

            for (int k=0; k<numThread; k++) {
                try{
                    if (pool[k].isAlive()) pool[k].join();
                    System.out.println("Waiting for thread " + k + " to complete");
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }

            return String.valueOf(totalTime);
        }

        @Override
        protected void onPostExecute(String v) {
            //resultText.setText(String.valueOf(totalTime));
        }
    }

}


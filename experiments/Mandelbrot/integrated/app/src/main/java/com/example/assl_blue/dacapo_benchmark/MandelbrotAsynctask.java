package com.example.assl_blue.dacapo_benchmark;

import android.os.AsyncTask;

public class MandelbrotAsynctask extends MBase {

    MandelbrotAsynctask(int numThreads) {
        super(numThreads);
    }

    @Override
    protected void doJob() {

        MandelAsyncTask[] tasks;

        int chunk = out.length/numThread;
        tasks = new MandelAsyncTask[numThread];
        for(int i=0; i<numThread; i++){
            tasks[i] = new MandelAsyncTask(i*chunk, (i+1)*chunk);
            tasks[i].execute();
        }

        for (int k=0; k<numThread; k++) {
            synchronized (lock) {
                try {
                    while (!tasks[k].done) lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    class MandelAsyncTask extends AsyncTask<Void, Void, String> {

        boolean done = false;
        int lb;
        int ub;

        public MandelAsyncTask(int a, int b) {
            lb = a;
            ub = b;
        }

        @Override
        protected String doInBackground(Void... params) {

            doTask(lb, ub);
            done = true;

            synchronized (lock) {
                lock.notify();
            }

            return null;
        }
    }
}


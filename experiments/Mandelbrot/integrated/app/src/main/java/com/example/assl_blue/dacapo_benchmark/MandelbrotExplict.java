package com.example.assl_blue.dacapo_benchmark;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;

import java.util.concurrent.atomic.AtomicInteger;

public class MandelbrotExplict extends MBase {

    MandelbrotExplict(int numThreads){
        super(numThreads);
    }

    protected  void doJob(){

        threads = new boolean[numThread];
        pool = new Thread[numThread];
        for (int i = 0; i < pool.length; i++) {
            pool[i] = new Thread() {
                    int temp =0;
                    public void run() {
                        doTask();
                        threads[temp] = true;
                        synchronized (this) {
                            notify();
                        }
                    }

                };
        }
        for (int k=0; k<numThread; k++) {
            pool[k].start();
        }

        for (int k=0; k<numThread; k++) {
            try{
                if (pool[k].isAlive()) pool[k].join();
            }catch (InterruptedException e){
                Log.e("Mbench", "exception", e);
            }
        }

    }

}




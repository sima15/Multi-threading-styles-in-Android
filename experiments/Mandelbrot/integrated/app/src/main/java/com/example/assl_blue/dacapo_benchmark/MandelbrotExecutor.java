package com.example.assl_blue.dacapo_benchmark;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Sima Mehri
 */
public class MandelbrotExecutor extends MBase {

    MandelbrotExecutor(int numThreads){
        super(numThreads);
    }


    @Override
    protected  void doJob(){
        ExecutorService executor;

        executor = Executors.newFixedThreadPool(numThread);
        for (int i = 0; i < numThread; i++) {

            executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        doTask();
                    }
                });
        }
        executor.shutdown();
        try {
            executor.awaitTermination(500, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.e("Mbench", "exception", e);
        }
    }

}

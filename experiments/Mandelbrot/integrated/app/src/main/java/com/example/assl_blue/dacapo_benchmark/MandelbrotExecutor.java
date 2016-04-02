package com.example.assl_blue.dacapo_benchmark;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
        chunk = out.length/numThread;
        executor = Executors.newFixedThreadPool(numThread);
        for (int i = 0; i < numThread; i++) {

            final int finalI = i;
            executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        doTask((finalI *chunk), (finalI +1)*chunk);
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

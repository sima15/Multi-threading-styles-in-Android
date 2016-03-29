package com.example.assl_blue.dacapo_benchmark;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
        Crb = new double[N + 7];
        Cib = new double[N + 7];
        double invN = 2.0 / N;
        for (int i = 0; i < N; i++) {
            Cib[i] = i * invN - 1.0;
            Crb[i] = i * invN - 1.5;
        }
        yCt = new AtomicInteger();
        out = new byte[N][(N + 7) / 8];

        ExecutorService executor;

        executor = Executors.newFixedThreadPool(numThread);
        for (int i = 0; i < numThread; i++) {

            executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        int y ;
                        while ((y = yCt.getAndIncrement()) < out.length) {
                            putLine(y, out[y]);
                        }
                    }
                });
        }
        executor.shutdown();
        try {
            executor.awaitTermination(500, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

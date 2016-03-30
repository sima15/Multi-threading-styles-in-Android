package com.example.assl_blue.dacapo_benchmark;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Sima Mehri
 */

public class MandelbrotHandlerR extends MBase {

    Handler handler = new Handler();

    MandelbrotHandlerR(int numThreads){
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

        try {
            Thread t = new Thread(new Mandelbrot2());
            t.start();
            t.join();
        } catch (InterruptedException e) {
            Log.e("Mbench", "exception", e);
        }
    }

    class Mandelbrot2 implements  Runnable {

        public void run() {
            pool = new Thread[numThread];
            for (int i = 0; i < pool.length; i++) {
                pool[i] = new Thread() {
                        public void run() {
                            int y;
                            while ((y = yCt.getAndIncrement()) < out.length) {
                                putLine(y, out[y]);
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

}

package com.example.assl_blue.dacapo_benchmark;

import android.os.Handler;
import android.util.Log;

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
        chunk = out.length/numThread;
        Handler[] handlers = new Handler[numThread];
        Thread[] threads = new Thread[numThread];

        for(int i=0; i<numThread; i++){
            threads[i] = new Thread(new Mandelbrot(i*chunk, (i+1)*chunk));
            threads[i].start();
        }

        for (int k=0; k<numThread; k++) {
            try{
                if (threads[k].isAlive()) threads[k].join();
            }catch (InterruptedException e){
                Log.e("Mbench", "exception", e);
            }
        }
    }

    class Mandelbrot implements  Runnable {
        int lb;
        int ub;

        public Mandelbrot(int a, int b){
            lb = a;
            ub = b;
        }
        public void run() {
            doTask(lb, ub);
        }
    }

}

package com.example.assl_blue.dacapo_benchmark;


import android.util.Log;

public class MandelbrotExplict extends MBase {

    MandelbrotExplict(int numThreads){
        super(numThreads);
    }

    protected  void doJob(){
        chunk = out.length/numThread;
        threads = new boolean[numThread];
        pool = new Thread[numThread];
        for (int i = 0; i < pool.length; i++) {
            final int finalI = i;
            pool[i] = new Thread() {
                    int temp =0;
                    public void run() {
                        doTask((finalI *chunk), (finalI +1)*chunk);
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




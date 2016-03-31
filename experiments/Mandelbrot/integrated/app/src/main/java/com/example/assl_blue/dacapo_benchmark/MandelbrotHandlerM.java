package com.example.assl_blue.dacapo_benchmark;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Sima Mehri
 */

public class MandelbrotHandlerM extends MBase {
    final static int SET_RESULT = 0;

    Handler handler = new Handler();

    MandelbrotHandlerM(){
        super();
    }

    MandelbrotHandlerM(int numThreads){
        super(numThreads);
    }

    @Override
    protected  void doJob(){
        Thread t1 = new Thread(new MandelBrotMsgHnd(handler));
        t1.start();
        try {
            t1.join();
        } catch (InterruptedException e) {
            Log.e("Mbench", "exception", e);
        }
    }

    public class MandelBrotMsgHnd implements Runnable {
        private final Handler handler;

        MandelBrotMsgHnd(Handler handler) {
            this.handler = handler;
        }

        public void run() {
            Message msg;

            pool = new Thread[numThread];
            for (int j = 0; j < pool.length; j++) {
                pool[j] = new Thread() {
                        public void run() {
                            doTask();
                        }
                    };
            }
            for (int k=0; k<numThread; k++) {
                pool[k].start();
            }

            for (int k=0; k<numThread; k++) {
                try{
                    if (pool[k].isAlive()){
                        pool[k].join();
                    }
                }catch (InterruptedException e){
                    Log.e("Mbench", "exception", e);
                }
            }

        }
    }
}

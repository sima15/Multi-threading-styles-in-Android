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
        Crb = new double[N + 7];
        Cib = new double[N + 7];
        double invN = 2.0 / N;
        for (int i = 0; i < N; i++) {
            Cib[i] = i * invN - 1.0;
            Crb[i] = i * invN - 1.5;
        }
        yCt = new AtomicInteger();
        out = new byte[N][(N + 7) / 8];

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

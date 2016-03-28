package com.example.sima.mandelbrot.handlermessage;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Sima Mehri
 */

public class MainActivity extends AppCompatActivity {
    final static int SET_RESULT = 0;

    long startTime;
    double duration;

    Thread[] pool;
    int N= 500;
    byte[][] out;
    static AtomicInteger yCt;
    static double[] Crb;
    static double[] Cib;


    private EditText textHandler;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case SET_RESULT: {
                    textHandler.setText((String) msg.obj);
                    break;
                }
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textHandler = (EditText) findViewById(R.id.input);

        startTime = System.nanoTime();
        System.out.println("Start time is: " + startTime);

        for(int i=0; i<800; i++) doJob();

    }

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
            e.printStackTrace();
        }
    }
    static int getByte(int x, int y) {
        int res = 0;
        for (int i = 0; i < 8; i += 2) {
            double Zr1 = Crb[x + i];
            double Zi1 = Cib[y];

            double Zr2 = Crb[x + i + 1];
            double Zi2 = Cib[y];

            int b = 0;
            int j = 49;
            do {
                double nZr1 = Zr1 * Zr1 - Zi1 * Zi1 + Crb[x + i];
                double nZi1 = Zr1 * Zi1 + Zr1 * Zi1 + Cib[y];
                Zr1 = nZr1;
                Zi1 = nZi1;

                double nZr2 = Zr2 * Zr2 - Zi2 * Zi2 + Crb[x + i + 1];
                double nZi2 = Zr2 * Zi2 + Zr2 * Zi2 + Cib[y];
                Zr2 = nZr2;
                Zi2 = nZi2;

                if (Zr1 * Zr1 + Zi1 * Zi1 > 4) {
                    b |= 2;
                    if (b == 3) break;
                }
                if (Zr2 * Zr2 + Zi2 * Zi2 > 4) {
                    b |= 1;
                    if (b == 3) break;
                }
            } while (--j > 0);
            res = (res << 2) + b;
        }
        return res ^ -1;
    }
    static void putLine(int y, byte[] line) {
        for (int xb = 0; xb < line.length; xb++)
            line[xb] = (byte) getByte(xb * 8, y);
    }

    public class MandelBrotMsgHnd implements Runnable {
        private final Handler handler;

        MandelBrotMsgHnd(Handler handler) {
            this.handler = handler;
        }

        public void run() {
            Message msg;

            int poolLength =4;
                pool = new Thread[poolLength];
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
            for (int k=0; k<poolLength; k++) {
                pool[k].start();
            }

            for (int k=0; k<poolLength; k++) {
                try{
                    if (pool[k].isAlive()){
                        pool[k].join();
                    }
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }

            duration = (System.nanoTime() - startTime)/1000000.00;
            System.out.println("Duration: " + duration);

            msg = handler.obtainMessage(SET_RESULT, String.valueOf( (int) duration));
            handler.sendMessage(msg);
        }
    }
}

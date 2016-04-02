package com.example.sima.mandelbrothandlermnew;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {

    final static int SET_RESULT = 0;

    long startTime;
    double duration;
    int N= 500;
    byte[][] out;
    static AtomicInteger yCt;
    static double[] Crb;
    static double[] Cib;

    Handler handler2 = new Handler();
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
    private TextView textHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textHandler = (TextView) findViewById(R.id.resultText);

        startTime = System.nanoTime();
        System.out.println("Start time is: " + startTime);

        for(int i=0; i<80; i++)
            doJob();
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

        Thread t1 = new Thread(new MandelBrotMsgHnd(handler2));
        t1.start();
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        duration = (System.nanoTime() - startTime)/1000000.00;
        System.out.println("Duration= "+duration);
        new Thread(new Result(handler)).start();
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

                        int y;
                        while ((y = yCt.getAndIncrement()) < out.length) {
                            putLine(y, out[y]);
                        }
        }
    }

    public  class  Result implements  Runnable{
        Message message;

        public Result (Handler h){
            handler = h;
        }

        public  void run(){
            String time = String.valueOf(duration);
            message  = handler.obtainMessage(SET_RESULT, time);
            handler.sendMessage(message);
        }

    }
}

package com.example.sima.mandelbrothandlerrnew;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Sima Mehri
 */

public class MainActivity extends AppCompatActivity {

    long startTime;
    double duration;
    int N = 500;
    int numThreads = 4;
    byte[][] out;
    static AtomicInteger yCt;
    static double[] Crb;
    static double[] Cib;
    private TextView resultText;
    Handler[] handlers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultText = (TextView) findViewById(R.id.resultText);

        startTime = System.nanoTime();
        System.out.println("Start time is: " + startTime);

        for (int i = 0; i < 800; i++)
            doJob();
    }

    protected void doJob() {
        Crb = new double[N + 7];
        Cib = new double[N + 7];
        double invN = 2.0 / N;
        for (int i = 0; i < N; i++) {
            Cib[i] = i * invN - 1.0;
            Crb[i] = i * invN - 1.5;
        }
        yCt = new AtomicInteger();
        out = new byte[N][(N + 7) / 8];

        int chunk = out.length/numThreads;
        handlers = new Handler[numThreads];
        Thread[] threads = new Thread[numThreads];

        for(int i=0; i<numThreads; i++){
            threads[i] = new Thread(new Mandelbrot(i*chunk, (i+1)*chunk));
            threads[i].start();
        }

        for (int k=0; k<numThreads; k++) {
            try{
                if (threads[k].isAlive()) threads[k].join();
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }

        duration = (System.nanoTime() - startTime) / 1000000.00;
        System.out.println("Duration: " + duration);
        resultText.setText(String.valueOf(duration));
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

    class Mandelbrot implements Runnable {

        int lb;
        int ub;

        public Mandelbrot(int a, int b){
            lb = a;
            ub = b;
        }
        public void run() {

//            System.out.println("lb= "+lb + " up = "+ ub);
            int y;
            yCt.set(lb);
            while ((y = yCt.getAndIncrement()) < ub)
                putLine(y, out[y]);


//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    resultText.setText(String.valueOf((int) duration));
//                }
//            });

        }

    }
}
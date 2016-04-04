package com.example.assl_blue.dacapo_benchmark;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import java.util.concurrent.atomic.AtomicInteger;

public class Mandelbrot1 extends AppCompatActivity {

    int N = 500;
    long startTime;
    long endTime = startTime;
    long totalTime;
    int poolLength;
    EditText inputText;
    Thread[] pool;

    boolean mainThread = true;
    boolean[] threads;

    byte[][] out;
    static AtomicInteger yCt;
    static double[] Crb;
    static double[] Cib;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mandelbrot1);

            startTime = System.currentTimeMillis();
            System.out.println("Start time is: " + startTime);
            inputText = (EditText) findViewById(R.id.inputText);

            for(int i=0; i<800; i++) doJob();

            if(System.currentTimeMillis()>endTime) endTime= System.currentTimeMillis();

            if(mainThread) totalTime = endTime - startTime;
            System.out.println("end time is: "+endTime);
            System.out.println("Total time is: "+totalTime);
            inputText.setText(String.valueOf(totalTime));
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

        poolLength = 32;
        threads = new boolean[poolLength];
            pool = new Thread[poolLength];
            for (int i = 0; i < pool.length; i++) {
                pool[i] = new Thread() {
                    int temp =0;
                    public void run() {
                        int y;
                        while ((y = yCt.getAndIncrement()) < out.length) {
                            putLine(y, out[y]);
                        }
                        threads[temp] = true;
                        if(System.currentTimeMillis()>endTime) endTime= System.currentTimeMillis();
                        synchronized (this) {
                            notify();
                        }
                    }

                };
            }
        for (int k=0; k<poolLength; k++) {
            pool[k].start();
        }

        for (int k=0; k<poolLength; k++) {
            try{
                if (pool[k].isAlive()) pool[k].join();
//                System.out.println("Waiting for thread " + k + " to complete");
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }

//        System.out.print(("P4\n" + N + " " + N + "\n").getBytes());
//        for(int i=0;i<N;i++) System.out.println(out[i]);

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

}




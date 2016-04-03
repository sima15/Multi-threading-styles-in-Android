package com.example.assl_blue.dacapo_benchmark;

import android.widget.TextView;

import java.util.concurrent.atomic.AtomicInteger;

public class MBase {

    byte[][] out;
    static AtomicInteger yCt;
    static double[] Crb;
    static double[] Cib;

    boolean mainThread = true;
    boolean[] threads;
    Thread[] pool;

    int chunk;
    long startTime;
    long endTime = startTime;
    long totalTime;
    int N =2000;
    TextView resultText;
    int numThread;
    Object lock = new Object();

    MBase(int numThread){
        super();
        init();
        this.numThread= numThread;
    }

    MBase(){
        init();
    };

    void init(){
        Crb = new double[N + 7];
        Cib = new double[N + 7];
        double invN = 2.0 / N;
        for (int i = 0; i < N; i++) {
            Cib[i] = i * invN - 1.0;
            Crb[i] = i * invN - 1.5;
        }
        yCt = new AtomicInteger();
        out = new byte[N][(N + 7) / 8];
    };

    protected  void doTask(int a, int b){
        int y;
        yCt.set(a);
        while ((y = yCt.getAndIncrement()) < b) {
            putLine(y, out[y]);
        }
    }

    protected  void doJob(){};
        

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


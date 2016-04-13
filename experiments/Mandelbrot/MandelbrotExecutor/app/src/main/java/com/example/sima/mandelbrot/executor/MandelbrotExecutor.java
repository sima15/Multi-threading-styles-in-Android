package com.example.sima.mandelbrot.executor;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Sima Mehri
 */
public class MandelbrotExecutor extends AppCompatActivity {
    long startTime;
    long totalTime;
    int N = 500;
    EditText inputText;

    byte[][] out;
    static AtomicInteger yCt;
    static double[] Crb;
    static double[] Cib;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mandelbrot_executor);

        startTime = System.currentTimeMillis();
        System.out.println("start time is: " + String.valueOf(startTime));

        inputText = (EditText)findViewById(R.id.inputText);

        //To increase the running time of the application
        for (int i=0; i<1; i++) doJob();

        totalTime = System.currentTimeMillis() - startTime;
        inputText.setText(String.valueOf(totalTime));
        System.out.println("Total time is: " + totalTime);
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

        int poolLength = 8;
        ExecutorService executor;

            executor = Executors.newFixedThreadPool(poolLength);
            for (int i = 0; i < poolLength; i++) {

                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        int y ;
                        while ((y = yCt.getAndIncrement()) < out.length) {
                            putLine(y, out[y]);
                        }

                        System.out.print(("P4\n" + N + " " + N + "\n").getBytes());
                        for(int i=0;i<N;i++) System.out.println(Arrays.toString(out[i]));
                    }
                });
            }
            executor.shutdown();
            try {
                executor.awaitTermination(500, TimeUnit.SECONDS);
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
}

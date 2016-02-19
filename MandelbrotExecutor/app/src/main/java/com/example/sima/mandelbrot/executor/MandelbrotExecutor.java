package com.example.sima.mandelbrot.executor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MandelbrotExecutor extends AppCompatActivity {
    long startTime;
    long endTime;
    long totalTime;
    int N;
    EditText inputText;

    byte[][] out;
    static AtomicInteger yCt;
    static double[] Crb;
    static double[] Cib;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mandelbrot_executor);

        Button submitButton = (Button) findViewById(R.id.submitButton);
        inputText = (EditText) findViewById(R.id.inputText);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTime = System.currentTimeMillis();
                System.out.println("start time is: " + String.valueOf(startTime));

                String inputStr = inputText.getText().toString();
                N = Integer.parseInt(inputStr);

                Crb = new double[N + 7];
                Cib = new double[N + 7];
                double invN = 2.0 / N;
                for (int i = 0; i < N; i++) {
                    Cib[i] = i * invN - 1.0;
                    Crb[i] = i * invN - 1.5;
                }
                yCt = new AtomicInteger();
                out = new byte[N][(N + 7) / 8];

//                int loopCount =  (Runtime.getRuntime().availableProcessors())*16;
                int poolLength = 4;
                ExecutorService executor = null;

                //To increase the running time of the application
                for (int j=1; j<=800; j++) {
                    executor = Executors.newFixedThreadPool(poolLength);
                    for (int i = 0; i < poolLength; i++) {

                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                int y ;
//                                long innerStartTime = System.currentTimeMillis();
                                while ((y = yCt.getAndIncrement()) < out.length) {
                                    putLine(y, out[y]);
                                }
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
                if(executor.isTerminated()) {
                    endTime = System.currentTimeMillis();
                    totalTime = endTime - startTime;
                    inputText.setText(String.valueOf(totalTime));
                    System.out.println("end time is: " + endTime);
                    System.out.println("Total time is: " + totalTime);
                }
            }
        });
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

package com.example.sima.mandelbrot.asynctask;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicInteger;

public class MandelbrotAsynctask extends Activity {

    byte[][] out;
    static AtomicInteger yCt;
    static double[] Crb;
    static double[] Cib;

    boolean mainThread = true;
    boolean[] threads;
    Thread[] pool;

    long startTime;
    long endTime = startTime;
    long totalTime;
    int N;
    EditText inputText;
    TextView resultText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mandelbrot_asynctask);

        Button submitButton = (Button) findViewById(R.id.submitButton);
        inputText = (EditText) findViewById(R.id.inputText);
        resultText = (TextView) findViewById(R.id.resultText);


        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTime = System.nanoTime();
                System.out.println("start time is: " + startTime);

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


                    new MandelAsyncTask().execute();
            }
        });
    }


    class MandelAsyncTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {

            int poolLength =16;
            threads = new boolean[poolLength];
            for (int loopCount = 0; loopCount < 8000; loopCount++) {
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
            }
            for (int k=0; k<poolLength; k++) {
                pool[k].start();
            }

            for (int k=0; k<poolLength; k++) {
                try{
                    if (pool[k].isAlive()) pool[k].join();
                    System.out.println("Waiting for thread " + k + " to complete");
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }

            System.out.print(("P4\n" + N + " " + N + "\n").getBytes());
            for(int i=0;i<N;i++) System.out.println(out[i]);


            if(System.nanoTime()>endTime) endTime= System.nanoTime();

            if(mainThread) totalTime = (endTime - startTime)/1000000;
            System.out.println("end time is: "+endTime);
            System.out.println("Total time is: "+totalTime);
            return String.valueOf(totalTime);
        }

//        Extension extension = new Extension();
        @Override
        protected void onPostExecute(String v) {
//            super.onPostExecute();
//            long endTime = System.nanoTime();
//            double totalTime = (endTime - startTime)/1000000000.0;
//            System.out.println("Total time is: " + totalTime);
            resultText.setText(String.valueOf(totalTime));
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


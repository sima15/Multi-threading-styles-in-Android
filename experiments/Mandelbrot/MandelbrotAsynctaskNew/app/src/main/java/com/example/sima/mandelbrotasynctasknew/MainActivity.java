package com.example.sima.mandelbrotasynctasknew;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {

    byte[][] out;
    static AtomicInteger yCt;
    static double[] Crb;
    static double[] Cib;

    long startTime;
    long endTime = startTime;
    long totalTime;
    int N =500;
    TextView resultText;
    int numThreads = 16;
    MandelAsyncTask[] tasks;
    Object object = new Object();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultText = (TextView) findViewById(R.id.resultText);
        startTime = System.nanoTime();
        System.out.println("start time is: " + startTime);

        for(int i=0; i<80; i++)
           doJob();

        String result = doJob();
        resultText.setText(result);
    }

    protected  String doJob(){
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
        tasks = new MandelAsyncTask[numThreads];
        for(int i=0; i<numThreads; i++){
            tasks[i] = new MandelAsyncTask(i*chunk, (i+1)*chunk);
            tasks[i].execute();
        }

        for (int k=0; k<numThreads; k++) {
            synchronized (object) {
                try {
                    while (!tasks[k].done) object.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        totalTime = (System.nanoTime() - startTime)/1000000;
        System.out.println("end time is: "+ System.nanoTime());
        System.out.println("Total time is: " + totalTime);
        return String.valueOf(totalTime);
    }

    class MandelAsyncTask extends AsyncTask<Integer, Void, String> {

        boolean done = false;
        int lb;
        int ub;

        public MandelAsyncTask(int a, int b){
            lb = a;
            ub = b;
        }
        @Override
        protected String doInBackground(Integer... params) {
            Log.d("Log", Thread.currentThread().getName() + " executing");
            int y;
            yCt.set(lb);
            while ((y = yCt.getAndIncrement()) < ub) {
                putLine(y, out[y]);
            }
            done = true;

//            System.out.print(("P4\n" + N + " " + N + "\n").getBytes());
//            for(int i=0;i<N;i++) System.out.println(out[i]);
//
            synchronized (object) {
                object.notify();
                Log.d("Log", Thread.currentThread().getName() + "Notified");
            }

            endTime= System.nanoTime();

           return  null;
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

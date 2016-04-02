package com.example.sima.mandelbrot.forkjoin;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MandelbrotForkJoin extends AppCompatActivity {

    int N = 500;
    long startTime;
    EditText inputText;
    long totalTime;

    static byte[][] out;
    static AtomicInteger yCt;
    static double[] Crb;
    static double[] Cib;

    ForkJoinPool forkJoinPool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mandelbrot_fork_join);

                startTime = System.currentTimeMillis();
                System.out.println("Start time is: " + startTime);
                inputText = (EditText) findViewById(R.id.inputText);


                for(int i=0; i<80; i++)
                    doJob();

                inputText.setText(String.valueOf(totalTime));
    }


    public void doJob(){
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

        MandelbrotTask task = new MandelbrotTask(0, out.length);


        forkJoinPool = new ForkJoinPool(poolLength);
        forkJoinPool.invoke(task);
        forkJoinPool.shutdown();

        try {
            forkJoinPool.awaitTermination(1, TimeUnit.DAYS);
            if (forkJoinPool.isTerminated()) {

                System.out.println(("P4\n" + N + " " + N + "\n").getBytes());
                for(int i=0;i<N;i++) System.out.println(out[i]);
                long endTime = System.currentTimeMillis();
                totalTime = endTime - startTime;
                System.out.println("end time is: " + endTime);
                System.out.println("Total time is: " + totalTime);

            }
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

    public class MandelbrotTask extends RecursiveAction {
        private static  final long serialVersionUID = 6136927121059165206L;

        private final  int THRESHOLD = 50;
        int upperBound;
        int lowerBound;
        int y;

        public MandelbrotTask(int a, int b){
            lowerBound = a;
            upperBound = b;
        }



        protected  void computeDirectly(int a , int b){

            yCt.set(a);
            while ((y = yCt.getAndIncrement()) < b) {
                putLine(y, out[y]);
            }
        }

        @Override
        public void compute() {

            if ((upperBound-lowerBound) < THRESHOLD) {
                computeDirectly(lowerBound, upperBound);
                return;
            }
            else{
                int middle = lowerBound + (upperBound-lowerBound)/2;
                MandelbrotTask worker1 = new MandelbrotTask(lowerBound, middle);
                MandelbrotTask worker2 = new MandelbrotTask(middle+1, upperBound);
                invokeAll(worker1, worker2);

            }
        }
    }

    static void putLine(int y, byte[] line) {
        for (int xb = 0; xb < line.length; xb++) {
            line[xb] = (byte) getByte(xb * 8, y);
        }
    }
}

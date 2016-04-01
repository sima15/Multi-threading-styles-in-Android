package com.example.sima.spectralnormforkjoin;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;
/**
 *
 * @author Sima Mehri
 */

public class MainActivity extends AppCompatActivity {
    long startTime;
    int numThread = 1;
    ForkJoinPool pool;
    Approximate[] threads;
    static int appIndex = 20;

    int range;
    double m_vBv = 0, m_vv = 0;
//    private double[] _u;
//    private double[] _v;
//    private double[] _tmp;

    double[] uArray;
    double[] vArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startTime = System.currentTimeMillis();
        System.out.println("Start time: " + startTime);

        final NumberFormat formatter = new DecimalFormat("#.000000000");
        int n = 16;
        try {
            for (int i = 0; i < 1; i++) {
                System.out.println("result is: " + formatter.format(spectralnormGame(n)));
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("total time: " + (System.currentTimeMillis() - startTime) + "ms");
    }

    private final double spectralnormGame(int n) throws InterruptedException {
        // create unit vector
        double[] u = new double[n];
        double[] v = new double[n];
        double[] tmp = new double[n];

        for (int i = 0; i < n; i++)
            u[i] = 1.0;

        // get available processor, then set up syn object
        System.out.println("No. of threads: " + numThread);

        int chunk = n / numThread;
        pool = new ForkJoinPool(numThread);
        threads = new Approximate[numThread];

        for (int i = 0; i < numThread; i++) {
            int r1 = i * chunk;
            int r2 = (i < (numThread - 1)) ? r1 + chunk : n;

            threads[i] = new Approximate(u, v, tmp, r1, r2);
            pool.submit(threads[i]);
            range += r2 - r1;
        }

        uArray = new double[range];
        vArray = new double[range];

        pool.shutdown();
        pool.awaitTermination(500, TimeUnit.SECONDS);

        for (int i = 0; i < range; i++) {
            m_vBv += uArray[i] * vArray[i];
            m_vv += vArray[i] * vArray[i];
        }
        return Math.sqrt(m_vBv / m_vv);
    }

    public class Approximate extends RecursiveAction {
        private double[] _u;
        private double[] _v;
        private double[] _tmp;
        private static final long serialVersionUID = 6136927121059165206L;
        int THRESHOLD = 0;


        private int range_begin, range_end;

        //        public Approximate(double[] u, double[] v, double[] tmp, int rbegin, int rend, double num1, double num2) {
        public Approximate(double[] u, double[] v, double[] tmp, int rbegin, int rend) {

            super();

            _u = u;
            _v = v;
            _tmp = tmp;

            range_begin = rbegin;
            range_end = rend;
        }

        public Approximate(double[] v, double[] tmp, double[] u, int r1, int r2, int num) {

            try {
                range_begin = r1;
                range_end = r2;

                _u = u;
                _v = v;
                _tmp = tmp;

                MultiplyAv(v, tmp);
                MultiplyAtv( tmp, u);

                MultiplyAv(u, tmp);
                MultiplyAtv( tmp, v);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }


        /* return element i,j of infinite matrix A */
        private final double eval_A(int i, int j) {
            int div = (((i + j) * (i + j + 1) >>> 1) + i + 1);
            return 1.0 / div;
        }

        /* multiply vector v by matrix A, each thread evaluate its range only */
        private final void MultiplyAv(final double[] v, double[] Av) throws InterruptedException {
            for (int i = range_begin; i < range_end; i++) {
                double sum = 0;
                for (int j = 0; j < v.length; j++)
                    sum += eval_A(i, j) * v[j];

                Av[i] = sum;
                uArray[i] = sum;
            }
            System.out.println(Thread.currentThread().getName() + " finished MultiplyAv");
        }

        /* multiply vector v by matrix A transposed */
        private final void MultiplyAtv(final double[] v, double[] Atv) throws InterruptedException {
            for (int i = range_begin; i < range_end; i++) {
                double sum = 0;
                for (int j = 0; j < v.length; j++)
                    sum += eval_A(j, i) * v[j];

                Atv[i] = sum;
                vArray[i] = sum;
            }
            System.out.println(Thread.currentThread().getName() + " finished MultiplyAtv");
        }
        /* multiply vector v by matrix A and then by matrix A transposed */
//        private final void MultiplyAtAv(final double[] v, double[] tmp, double[] AtAv) {
//            try {
//                MultiplyAv(v, tmp);
//                // all thread must syn at completion
//                MultiplyAtv(tmp, AtAv);
//                // all thread must syn at completion
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }


            @Override
            public void compute () {
                if (appIndex > THRESHOLD) {
                    appIndex--;
                    System.out.println("Index is: " + (appIndex+1));
                    Approximate ap11 = new Approximate(_u, _tmp, _v, range_begin, range_end, 10);
                    Approximate ap21 = new Approximate(_v, _tmp, _u, range_begin, range_end, 10);
//                    Approximate ap12 = new Approximate(_u, _tmp, range_begin, range_end);
//                    Approximate ap13 = new Approximate(_u, _tmp, range_begin, range_end);
//                    Approximate ap14 = new Approximate(_u, _tmp, range_begin, range_end);

                    invokeAll(ap11, ap21);
//                    ap11.invoke();
//                    ap12.fork();
//                    ap13.fork();
//                    ap14.fork();

//                    Approximate ap31 = new Approximate(_tmp, _v, range_begin, range_end, 5);
//                    Approximate ap32 = new Approximate(_tmp, _v, range_begin, range_end, 5);
//                    Approximate ap33 = new Approximate(_tmp, _v, range_begin, range_end, 5);
//                    Approximate ap34 = new Approximate(_tmp, _v, range_begin, range_end, 5);

//                    ap31.invoke();
//                    ap32.fork();
//                    ap33.fork();
//                    ap34.fork();

//                    ap11.join();
//                    ap31.join();



//                    Approximate ap21 = new Approximate(_v, _tmp, _u, range_begin, range_end);
//                    Approximate ap22 = new Approximate(_v, _tmp, range_begin, range_end);
//                    Approximate ap23 = new Approximate(_v, _tmp, range_begin, range_end);
//                    Approximate ap24 = new Approximate(_v, _tmp, range_begin, range_end);

//                    ap21.invoke();
//                    ap22.fork();
//                    ap23.fork();
//                    ap24.fork();

//                Approximate ap2 = new Approximate(_v, _tmp, _u);


//                    Approximate ap41 = new Approximate( _tmp, _u,  range_begin, range_end, 5);
//                    Approximate ap42 = new Approximate( _tmp, _u,  range_begin, range_end, 5);
//                    Approximate ap43 = new Approximate( _tmp, _u,  range_begin, range_end, 5);
//                    Approximate ap44 = new Approximate( _tmp, _u,  range_begin, range_end, 5);

//                    ap41.invoke();
//                    ap42.fork();
//                    ap43.fork();
//                    ap44.fork();

//                    ap11.join();
//                    ap12.join();
//                    ap13.join();
//                    ap14.join();

//                    ap21.join();
//                    ap22.join();
//                    ap23.join();
//                    ap24.join();

//                    ap31.join();
//                    ap32.join();
//                    ap33.join();
//                    ap34.join();

//                    ap41.join();
//                    ap42.join();
//                    ap43.join();
//                    ap44.join();
                } else {
                    return;
                }
            }
        }
}



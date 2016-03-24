package com.example.sima.spectral2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 *
 * @author Sima Mehri
 */

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        System.out.println("Just started");
        final NumberFormat formatter = new DecimalFormat("#.000000000");
        int n = 1000;
        long startTime = System.currentTimeMillis();
        System.out.println("Start time: "+ startTime);
        try {
            for(int i = 0; i < 10; i++) {

                System.out.println("result is: " + formatter.format(spectralnormGame(n)));
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("total time: " + (System.currentTimeMillis() - startTime) + "ms");

    }
    Lock lock = new Lock();

    static Approximate[] ap;
    static int nthread = Runtime.getRuntime().availableProcessors();


    private final double spectralnormGame(int n) throws InterruptedException {
        // create unit vector
        double[] u = new double[n];
        double[] v = new double[n];
        double[] tmp = new double[n];

        for (int i = 0; i < n; i++)
            u[i] = 1.0;

        // get available processor, then set up syn object
        System.out.println("No. of threads: " + nthread);

        int chunk = n / nthread;
        ap = new Approximate[nthread];

        for (int i = 0; i < nthread; i++) {
            int r1 = i * chunk;
            int r2 = (i < (nthread - 1)) ? r1 + chunk : n;

            ap[i] = new Approximate(u, v, tmp, r1, r2);
        }

        lock.justStarted = true;

        synchronized (lock) {
            int i = 0;
            while (!checkDone()) {
                if (!lock.checkIndex() || lock.justStarted) {
                    lock.justStarted = false;
                    lock.wait();
                }

                //wake all threads
                for (Approximate a : ap) {
                    synchronized (a) {
                        a.notifyAll();
                    }
                }
                lock.justStarted = true;

                i++;
            }
            System.out.println("iteration: " + (i++));
        }

        double vBv = 0, vv = 0;
        for (int i = 0; i < nthread; i++) {
            try {
                ap[i].join();

                vBv += ap[i].m_vBv;
                vv += ap[i].m_vv;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return Math.sqrt(vBv / vv);
    }

    public static synchronized boolean checkDone() {

        for (Approximate a : ap) {
            if (!a.done) return false;
        }
        return true;
    }

    public class Approximate extends Thread {
        private double[] _u;
        private double[] _v;
        private double[] _tmp;

        private int range_begin, range_end;
        double m_vBv = 0, m_vv = 0;

        public boolean done = false;

        public Approximate(double[] u, double[] v, double[] tmp, int rbegin, int rend) {
            super();

            _u = u;
            _v = v;
            _tmp = tmp;

            range_begin = rbegin;
            range_end = rend;

            start();
        }

        public void run() {
            // 20 steps of the power method
            for (int i = 0; i < 20; i++) {
                MultiplyAtAv(_u, _tmp, _v);
                MultiplyAtAv(_v, _tmp, _u);
            }


            for (int i = range_begin; i < range_end; i++) {
                m_vBv += _u[i] * _v[i];
                m_vv += _v[i] * _v[i];
            }

            done = true;
            //done, let's notify to the main
            synchronized (lock) {
                lock.notify();
            }
        }

        /* return element i,j of infinite matrix A */
        private final  double eval_A(int i, int j) {
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
            }

            synchronized (this) {
                lock.increment();
                this.wait();
            }

        }

        /* multiply vector v by matrix A transposed */
        private final void MultiplyAtv(final double[] v, double[] Atv) throws InterruptedException {
            for (int i = range_begin; i < range_end; i++) {
                double sum = 0;
                for (int j = 0; j < v.length; j++)
                    sum += eval_A(j, i) * v[j];

                Atv[i] = sum;
            }

            synchronized (this) {
                lock.increment();
                this.wait();
            }

        }

        /* multiply vector v by matrix A and then by matrix A transposed */
        private final void MultiplyAtAv(final double[] v, double[] tmp, double[] AtAv) {
            try {
                MultiplyAv(v, tmp);
                // all thread must syn at completion
                MultiplyAtv(tmp, AtAv);
                // all thread must syn at completion
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class Lock {
        public int index = 0;
        public boolean justStarted = false;

        synchronized boolean increment() {
            index++;
            if (index % nthread == 0) {
                synchronized(lock) {	//the last thread is done
                    index = 0;
                    lock.notify();
                }
                return true;
            } else {
                return false;
            }
        }

        synchronized boolean checkIndex() {
            if (index % nthread != 0) {
                return false;
            }

            return true;
        }
    }
}

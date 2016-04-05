package com.example.sima.spectralnormexecutor;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
/**
 *
 * @author Sima Mehri
 */

public class MainActivity extends AppCompatActivity {

    long startTime;
    int numThread = 4;
    int n = 1000;
    ExecutorService pool;
    Lock lock = new Lock();
    int count;
//    double vBv = 0, vv = 0;
    Future<Double[]> future;
    Approximate[] ap;
    final NumberFormat formatter = new DecimalFormat("#.000000000");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startTime = System.currentTimeMillis();
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
        pool = Executors.newFixedThreadPool(numThread);

        ap = new Approximate[numThread];

        for (int i = 0; i < numThread; i++) {

            int r1 = i * chunk;
            int r2 = (i < (numThread - 1)) ? r1 + chunk : n;

            ap[i] = new Approximate(u, v, tmp, r1, r2);
            future =  pool.submit(ap[i]);
        }

        lock.justStarted = true;

        synchronized (lock) {
            int j = 0;
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

                j++;
            }
        }
        double vBv = 0, vv = 0;
        for (int i = 0; i < numThread; i++) {
            try {
                vBv += future.get()[0];
                vv += future.get()[1];
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        pool.shutdown();

        return Math.sqrt(vBv / vv);
    }

    public  synchronized boolean checkDone() {
        for (Approximate a : ap) {
            if (!a.done) return false;
        }
        return true;
    }

    public synchronized void increment(){
        count++;
    }

    public class Approximate implements Callable<Double[]> {
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
        }

        public Double[] call() {
            // 20 steps of the power method
            for (int i = 0; i < 20; i++) {
                MultiplyAtAv(_u, _tmp, _v);
                MultiplyAtAv(_v, _tmp, _u);
            }


            for (int i = range_begin; i < range_end; i++) {
                m_vBv += _u[i] * _v[i];
                m_vv += _v[i] * _v[i];
            }

            Double[] vbv = new Double[2];
            vbv[0]= m_vBv;
            vbv[1] = m_vv;

            done = true;
            increment();
            //done, let's notify to the main
            synchronized (lock) {
//                System.out.println("notify to main?");
                lock.notify();
            }
            return vbv;
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
                increment();
                lock.increment();
//                System.out.println(Thread.currentThread().getName() + " is waiting in MultiplyAv");
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
                increment();
                lock.increment();
//                System.out.println(Thread.currentThread().getName() + " is waiting in MultiplyAtv");
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
            if (index % numThread == 0) {
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
            if (index % numThread != 0) {
                return false;
            }

            return true;
        }
    }

}


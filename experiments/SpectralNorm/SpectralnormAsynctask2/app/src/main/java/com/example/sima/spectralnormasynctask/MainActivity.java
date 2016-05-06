package com.example.sima.spectralnormasynctask;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
*
* @author Sima Mehri
*/

public class MainActivity extends AppCompatActivity {
    long startTime;
    int numThread = 8;
    int n = 1000;
    static Approximate[] ap;
    Lock lock = new Lock();
    Object object = new Object();
    final NumberFormat formatter = new DecimalFormat("#.000000000");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startTime = System.currentTimeMillis();
        System.out.println("Start time: " + startTime);
        System.out.println("Just started");

        try {
            for(int i = 0; i < 1; i++) {
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
        ap = new Approximate[numThread];

        for (int i = 0; i < numThread; i++) {
            int r1 = i * chunk;
            int r2 = (i < (numThread - 1)) ? r1 + chunk : n;

            ap[i] = new Approximate(u, v, tmp, r1, r2);
            ap[i].executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//            ap[i].execute();
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
        Log.d("Debug", Thread.currentThread().getName() +" inside main thread");
        double vBv = 0, vv = 0;
        for (int i = 0; i < numThread; i++) {
            synchronized (object){
                try {
                        while(!ap[i].finished) object.wait();
                    Log.d("Debug", Thread.currentThread().getName() + " finished waiting for " + ap[i]);
                        vBv += ap[i].m_vBv;
                        vv += ap[i].m_vv;
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

    public class Approximate extends AsyncTask {

        boolean finished = false;
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

        @Override
        protected Object doInBackground(Object[] params) {
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
            finished = true;
            synchronized (object){
                object.notify();
            }
            Log.d("Debug", Thread.currentThread().getName() +" finished");
            return  null;
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

            Log.d("Debug", Thread.currentThread().getName() +" finished MultiplyAv");
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
            Log.d("Debug", Thread.currentThread().getName() +" finished MultiplyAtv");

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


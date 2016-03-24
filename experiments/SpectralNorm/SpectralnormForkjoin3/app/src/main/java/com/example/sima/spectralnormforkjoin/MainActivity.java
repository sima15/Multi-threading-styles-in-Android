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
    int numThread = 4;
    ForkJoinPool pool;
    Lock lock = new Lock();
    double vBv = 0, vv = 0;
    Approximate[] threads;
    double[] vbv = new double[2];
    int appIndex = 20;

//    private int range_begin, range_end;
//    double m_vBv = 0, m_vv = 0;
    private double[] _u;
    private double[] _v;
    private double[] _tmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startTime = System.currentTimeMillis();
        System.out.println("Start time: "+ startTime);

        final NumberFormat formatter = new DecimalFormat("#.000000000");
        int n = 16;
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
        pool = new ForkJoinPool(numThread);
        threads = new Approximate[numThread];

        for (int i = 0; i < numThread; i++) {
            int r1 = i * chunk;
            int r2 = (i < (numThread - 1)) ? r1 + chunk : n;

            threads[i] = new Approximate(u, v, tmp, r1, r2, 0, 0);
//            future = pool.submit(threads[i]);
            pool.submit(threads[i]);
        }

//        lock.justStarted = true;
//
//        synchronized (lock) {
//            int j = 0;
//            while (!checkDone()) {
//                if (!lock.checkIndex() || lock.justStarted) {
//                    lock.justStarted = false;
//                    lock.wait();
//                }
//
//                //wake all threads
//                for (Approximate a : threads) {
//                    synchronized (a) {
//                        a.notifyAll();
//                    }
//                }
//                lock.justStarted = true;
//                System.out.println("iteration: " + (j++));
//            }
//        }
//
//            for (int i = 0; i < numThread; i++) {
//                try {
//                    vBv += future.get()[0];
//                    vv += future.get()[1];
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        pool.shutdown();
//
//        return Math.sqrt(vBv); // / vv);
//        }

//        for (int i = range_begin; i < range_end; i++) {
//            m_vBv += _u[i] * _v[i];
//            m_vv += _v[i] * _v[i];
//        }
//        vbv[0] = m_vBv;
//        vbv[1] = m_vv;


        pool.shutdown();
        int i=0;
        while(i<numThread) {
            for (i = 0; i < numThread; i++) {
                if (threads[i].isDone()) {
                    try {
//                vBv += future.get()[0];
                        vBv += threads[i].m_vBv;
//                vv += future.get()[1];
//                    vBv += m_vBv;
                        vv += threads[i].m_vv;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else continue;
            }
        }
        pool.awaitTermination(500, TimeUnit.SECONDS);


        return Math.sqrt(vBv / vv);
    }

    public  synchronized boolean checkDone() {
        for (Approximate a : threads) {
            if (!a.done) return false;
        }
        return true;
    }

//    public synchronized void increment(){
//        count++;
//    }

    public class Approximate extends RecursiveAction {
//        private double[] _u;
//        private double[] _v;
//        private double[] _tmp;
        private static  final long serialVersionUID = 6136927121059165206L;
        int THRESHOLD = 0;


        private int range_begin, range_end;
        double m_vBv, m_vv;

        public boolean done = false;

        public Approximate(double[] u, double[] v, double[] tmp, int rbegin, int rend, double num1, double num2) {
            super();

            _u = u;
            _v = v;
            _tmp = tmp;

            range_begin = rbegin;
            range_end = rend;

            m_vBv = num1;
            m_vv = num2;
            //start();
        }
        public Approximate(double[] u, double[] v, double[] tmp){

           try{
            MultiplyAv(u, v);
           }catch (Exception e){
               e.printStackTrace();
           }

//            MultiplyAtAv( u,v, tmp);
//            MultiplyAtAv( _v, _tmp, _u);
        }

        public Approximate(double[] v, double[] tmp){
            try{
            MultiplyAtv(v, tmp);
            }catch (Exception e){
                e.printStackTrace();
            }
        }


        /* return element i,j of infinite matrix A */
            private final double eval_A ( int i, int j){
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
            System.out.println(Thread.currentThread().getName() + " finished MultiplyAv");

//            synchronized (this) {
//                lock.increment();
//                System.out.println(Thread.currentThread().getName() + " is waiting in MultiplyAv");
//                this.wait();
//            }

        }

        /* multiply vector v by matrix A transposed */
        private final void MultiplyAtv(final double[] v, double[] Atv) throws InterruptedException {
            for (int i = range_begin; i < range_end; i++) {
                double sum = 0;
                for (int j = 0; j < v.length; j++)
                    sum += eval_A(j, i) * v[j];

                Atv[i] = sum;
            }
            System.out.println(Thread.currentThread().getName() + " finished MultiplyAtv");

//            synchronized (this) {
//                lock.increment();
//                System.out.println(Thread.currentThread().getName() + " is waiting in MultiplyAtv");
//                this.wait();
//            }
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
//        protected void computeDirectly() {
//
//
//            for (int i = range_begin; i < range_end; i++) {
//                m_vBv += _u[i] * _v[i];
//                m_vv += _v[i] * _v[i];
//            }
//                vbv[0] = m_vBv;
//                vbv[1] = m_vv;
//
//
////            return vbv;
//        }


        @Override
        public void compute() {
            if(appIndex >= THRESHOLD){
                appIndex--;
                Approximate ap1 = new Approximate(_u, _tmp, _v);
                Approximate ap2 = new Approximate(_v, _tmp, _u);

                System.out.println("Index is: "+ appIndex);
                ap1.fork();
                ap2.fork();
                ap1.join();
                ap2.join();
                Approximate ap3 = new Approximate(_u, _tmp);
                Approximate ap4 = new Approximate(_v, _tmp);
                ap3.fork();
                ap4.fork();
                ap3.join();
                ap4.join();

//                done = true;
////                increment();
//
//                synchronized (lock) {
//                    System.out.println("notify to main?");
//                    lock.notify();
//                }
//                done = false;
            }else{
//                computeDirectly();
                for (int i = range_begin; i < range_end; i++) {
                    m_vBv += _u[i] * _v[i];
                    m_vv += _v[i] * _v[i];
                }
                vbv[0] = m_vBv;
                vbv[1] = m_vv;
                return;
            }

//            return result;
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



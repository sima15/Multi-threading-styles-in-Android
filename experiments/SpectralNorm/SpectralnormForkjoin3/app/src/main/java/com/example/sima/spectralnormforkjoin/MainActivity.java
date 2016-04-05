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

    int n = 16;
    int numThread = 4;
    ForkJoinPool pool;

    int range = n;
    double m_vBv = 0, m_vv = 0;

    double[] uArray;
    double[] vArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startTime = System.currentTimeMillis();
        System.out.println("Start time: " + startTime);

        final NumberFormat formatter = new DecimalFormat("#.000000000");

        try {
            for (int i = 0; i < 1; i++) {
                System.out.println("result is: " + formatter.format(spectralnormGame(n)));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("total time: " + (System.currentTimeMillis() - startTime) + "ms");
    }


    public class Approximate extends RecursiveAction {

        private double[] _u;
        private double[] _v;
        private double[] _tmp;

        private static final long serialVersionUID = 6136927121059165206L;
        int THRESHOLD = 4;


        private int range_begin;
        private int range_end;

        public Approximate(double[] u, double[] v, double[] tmp, int rbegin, int rend) {

            _u = u;
            _v = v;
            _tmp = tmp;

            range_begin = rbegin;
            range_end = rend;
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


            @Override
            public void compute () {
                if((range_end-range_begin)<THRESHOLD){
                    computeDirectly();
                    return;
                }

                int middle = range_begin + (range_end-range_begin)/2;
                invokeAll(new Approximate(_u, _v, _tmp, range_begin, middle),
                        new Approximate(_u, _v, _tmp, middle, range_end));

            }

        public void computeDirectly(){
            System.out.println("r1= "+ range_begin + " r2= "+ range_end);
            MultiplyAtAv(_u, _tmp, _v);
            MultiplyAtAv(_v, _tmp, _u);
        }
    }

    private final double spectralnormGame(int n) throws InterruptedException {
        // create unit vector
        double[] u = new double[n];
        double[] v = new double[n];
        double[] tmp = new double[n];

        for (int i = 0; i < n; i++)
            u[i] = 1.0;

        pool = new ForkJoinPool(numThread);

        uArray = new double[range];
        vArray = new double[range];

        Approximate task = new Approximate(u, v, tmp, 0, n);
        pool.invoke(task);
        pool.shutdown();
        pool.awaitTermination(500, TimeUnit.SECONDS);

        for (int i = 0; i < range; i++) {
            m_vBv += uArray[i] * vArray[i];
            m_vv += vArray[i] * vArray[i];
        }
        return Math.sqrt(m_vBv / m_vv);
    }
}




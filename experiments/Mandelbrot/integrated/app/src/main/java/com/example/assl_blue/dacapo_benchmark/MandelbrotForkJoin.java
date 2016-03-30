package com.example.assl_blue.dacapo_benchmark;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MandelbrotForkJoin extends MBase {


    ForkJoinPool forkJoinPool;


    int index ;
    MandelbrotForkJoin(){
        super();
        index = numThread;
    }

    MandelbrotForkJoin(int numThread){
        super(numThread);
        index = numThread;
    }


    @Override
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

        MandelbrotTask task = new MandelbrotTask();
        forkJoinPool = new ForkJoinPool(numThread);
        forkJoinPool.invoke(task);
        forkJoinPool.shutdown();

        try {
            forkJoinPool.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            Log.e("Mbench", "exception", e);
        }
    }

    public class MandelbrotTask extends RecursiveAction {
        private static  final long serialVersionUID = 6136927121059165206L;

        private final  int THRESHOLD = 0;

        @Override
        public void compute() {
            if (index >= THRESHOLD) {
                index--;

                int y;
                while ((y = yCt.getAndIncrement()) < out.length) {
                    putLine(y, out[y]);
                }

                MandelbrotTask worker = new MandelbrotTask();
                worker.invoke();
            }
            else{
                return;
            }
        }
    }
}

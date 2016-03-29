package com.example.assl_blue.dacapo_benchmark;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

        int poolLength = 32;
        MandelbrotTask task = new MandelbrotTask();
        forkJoinPool = new ForkJoinPool(poolLength);
        forkJoinPool.invoke(task);
        forkJoinPool.shutdown();

        try {
            forkJoinPool.awaitTermination(1, TimeUnit.DAYS);
            if (forkJoinPool.isTerminated()) {
                for(int i=0;i<N;i++) System.out.println(out[i]);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public class MandelbrotTask extends RecursiveAction {
        private static  final long serialVersionUID = 6136927121059165206L;

        private final  int THRESHOLD = 0;

        @Override
        public void compute() {
            if (index >= THRESHOLD) {
                index--;
                int y = 1;
                byte res = 0;
                for (int xb = 0; xb < out[y].length; xb++) {
                    out[y][xb] = (byte) getByte(xb * 8, y);
                    res = out[y][xb];
                }
                System.out.println("res is: "+String.valueOf(res));

                MandelbrotTask worker = new MandelbrotTask();
                worker.invoke();
            }
            else{


                return;
            }
        }
    }
}

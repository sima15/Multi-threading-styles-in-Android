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


    MandelbrotForkJoin(){
        super();
    }

    MandelbrotForkJoin(int numThread){
        super(numThread);
    }


    @Override
    public void doJob(){
        MandelbrotTask task = new MandelbrotTask(numThread);
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

        private int count = 0;

        MandelbrotTask(int cnt){
            super();
            count = cnt;
        }

        @Override
        public void compute() {
            if(count <= 1) {
                doTask();
                return;
            }
            invokeAll(new MandelbrotTask(count/2),
                    new MandelbrotTask(count/2));
        }
    }
}

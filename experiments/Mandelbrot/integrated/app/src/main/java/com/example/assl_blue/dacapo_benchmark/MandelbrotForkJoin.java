package com.example.assl_blue.dacapo_benchmark;

import android.util.Log;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

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
        MandelbrotTask task = new MandelbrotTask(0, out.length);
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
        private final  int THRESHOLD = 50;
        int upperBound;
        int lowerBound;
        int y;

        public MandelbrotTask(int a, int b){
            lowerBound = a;
            upperBound = b;
        }

        protected  void computeDirectly(int a , int b){
            doTask(a, b);
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
}

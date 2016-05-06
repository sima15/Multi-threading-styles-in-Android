package com.example.sima.incrementforkjoin;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.math.BigInteger;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    TextView resultView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resultView = (TextView) findViewById(R.id.resultView);
        BigInteger number = new BigInteger("0");

        int upperLimit = 2250000;
        int numThreads = 4;
//        int chunk = upperLimit/numThreads;

        ForkJoinPool pool = new ForkJoinPool(numThreads);
        Increment task = new Increment(0, upperLimit);
        pool.submit(task);
        pool.shutdown();

        try {
            pool.awaitTermination(500, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        number = task.num;
        resultView.setText(String.valueOf(number));
    }

    synchronized BigInteger increase(int i, BigInteger number){
        return number.add(BigInteger.valueOf(i));
    }


    class Increment extends RecursiveTask{

        BigInteger num = new BigInteger("0");
        int start;
        int end;
        Increment(int start, int end){
            this.start = start;
            this.end = end;
        }


        int length = end - start;
        int threshold = 10;

        @Override
        protected BigInteger compute() {
            if (length < threshold) {
                num = computeDirectly();
                return num;
            }

            int split = length / 2;

            invokeAll(new Increment(start, split),
                    new Increment( start + split, end));

            return null;
        }

        public  BigInteger computeDirectly(){
            for (int i = start; i < end; i++) {
                num = increase(i, num);
//                Log.d("Debug", Thread.currentThread().getName() + ": num= " + num);
            }
            return num;
        }
    }
}

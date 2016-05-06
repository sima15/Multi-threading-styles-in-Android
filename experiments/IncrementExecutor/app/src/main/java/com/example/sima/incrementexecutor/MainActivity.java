package com.example.sima.incrementexecutor;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.math.BigInteger;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    TextView resultView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        long start = System.currentTimeMillis();
        resultView = (TextView)findViewById(R.id.resultView);

        BigInteger number = new BigInteger("0");
        int upperLimit = 1000;
        int numThreads = 8;
        int chunk = upperLimit/numThreads;
//        Increment[] pool = new Increment[numThreads];

        ExecutorService pool = Executors.newFixedThreadPool(numThreads);
        Future[] future = new Future[numThreads];
        for(int i = 0; i <numThreads; i++){
            future[i] = pool.submit(new Increment(i*chunk, (i+1)*chunk));
        }

        for(int j=0; j<numThreads; j++) {
            try {
                number = number.add((BigInteger) future[j].get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        pool.shutdown();
        try {
            pool.awaitTermination(500, TimeUnit.SECONDS);
        }catch(Exception e){
            e.printStackTrace();
        }


        resultView.setText(String.valueOf(number));
        long duration = System.currentTimeMillis()-start;
        Log.d("Duration: ", String.valueOf(duration));
    }

    BigInteger increase(int i, BigInteger number){
        return number.add(BigInteger.valueOf(i));
    }

    class Increment implements Callable{

        BigInteger num = new BigInteger("0");
        int start;
        int end;
        Increment(int start, int end){
            this.start = start;
            this.end = end;
        }

        @Override
        public BigInteger call() {
            for (int i = start; i < end; i++) {
                num = increase(i, num);
//                Log.d("Debug", Thread.currentThread().getName()+ ": num= "+ num);
            }
            return num;
        }
    }
}

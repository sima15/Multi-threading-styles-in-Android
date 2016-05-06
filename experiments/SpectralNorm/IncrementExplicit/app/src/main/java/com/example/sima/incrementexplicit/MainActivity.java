package com.example.sima.incrementexplicit;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.math.BigInteger;

public class MainActivity extends AppCompatActivity {

    TextView resultView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        long start = System.currentTimeMillis();
        resultView = (TextView)findViewById(R.id.resultView);

        BigInteger number = new BigInteger("0");
        int upperLimit = 100;
        int numThreads = 2;
        int chunk = upperLimit/numThreads;
        Increment[] pool = new Increment[numThreads];

        for(int i = 0; i <numThreads; i++){
            pool[i] = new Increment(i*chunk, (i+1)*chunk);
            pool[i].start();
        }

        for(int k=0; k<numThreads; k++){
            try {
                pool[k].join();
//                Log.d("Debug", "num= " + pool[k].num);
                number = number.add(pool[k].num);
//                Log.d("Debug", "number= " + number);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        resultView.setText(String.valueOf(number));
        long duration = System.currentTimeMillis()-start;
        Log.d("Duration: ", String.valueOf(duration));
    }

    BigInteger increase(int i, BigInteger number){
        return number.add(BigInteger.valueOf(i));
    }


    class Increment extends  Thread {

        BigInteger num = new BigInteger("0");
        int start;
        int end;
        Increment(int start, int end){
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {
            for (int i = start; i < end; i++) {
                num = increase(i, num);
//                Log.d("Debug", Thread.currentThread().getName() + ": num= " + num);
            }
        }
    }
}

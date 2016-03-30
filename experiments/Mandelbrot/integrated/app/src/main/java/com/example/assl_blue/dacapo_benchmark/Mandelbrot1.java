package com.example.assl_blue.dacapo_benchmark;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;

import java.util.concurrent.atomic.AtomicInteger;

public class Mandelbrot1 extends AppCompatActivity {

    int repeatNum = 50;

    public enum Style {
        Explict, ForkJoin, AsyncTask, Executor, HandlerR, HandlerM
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mandelbrot1);


        localTest();

    }


    private void localTest(){
        int[] testThreads = new int[] {1,2,4,8,16,32,64};

        for (Style style : Style.values()) {
            for (int numThreads : testThreads) {
                startTest(style, numThreads);
            }

        }
    }

    private void startTest(Style style, int numThreads){
        long startTime;
        Log.i("MBench Local Test Start", String.format("Style : %s Number of Thread %d", style.toString(), numThreads));
        startTime = System.nanoTime();
        Log.d("MBench Test start", "start time is: " + startTime);
        MBase c = getStyleClass(style, numThreads);
        c.doJob();
        Log.d("MBench Test End", "end time is: " + startTime + " Duration" + (System.nanoTime() - startTime));
        Log.i("MBench Local Test End", String.format("Style : %s Number of Thread %d", style.toString(), numThreads));
    }

    private MBase getStyleClass(Style style, int numThreads) {
        switch (style) {
        case Explict:
            return new MandelbrotExplict(numThreads);
        case ForkJoin:
            return new MandelbrotForkJoin(numThreads);
        case AsyncTask:
            return new MandelbrotAsynctask(numThreads);
        case Executor:
            return new MandelbrotExecutor(numThreads);
        case HandlerR:
            return new MandelbrotHandlerM(numThreads);
        case HandlerM:
            return new MandelbrotHandlerR(numThreads);
        }

        return null;
    }

    private void testWithPM(){
        PowerMonitor powerMonitor = new PowerMonitor();
        powerMonitor.startMonitoring();
        Style style;
        int numThreads;
        while (true) {
            String targetString = powerMonitor.getTarget();

            if (targetString.equalsIgnoreCase("Done")) break;

            String[] target = targetString.split(" ");
            numThreads = Integer.parseInt(target[1]);

            try{
                style = Style.valueOf(target[0]);
                startTest(style, numThreads);
            }catch (Exception e){
                Log.d("Parsing Error", targetString);
                Log.d("Parsing Error", e.getMessage());
                SystemClock.sleep(10000);
                continue;
            }


            Log.i("MBench Info", String.format("Test Start Style: %s Thread : %d", style.name(), numThreads));


            for (int j = 0; j < repeatNum; j++) {

                if(style.equals(Style.AsyncTask)){
                    break;
                }
            }

            powerMonitor.stopMonitoring(String.format("Mandelbrot_%s_%d", style.name(), numThreads));
            SystemClock.sleep(1000);
        }
        powerMonitor.saveMonitoring();

    }
    

}




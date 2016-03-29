package com.example.assl_blue.dacapo_benchmark;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;

import java.util.concurrent.atomic.AtomicInteger;

public class Mandelbrot1 extends AppCompatActivity {

    long startTime;
    long endTime = startTime;
    long totalTime;
    int repeatNum = 10;

    public enum Style {
        /*Explict,*/ ForkJoin, AsyncTask, Executor, HandlerR, HandlerM
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mandelbrot1);

        startTime = System.nanoTime();
        System.out.println("start time is: " + startTime);
        Style style = Style.Executor;
        localTest();

    }


    private void localTest(){
        int[] testThreads = new int[] {1,2,4,8,16,32,64};

        for (Style style : Style.values()) {
            for (int numThreads : testThreads) {
                Log.i("Local Test Start", String.format("Style : %s Number of Thread %d", style.toString(), numThreads));
                startTest(style, numThreads);
                Log.i("Local Test End", String.format("Style : %s Number of Thread %d", style.toString(), numThreads));
            }

        }
    }

    private void startTest(Style style, int numThreads){
        MBase c = getStyleClass(style, numThreads);
        c.doJob();
    }

    private MBase getStyleClass(Style style, int numThreads) {
        switch (style) {
        /*case Explict:
            new Ma();
            break;*/
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


            Log.i("Thread Style Info", String.format("Test Start Style: %s Thread : %d", style.name(), numThreads));


            for (int j = 0; j < repeatNum; j++) {

                if(style.equals(Style.AsyncTask)){
                    break;
                }
            }

            powerMonitor.stopMonitoring(String.format("ImageBlur_%s_%d", style.name(), numThreads));
            //SystemClock.sleep(1000);
        }
        powerMonitor.saveMonitoring();

    }
    // @Override
    // protected void onCreate(Bundle savedInstanceState) {
    //     super.onCreate(savedInstanceState);
    //     setContentView(R.layout.activity_mandelbrot1);

    //     startTime = System.currentTimeMillis();
    //     System.out.println("Start time is: " + startTime);
    //     inputText = (EditText) findViewById(R.id.inputText);

    //     for(int i=0; i<800; i++) doJob();

    //     if(System.currentTimeMillis()>endTime) endTime= System.currentTimeMillis();

    //     if(mainThread) totalTime = endTime - startTime;
    //     System.out.println("end time is: "+endTime);
    //     System.out.println("Total time is: "+totalTime);
    //     inputText.setText(String.valueOf(totalTime));
    // }


    

}




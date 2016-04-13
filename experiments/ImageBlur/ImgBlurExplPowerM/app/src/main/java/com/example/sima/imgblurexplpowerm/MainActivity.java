package com.example.sima.imgblurexplpowerm;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.LinearLayout;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    int repeatNum = 10;
    int[] src, dst;
    int w, h;
    int pieceWidth;
    static  int numThreads;
    long startTime;
    Bitmap orgBitmap;
    Bitmap bitmap;
    Bitmap dest;
    static  LinearLayout layout;
    MainActivity mainActivity;
    static Handler handler;

    public enum Style {
        Explict, Executor, ForkJoin,  HandlerR, HandlerM, AsyncTask
    }
//    public enum Style {
//        Asynctask
//    }
    Style style;


    void doJob(){

        bitmap = orgBitmap.copy(Bitmap.Config.RGB_565, true);
        dest = orgBitmap.copy(Bitmap.Config.RGB_565, true);

        w = bitmap.getWidth();
        h = bitmap.getHeight();
        pieceWidth = w*h/numThreads;

        src = new int[w * h];
        dst = new int[w * h];
        bitmap.getPixels(src, 0, w, 0, 0, w, h);
    }


    private void startExplict() throws InterruptedException {
        JobHandler jobHandler = new JobHandler(mainActivity, 0);
        jobHandler.start();
        jobHandler.join();
        jobHandler = null;
        System.gc();

    }

    private void startExecutor() throws InterruptedException{
        JobHandler jobHandler = new JobHandler(mainActivity, 1);
        jobHandler.start();
        jobHandler.join();
        jobHandler = null;
        System.gc();
    }

    private void startForkJoin() throws InterruptedException {
        JobHandler jobHandler = new JobHandler(mainActivity, 2);
        jobHandler.start();
        jobHandler.join();
        jobHandler = null;
        System.gc();
    }

    private void startAsyncTask() throws InterruptedException {
        JobHandler jobHandler = new JobHandler(mainActivity, 3);
        jobHandler.start();
        jobHandler.join();
        jobHandler = null;
        System.gc();
    }

    private void startHandlerR() throws InterruptedException{
        JobHandler jobHandler = new JobHandler(mainActivity, 4);
        jobHandler.start();
        jobHandler.join();
        jobHandler = null;
        System.gc();
    }

    private void startHandlerM() throws InterruptedException{
        JobHandler jobHandler = new JobHandler(mainActivity, 5);
        jobHandler.start();
        jobHandler.join();
        jobHandler = null;
        System.gc();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        orgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.testimage);

        mainActivity = new MainActivity();
        mainActivity.orgBitmap = orgBitmap;
        layout = (LinearLayout)findViewById(R.id.layout);
        handler = new Handler();

        Log.d("Debug", "Main activity created");
//        startWithPM();
        localTest();
    }

    private void localTest() {
        int[] testThreads = new int[]{1, 2, 4, 8, 16, 32, 64};
        System.out.println("Test threads:" + Arrays.toString(testThreads));
        repeatNum = 5;
        for (Style s : Style.values()) {
            style = s;
            for (int n : testThreads) {
                numThreads = n;
                Log.i("IBench", String.format("Test Start Style: %s Thread : %d", s.name(), numThreads));
                for (int j = 0; j < repeatNum; j++) {
                    try {
                        startTest();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.gc();
                }

                Log.i("IBench", String.format("Test End Style: %s Thread : %d", s.name(), numThreads));
            }

        }

    }

    private void startWithPM() throws InterruptedException {

        PowerMonitor powerMonitor = new PowerMonitor();
        powerMonitor.startMonitoring();
        while (true) {
            String targetString = powerMonitor.getTarget();

            if (targetString.equalsIgnoreCase("Done")) break;

            String[] target = targetString.split(" ");

            try {
                style = Style.valueOf(target[0]);
                numThreads = Integer.parseInt(target[1]);
            } catch (Exception e) {
                Log.d("IBench", String.format("Erro during parse String %s, Style : %s ", targetString, target[0]));
                Log.e("IBench", "Style Parsing Error", e);
                SystemClock.sleep(10000);
                continue;
            }

            Log.i("IBench", String.format("Test Start Style: %s Thread : %d", style.name(), numThreads));

            powerMonitor.readFirstUsage();
            for (int j = 0; j < repeatNum; j++) {
                startTest();
            }
            powerMonitor.readLastUsage();


            powerMonitor.stopMonitoring(String.format("ImageBlur_%s_%d", style.name(), numThreads));
            SystemClock.sleep(10000);
        }
        powerMonitor.saveMonitoring();
    }

    private void startTest() throws InterruptedException {
        switch (style) {
            case Explict:
                startExplict();
                break;
            case ForkJoin:
                startForkJoin();
                break;
            case AsyncTask:
                startAsyncTask();
                break;
            case Executor:
                startExecutor();
                break;
            case HandlerR:
                startHandlerR();
                break;
            case HandlerM:
                startHandlerM();
                break;
        }

//        startAsyncTask();
    }
}

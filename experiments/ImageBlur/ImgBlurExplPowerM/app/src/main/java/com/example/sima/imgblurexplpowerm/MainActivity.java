package com.example.sima.imgblurexplpowerm;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {
    int repeatNum;
    int[] src, dst;
    int w, h;
    int pieceWidth;
    int numThreads = 16;
    long startTime;
    Bitmap orgBitmap;
    Bitmap bitmap;
    Bitmap dest;
    LinearLayout layout;
    MainActivity mainActivity;

    public enum Style {
        Explict, ForkJoin, AsyncTask, Executor, HandlerR, HandlerM
    }

    Style style;


    void doJob(){


        bitmap = orgBitmap.copy(Bitmap.Config.RGB_565, true);
        dest = orgBitmap.copy(Bitmap.Config.RGB_565, true);

        w = bitmap.getWidth();
        h = bitmap.getHeight();
        pieceWidth = w/numThreads;

        src = new int[w * h];
        dst = new int[w * h];
        bitmap.getPixels(src, 0, w, 0, 0, w, h);
    }


    private void startExplict() {
        JobHandler jobHandler = new JobHandler(mainActivity, 0);
        jobHandler.start();

    }

    private void startForkJoin() {
        JobHandler jobHandler = new JobHandler(mainActivity, 2);
        jobHandler.start();
    }

    private void startAsyncTask() {
        JobHandler jobHandler = new JobHandler(mainActivity, 3);
        jobHandler.start();
    }

    private void startExecutor() {
        JobHandler jobHandler = new JobHandler(mainActivity, 1);
        jobHandler.start();
    }

    private void startHandlerR() {
        JobHandler jobHandler = new JobHandler(mainActivity, 4);
        jobHandler.start();
    }

    private void startHandlerM() {
        JobHandler jobHandler = new JobHandler(mainActivity, 5);
        jobHandler.start();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        orgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.testimage);

        mainActivity = new MainActivity();
        mainActivity.orgBitmap = orgBitmap;
        layout = (LinearLayout)findViewById(R.id.layout);

//        startWithPM();
        localTest();
    }

    private void localTest() {
        int[] testThreads = new int[]{1, 2, 4, 8, 16, 32, 64};
        repeatNum = 5;
        for (Style s : Style.values()) {
            style = s;
            for (int n : testThreads) {
                numThreads = n;
                Log.i("IBench", String.format("Test Start Style: %s Thread : %d", s.name(), numThreads));
                for (int j = 0; j < repeatNum; j++) {
                    startTest();
                }

                Log.i("IBench", String.format("Test End Style: %s Thread : %d", s.name(), numThreads));
            }

        }
    }

    private void startWithPM() {

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

    private void startTest() {
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
    }
}

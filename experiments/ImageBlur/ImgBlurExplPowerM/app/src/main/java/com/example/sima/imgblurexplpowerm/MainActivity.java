package com.example.sima.imgblurexplpowerm;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    int w, h;
    static int pieceWidth;
    int numThreads = 2;
    //Worker[] pool;
    long startTime;
    long duration;

    Bitmap orgBitmap;
    Bitmap bitmap;
    Bitmap placeholder;
    Canvas canvas;
    LinearLayout layout;
    Bitmap[] bmpArray; // = new Bitmap[numThreads];
    Object lock1 = new Object();
    TextView view;
    public int repeatNum = 50;
    public enum Style {
        Explict, ForkJoin, AsyncTask, Executor, HandlerR, HandlerM
    }

    Style style;

    public Bitmap createPlaceholder() {

        Bitmap placeholderObj = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        return placeholderObj;
    }

    public  void splitImage() {
        pieceWidth = w / numThreads;
        for (int i = 0; i < bmpArray.length; i++) {
            bmpArray[i] = Bitmap.createBitmap(bitmap, i * pieceWidth, 0, pieceWidth, h);
        }
    }

    public void copyPartToPlaceholder(Bitmap smallBitmap, int index) {

        canvas.drawBitmap(smallBitmap, index * pieceWidth, 0, null);
    }


    private void startExplict() {
        bitmap = orgBitmap.copy(orgBitmap.getConfig(), true);
        Explicit w1 = new Explicit(this);
        w1.start();
        try {
            w1.join();
        } catch (Exception e) {
            Log.e("IBench", "exception", e);
        }
        layout.setBackground(new BitmapDrawable(placeholder));
    }

    private void startForkJoin() {
        Bitmap dest;
        bitmap = orgBitmap.copy(Bitmap.Config.RGB_565, true);
        dest = orgBitmap.copy(Bitmap.Config.RGB_565, true);


        int[] src = new int[w * h];
        int[] dst = new int[w * h];


        bitmap.getPixels(src, 0, w, 0, 0, w, h);
        ForkBlur fb = new ForkBlur(src, 0, src.length, dst);
        ForkJoinPool pool = new ForkJoinPool(numThreads);
        pool.invoke(fb);
        pool.shutdown();
        try {
            pool.awaitTermination(500, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e("IBench", "exception", e);
        }
        dest.setPixels(dst, 0, w, 0, 0, w, h);

        layout.setBackground(new BitmapDrawable(dest));
    }

    private void startAsyncTask() {
        bitmap = orgBitmap.copy(orgBitmap.getConfig(), true);
        AsyncTaskBlur task = new AsyncTaskBlur(this);
        task.execute();

    }

    private void startExecutor() {
        ExecutorService pool;
        pool = Executors.newFixedThreadPool(numThreads);
        bitmap = orgBitmap.copy(orgBitmap.getConfig(), true);
        Thread task = new Thread(new ExecutorBlur(this, pool));
        task.start();
        try {
            task.join();
        } catch (InterruptedException e) {
            Log.e("IBench", "exception", e);
        }
        layout.setBackground(new BitmapDrawable(placeholder));
    }

    private void startHandlerR() {
        bitmap = orgBitmap.copy(orgBitmap.getConfig(), true);
        HandlerR handle = new HandlerR(this);
        Thread t = new Thread(handle);
        t.start();
        try {
            t.join();
        } catch (Exception e) {
            Log.e("IBench", "exception", e);
        }
        layout.setBackground(new BitmapDrawable(placeholder));
    }

    private void startHandlerM() {
        bitmap = orgBitmap.copy(orgBitmap.getConfig(), true);
        Handler handler = new Handler();
        Thread connectionThread = new Thread(new HandlerM(this, handler));
        connectionThread.start();
        try {
            connectionThread.join();
        } catch (InterruptedException e) {
            Log.e("IBench", "exception", e);
        }

        layout.setBackground(new BitmapDrawable(placeholder));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        layout = (LinearLayout) findViewById(R.id.layout);
        view = (TextView) findViewById(R.id.textView);

        orgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bjjwallpaper);
        w = orgBitmap.getWidth();
        h = orgBitmap.getHeight();


        startWithPM();

    }

    private void localTest(){
        int[] testThreads = new int[] {2,4, 8};
        repeatNum = 5;
        for (Style s : Style.values()) {
            for (int n : testThreads) {
                numThreads = n;
                style = s;

                Log.i("IBench", String.format("Test Start Style: %s Thread : %d", style.name(), numThreads));
                startTest();

                Log.i("IBench", String.format("Test End Style: %s Thread : %d", style.name(), numThreads));
            }

        }
    }

    private void startWithPM(){

        PowerMonitor powerMonitor = new PowerMonitor();
       powerMonitor.startMonitoring();
        while (true) {
            String targetString = powerMonitor.getTarget();

            if (targetString.equalsIgnoreCase("Done")) break;

            String[] target = targetString.split(" ");

            try{
                style = Style.valueOf(target[0]);
                numThreads = Integer.parseInt(target[1]);
            }catch (Exception e){
                Log.d("IBench", String.format("Erro during parse String %s, Style : %s ", targetString, target[0]) );
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
            SystemClock.sleep(1000);
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

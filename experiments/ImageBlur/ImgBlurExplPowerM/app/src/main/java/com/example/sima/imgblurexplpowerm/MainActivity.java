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
import android.os.Environment;
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

    int[] src, dst;
    int w, h;
    int pieceWidth;
    int numThreads = 16;
//    Worker[] pool;
    long startTime;
    Bitmap orgBitmap;
    Bitmap bitmap;
    Bitmap dest;
    LinearLayout layout;
    MainActivity mainActivity = new MainActivity();

    public enum Style {
        Explict, ForkJoin, AsyncTask, Executor, HandlerR, HandlerM
    }

    Style style;

    void doJob(){
        System.out.println("Start time: " + String.valueOf(startTime));
        layout = (LinearLayout)findViewById(R.id.linearLayout);

        String bitmapPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/redrose-2.jpg";
        orgBitmap = BitmapFactory.decodeFile(bitmapPath);
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
//        bitmap = orgBitmap.copy(orgBitmap.getConfig(), true);
//        Explicit w1 = new Explicit(this);
        doJob();
        Worker[] pool = new Worker[numThreads];


        for(int i=0; i<numThreads; i++){
            pool[i] = new Worker(mainActivity, src, i*pieceWidth, w*h, dst);
            pool[i].start();
        }

        for(int j=0; j<numThreads; j++){
            if(pool[j].isAlive()){
                try {
                    pool[j].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        dest.setPixels(dst, 0, w, 0, 0, w, h);

        layout.setBackground(new BitmapDrawable(dest));
        System.out.println("Duration: "+ (System.currentTimeMillis()-startTime));
//        w1.start();
//        try {
//            w1.join();
//        } catch (Exception e) {
//            Log.e("IBench", "exception", e);
//        }
//        layout.setBackground(new BitmapDrawable(placeholder));
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

    boolean AsyncCheckDone(AsyncTaskBlur[] asyncTasks )  {
        for (AsyncTaskBlur a : asyncTasks) {
            if (!a.bluer.done)
                return false;
        }
        return true;
    }

    private void startAsyncTask() {
        bitmap = orgBitmap.copy(orgBitmap.getConfig(), true);
        bmpArray = new Bitmap[numThreads];
        splitImage();

        AsyncTaskBlur[] asyncTasks = new AsyncTaskBlur[numThreads];
        for
                (int j = 0; j < numThreads; j++) {
            asyncTasks[j] = new AsyncTaskBlur(this, pieceWidth, h, j, bmpArray[j]);
            asyncTasks[j].executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }


        synchronized (lock1) {
            while (!AsyncCheckDone(asyncTasks)) {
                try {
                    lock1.wait();
                } catch (InterruptedException e) {
                    Log.e("IBench", "exception", e);
                }
            }
        }


        Bitmap placeholder = createPlaceholder();
        canvas = new Canvas(placeholder);
        for (int i = 0; i < numThreads; i++) {
            copyPartToPlaceholder(bmpArray[i], i);
        }

        layout.setBackground(new BitmapDrawable(placeholder));
    }

    private void startExecutor() {

        doJob();
        ExecutorService pool;
        pool = Executors.newFixedThreadPool(numThreads);
        for (int j = 0; j < numThreads; j++) {
            pool.submit(new Worker(mainActivity, src, j*pieceWidth, w*h, dst));
        }

        pool.shutdown();
        try {
            pool.awaitTermination(500, TimeUnit.SECONDS);
        }catch(Exception e){
            e.printStackTrace();
        }

        dest.setPixels(dst, 0, w, 0, 0, w, h);

        layout.setBackground(new BitmapDrawable(dest));
        System.out.println("Duration: " + (System.currentTimeMillis() - startTime));
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
        //localTest();
    }

    private void localTest() {
        int[] testThreads = new int[]{2, 4, 8, 16, 32, 64};
        repeatNum = 5;
        for (Style s : Style.values()) {
            for (int n : testThreads) {
                numThreads = n;
                Log.i("IBench", String.format("Test Start Style: %s Thread : %d", style.name(), numThreads));
                for (int j = 0; j < repeatNum; j++) {
                    startTest();
                }

                Log.i("IBench", String.format("Test End Style: %s Thread : %d", style.name(), numThreads));
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

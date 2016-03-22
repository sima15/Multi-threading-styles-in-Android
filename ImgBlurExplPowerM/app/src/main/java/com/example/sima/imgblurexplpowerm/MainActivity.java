package com.example.sima.imgblurexplpowerm;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    int w, h;
    int pieceWidth;
    int numThreads = 2;
    Worker[] pool;
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

    public Bitmap createPlaceholder() {

        Bitmap placeholderObj = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        return placeholderObj;
    }

    public void splitImage() {
        pieceWidth = w/numThreads;
        for (int i = 0; i < bmpArray.length; i++) {
            bmpArray[i] = Bitmap.createBitmap(bitmap, i*pieceWidth, 0,pieceWidth, h);
        }
    }

    public void copyPartToPlaceholder(Bitmap smallBitmap, int index) {

        canvas.drawBitmap(smallBitmap, index * pieceWidth, 0, null);
    }

    boolean checkDone() {
        for (Worker a : pool) {
            if (!a.done) return false;
        }
        return true;
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

        PowerMonitor.startMonitoring();

        //Explict
        if(false) {
            bitmap = orgBitmap.copy(orgBitmap.getConfig(), true);
            Explicit w1 = new Explicit(this);
            w1.start();
            try {
                w1.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
            layout.setBackground(new BitmapDrawable(placeholder));
        }
        //Fork Join
        if(true) {
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
                e.printStackTrace();
            }
            dest.setPixels(dst, 0, w, 0, 0, w, h);

            layout.setBackground(new BitmapDrawable(dest));
        }


        PowerMonitor.saveMonitoring(String.format("ImageBlur%d.csv", numThreads));

    }

}

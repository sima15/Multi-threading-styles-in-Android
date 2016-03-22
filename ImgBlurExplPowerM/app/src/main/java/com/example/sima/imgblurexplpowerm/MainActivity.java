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

        Explicit w1= new Explicit(this);
        w1.start();
        try {
            w1.join();
        }catch(Exception e){
            e.printStackTrace();
        }
        layout.setBackground(new BitmapDrawable(placeholder));
    }

}

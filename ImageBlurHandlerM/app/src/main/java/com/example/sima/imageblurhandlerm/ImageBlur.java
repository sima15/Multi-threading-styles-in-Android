package com.example.sima.imageblurhandlerm;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class ImageBlur extends AppCompatActivity {

//    final static int SET_PROGRESS_BAR_VISIBILITY = 0;
//    final static int PROGRESS_UPDATE = 1;
//    final static int SET_RESULT = 2;

    int w, h;
    int numThreads= 4;
    int logThreads ;
    Worker[] pool;
    long startTime;
    int pieceWidth;

//    Bitmap orgBitmap;
    Bitmap bitmap;
    Bitmap placeholder;
    Bitmap[] bmpArray= new Bitmap[numThreads];
    Object lock1 = new Object();
    Canvas canvas;
    LinearLayout layout;

    URLConnection myUrlConnection;
    URLConnection endConnection;
    URL endMonsoon;

    Handler handler = new Handler();


    public Bitmap createPlaceholder() {
            Bitmap placeHldBmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            return placeHldBmap;
    }

    public void splitImage() {
        pieceWidth = w/numThreads;
        for (int i = 0; i < bmpArray.length; i++) {
            bmpArray[i] = Bitmap.createBitmap(bitmap, i*pieceWidth, 0, pieceWidth,h);
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
        setContentView(R.layout.activity_image_blur);

        startTime = System.currentTimeMillis();
        System.out.println("Start time: "+ startTime);
        layout = (LinearLayout)findViewById(R.id.layout);

        Context context = getApplicationContext();
        ConnectivityManager check = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] info = check.getAllNetworkInfo();

        for (int i = 0; i < info.length; i++) {
            if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                Toast.makeText(context, "Internet is connected", Toast.LENGTH_SHORT).show();
            }
        }
        NetworkHandler connectionThread = new NetworkHandler();
        //Thread connectionThread = new Thread(new MyHandler(handler));
        connectionThread.start();
        try {
            connectionThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Duration: "+(System.currentTimeMillis()-startTime));
    }


    class NetworkHandler extends Thread {

        public void run (){
            try {
                Log.d("Debug Info", "Send Power Monitoring Start Request");

                //URL startMonsoon = new URL("http://129.123.7.199:8000/start");
                URL startMonsoon = new URL("http://google.com");
                myUrlConnection = startMonsoon.openConnection();

            }
            catch(Exception e) {
                e.printStackTrace();
            }


            for( logThreads=0; logThreads<6; logThreads++)
            {
                numThreads = (int) Math.pow(2,  logThreads);
                try {
                    myUrlConnection.connect();
                    Thread t1 = new Thread(new MyHandler(handler));
                    t1.start();
                    try {
                        t1.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    Log.d("Debug Info", "Send Power Monitoring Save Request");
                    /*
                    String urlStr = "sima1.pt5";
                   // String urlStr = String.format("http://129.123.7.199:8000/save?file=%s",numThreads);
                    endMonsoon = new URL(urlStr);
                    endConnection = endMonsoon.openConnection();
                    endConnection.connect();
                    */
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public class MyHandler implements  Runnable {

        Handler handler = new Handler();
        public MyHandler(Handler handler){
            this.handler = handler;
        }
        @Override
        public void run() {

            //String bitmapPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()+ "/bjjwallpaper.jpg";
            //bitmap = BitmapFactory.decodeFile(bitmapPath);

            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bjjwallpaper);

            w = bitmap.getWidth();
            h = bitmap.getHeight();

            for (int j = 1; j <= 15; j++) {
                splitImage();
                pool = new Worker[numThreads];


                for (int i = 0; i < numThreads; i++) {
                    pool[i] = new Worker(pieceWidth, h, i, bmpArray[i]);
                    pool[i].start();
                }

                synchronized (lock1) {
                    while (!checkDone()) {
                        try {
                            lock1.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                placeholder = createPlaceholder();
                canvas = new Canvas(placeholder);
                for (int i = 0; i < numThreads; i++) {
                    copyPartToPlaceholder(bmpArray[i], i);
                }
            }
            layout.setBackground(new BitmapDrawable(placeholder));
        }
    }

    public class Worker extends Thread {
            int w, h;
            int radius = 35;
            boolean done = false;
            int index;
            Bitmap bitmap = null;//Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

            Worker(int w, int h, int index, Bitmap orgBmp) {
                this.w = w;
                this.h = h;
                this.index = index;
                bitmap = Bitmap.createBitmap(orgBmp, 0, 0, orgBmp.getWidth(), orgBmp.getHeight());
            }

            @Override
            public void run() {
                int[] pix = new int[w * h];
                bitmap.getPixels(pix, 0, w, 0, 0, w, h);

                int wm = w - 1;
                int hm = h - 1;
                int wh = w * h;
                int div = radius + radius + 1;

                int r[] = new int[wh];
                int g[] = new int[wh];
                int b[] = new int[wh];
                int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
                int vmin[] = new int[Math.max(w, h)];

                int divsum = (div + 1) >> 1;
                divsum *= divsum;
                int dv[] = new int[256 * divsum];
                for (i = 0; i < 256 * divsum; i++) {
                    dv[i] = (i / divsum);
                }

                yw = yi = 0;

                int[][] stack = new int[div][3];
                int stackpointer;
                int stackstart;
                int[] sir;
                int rbs;
                int r1 = radius + 1;
                int routsum, goutsum, boutsum;
                int rinsum, ginsum, binsum;

                for (y = 0; y < h; y++) {
                    rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
                    for (i = -radius; i <= radius; i++) {
                        p = pix[yi + Math.min(wm, Math.max(i, 0))];
                        sir = stack[i + radius];
                        sir[0] = (p & 0xff0000) >> 16;
                        sir[1] = (p & 0x00ff00) >> 8;
                        sir[2] = (p & 0x0000ff);
                        rbs = r1 - Math.abs(i);
                        rsum += sir[0] * rbs;
                        gsum += sir[1] * rbs;
                        bsum += sir[2] * rbs;
                        if (i > 0) {
                            rinsum += sir[0];
                            ginsum += sir[1];
                            binsum += sir[2];
                        } else {
                            routsum += sir[0];
                            goutsum += sir[1];
                            boutsum += sir[2];
                        }
                    }
                    stackpointer = radius;

                    for (x = 0; x < w; x++) {

                        r[yi] = dv[rsum];
                        g[yi] = dv[gsum];
                        b[yi] = dv[bsum];

                        rsum -= routsum;
                        gsum -= goutsum;
                        bsum -= boutsum;

                        stackstart = stackpointer - radius + div;
                        sir = stack[stackstart % div];

                        routsum -= sir[0];
                        goutsum -= sir[1];
                        boutsum -= sir[2];

                        if (y == 0) {
                            vmin[x] = Math.min(x + radius + 1, wm);
                        }
                        p = pix[yw + vmin[x]];

                        sir[0] = (p & 0xff0000) >> 16;
                        sir[1] = (p & 0x00ff00) >> 8;
                        sir[2] = (p & 0x0000ff);

                        rinsum += sir[0];
                        ginsum += sir[1];
                        binsum += sir[2];

                        rsum += rinsum;
                        gsum += ginsum;
                        bsum += binsum;

                        stackpointer = (stackpointer + 1) % div;
                        sir = stack[(stackpointer) % div];

                        routsum += sir[0];
                        goutsum += sir[1];
                        boutsum += sir[2];

                        rinsum -= sir[0];
                        ginsum -= sir[1];
                        binsum -= sir[2];

                        yi++;
                    }
                    yw += w;
                }
                for (x = 0; x < w; x++) {
                    rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
                    yp = -radius * w;
                    for (i = -radius; i <= radius; i++) {
                        yi = Math.max(0, yp) + x;

                        sir = stack[i + radius];

                        sir[0] = r[yi];
                        sir[1] = g[yi];
                        sir[2] = b[yi];

                        rbs = r1 - Math.abs(i);

                        rsum += r[yi] * rbs;
                        gsum += g[yi] * rbs;
                        bsum += b[yi] * rbs;

                        if (i > 0) {
                            rinsum += sir[0];
                            ginsum += sir[1];
                            binsum += sir[2];
                        } else {
                            routsum += sir[0];
                            goutsum += sir[1];
                            boutsum += sir[2];
                        }

                        if (i < hm) {
                            yp += w;
                        }
                    }
                    yi = x;
                    stackpointer = radius;
                    for (y = 0; y < h; y++) {
                        // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                        pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                        rsum -= routsum;
                        gsum -= goutsum;
                        bsum -= boutsum;

                        stackstart = stackpointer - radius + div;
                        sir = stack[stackstart % div];

                        routsum -= sir[0];
                        goutsum -= sir[1];
                        boutsum -= sir[2];

                        if (x == 0) {
                            vmin[y] = Math.min(y + r1, hm) * w;
                        }
                        p = x + vmin[y];

                        sir[0] = r[p];
                        sir[1] = g[p];
                        sir[2] = b[p];

                        rinsum += sir[0];
                        ginsum += sir[1];
                        binsum += sir[2];

                        rsum += rinsum;
                        gsum += ginsum;
                        bsum += binsum;

                        stackpointer = (stackpointer + 1) % div;
                        sir = stack[stackpointer];

                        routsum += sir[0];
                        goutsum += sir[1];
                        boutsum += sir[2];

                        rinsum -= sir[0];
                        ginsum -= sir[1];
                        binsum -= sir[2];

                        yi += w;
                    }
                }

                bitmap.setPixels(pix, 0, w, 0, 0, w, h);
                bmpArray[index] = bitmap;
                done = true;

                synchronized (lock1) {
                    lock1.notify();
                }
            }

    }
}



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
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ImageBlur extends AppCompatActivity {

//    final static int SET_PROGRESS_BAR_VISIBILITY = 0;
//    final static int PROGRESS_UPDATE = 1;
    final static int SET_RESULT = 2;

    int w, h;
    int numThreads= 4;
    Worker[] pool;
    long startTime;
    int pieceWidth;
    TextView view;

    Bitmap bitmap;
    Bitmap placeholder;
    Bitmap[] bmpArray= new Bitmap[numThreads];
    Object lock1 = new Object();
    Canvas canvas;
    LinearLayout layout;


    URL endMonsoon;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
//                case SET_PROGRESS_BAR_VISIBILITY: {
//                    mProgressBar.setVisibility((Integer) msg.obj);
//                    break;
//                }
//                case PROGRESS_UPDATE: {
//                    mProgressBar.setProgress((Integer) msg.obj);
//                    break;
//                }
                case SET_RESULT: {
                    view.setText((String) msg.obj);
                    break;
                }
            }
        }

    };

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
        view = (TextView) findViewById(R.id.textView);

        Context context = getApplicationContext();
        ConnectivityManager check = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] info = check.getAllNetworkInfo();

        for (int i = 0; i < info.length; i++) {
            if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                Toast.makeText(context, "Internet is connected", Toast.LENGTH_SHORT).show();
            }
        }
//        NetworkHandler connectionThread = new NetworkHandler();
        Thread connectionThread = new Thread(new MyHandler(handler));
        connectionThread.start();
        try {
            connectionThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Duration: "+(System.currentTimeMillis()-startTime));
    }


    class NetworkHandler extends Thread {
        private final String USER_AGENT = "Mozilla/5.0";
        public void run (){
            System.out.println("Hello from inside the thread");

            System.out.println("Starting the connection");
            URL startMonsoon = null;
            try {
                startMonsoon = new URL("http://129.123.7.199:8000/start");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            for(int i=0; i<=6; i++) {
                try {

                    HttpURLConnection con = (HttpURLConnection) startMonsoon.openConnection();
                    con.setRequestMethod("GET");
                    con.setRequestProperty("User-Agent", USER_AGENT);
                    int responseCode = con.getResponseCode();
                    System.out.println("\nSending 'GET' request to URL : " + startMonsoon);
                    System.out.println("Response Code : " + responseCode);
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(con.getInputStream()));
                    // String inputLine;
                    StringBuffer response = new StringBuffer();
                    System.out.println(response.toString());

                    startTime = System.currentTimeMillis();
                    System.out.println("Started...");
                    numThreads =(int) Math.pow(2, i);
                    bmpArray = new Bitmap[numThreads];
                    //Handler hndlr = new JobHandler();
                    Thread hndlr = new Thread(new MyHandler(handler));
                    hndlr.start();
                    hndlr.join();

                    in.close();


                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                long duration = System.currentTimeMillis()-startTime;
                System.out.println("Duration: "+ duration);
                try {
                    StringBuilder sb = new StringBuilder("http://129.123.7.199:8000/save?file=ImageBlurExplicit");

                    sb.append(String.format("%d%s\n", i, ".pt5"));
                    System.out.println(sb);
                    //String urlString = "http://129.123.7.199:8000/save?file=ImageBlurExplicit.pt5";
                    System.out.println("trying to end the connection");
                    endMonsoon = new URL(String.valueOf(sb));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                try {
                    HttpURLConnection endCon = (HttpURLConnection) endMonsoon.openConnection();
                    endCon.setRequestMethod("GET");
                    endCon.setRequestProperty("User-Agent", USER_AGENT);
                    int responseCode2 = endCon.getResponseCode();
                    System.out.println("\nSending 'GET' request to URL : " + endMonsoon);
                    System.out.println("Response Code : " + responseCode2);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Program finished");
        }
    }
    public class MyHandler implements  Runnable {
        long startTime = System.currentTimeMillis();
        Message msg = null;

        public MyHandler(Handler han){
            handler = han;
        }
        @Override
        public void run() {

            String bitmapPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()+ "/redrose-2.jpg";
//            String bitmapPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()+ "/downloadfile.jpg";
            bitmap = BitmapFactory.decodeFile(bitmapPath);

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
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            msg = handler.obtainMessage(SET_RESULT, String.valueOf(duration));
            handler.sendMessage(msg);
        }
    }

    public class Worker extends Thread {
            int w, h;
            int radius = 35;
            boolean done = false;
            int index;
            Bitmap bitmap = null;

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



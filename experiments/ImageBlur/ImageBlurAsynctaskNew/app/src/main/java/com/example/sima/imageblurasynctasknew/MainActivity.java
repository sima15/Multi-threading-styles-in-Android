package com.example.sima.imageblurasynctasknew;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

/**
*
* @author Sima Mehri
*/

public class MainActivity extends AppCompatActivity {

    long startTime;
    int w, h;
    int pieceWidth;
    int numThreads = 32;

    Bitmap orgBitmap;
    Bitmap bitmap;
    Bitmap placeholder;
    Bitmap[] bmpArray = new Bitmap[numThreads];

    BlurTask[] tasks;
    Canvas canvas;
    LinearLayout layout;
    boolean[] done = new boolean[numThreads];
    Object lock1 = new Object();

    public Bitmap createPlaceholder() {
        Bitmap placeHldBmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        return placeHldBmap;
    }

    public void splitImage() {
        pieceWidth = w/numThreads;
        for (int i = 0; i < bmpArray.length; i++) {
            bmpArray[i] = Bitmap.createBitmap(bitmap, i*pieceWidth, 0, pieceWidth, h);
        }
    }

    public void copyPartToPlaceholder( Bitmap smallBitmap, int index) {
        canvas.drawBitmap(smallBitmap, index * pieceWidth, 0, null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startTime = System.currentTimeMillis();
        System.out.println("Start time: " + String.valueOf(startTime));

//        for(int i=0; i<8; i++)
        try {
            doJob();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void doJob() throws InterruptedException {
        layout = (LinearLayout) findViewById(R.id.layout);

        String bitmapPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/rose-06.jpg";
        orgBitmap = BitmapFactory.decodeFile(bitmapPath);
        bitmap = orgBitmap.copy(orgBitmap.getConfig(), true);

        w = bitmap.getWidth();
        h = bitmap.getHeight();

        splitImage();

        tasks = new BlurTask[numThreads];
        for(int i=0; i<numThreads; i++) {
            tasks[i] = new BlurTask(i);
            tasks[i].execute();
        }

//        Thread.sleep(10000);
        for(int k=numThreads-1; k>=0; k--){
            synchronized (lock1) {
                while (!done[k]) try {

                    Log.d("Debug", "Main waiting for "+ tasks[k]);
                    lock1.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        placeholder = createPlaceholder();
        canvas = new Canvas(placeholder);

        for (int j = 0; j < numThreads; j++) {
            copyPartToPlaceholder(bmpArray[j], j);
        }
        layout.setBackground(new BitmapDrawable(placeholder));
        long endTime = System.currentTimeMillis();
        System.out.println("End time: " + endTime);
        System.out.println("Duration: " + String.valueOf(endTime - startTime));
    }

    class BlurTask extends AsyncTask<Void, Void, Void> {
        int index;
        public BlurTask(int i){
            index = i;
        }

        @Override
        protected Void doInBackground(Void... v) {

//            Worker worker = new Worker(pieceWidth, h, index, bmpArray[index]);
//            worker.start();
            new Worker(pieceWidth, h, index, bmpArray[index]).start();
            return null;
        }

    }
    public class Worker extends  Thread {
        int w, h;
        int radius = 35;
        int index;
        Bitmap bitmap = null;

        Worker( int w, int h, int index, Bitmap orgBmp) {
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
            Log.d("Debug",  tasks[index] + " finished its job");
            done[index] = true;

            synchronized (lock1){
                lock1.notify();
                Log.d("Debug", tasks[index] + " notified Main");
            }
        }

    }

}

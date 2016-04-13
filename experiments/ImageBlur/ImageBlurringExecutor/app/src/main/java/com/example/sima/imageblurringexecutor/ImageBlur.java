package com.example.sima.imageblurringexecutor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Sima Mehri
 */

public class ImageBlur extends AppCompatActivity {

    int[] src, dst;
    int w, h;
    int pieceWidth;
    int numThreads = 2;
    ExecutorService pool;
    long startTime;
    Bitmap orgBitmap;
    Bitmap bitmap;
    Bitmap dest;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_blur);

        startTime = System.currentTimeMillis();
       for (int i=0; i<100; i++)
           doJob();

    }

    void doJob(){
        System.out.println("Start time: " + String.valueOf(startTime));
        LinearLayout layout = (LinearLayout) findViewById(R.id.layout);



        String bitmapPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/redrose-2.jpg";
        orgBitmap = BitmapFactory.decodeFile(bitmapPath);
        bitmap = orgBitmap.copy(Bitmap.Config.RGB_565, true);
        dest = orgBitmap.copy(Bitmap.Config.RGB_565, true);

        w = bitmap.getWidth();
        h = bitmap.getHeight();
        pieceWidth = w*h/numThreads;

        src = new int[w * h];
        dst = new int[w * h];
        bitmap.getPixels(src, 0, w, 0, 0, w, h);


        pool = Executors.newFixedThreadPool(numThreads);

        for (int j = 0; j < numThreads; j++) {
            pool.submit(new Worker(src, j*pieceWidth , pieceWidth, dst));
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


    public class Worker implements  Runnable {

        int[] mSource;
        int mStart;
        int mLength;
        int[] mDestination;

        // Processing window size; should be odd.
        private int mBlurWidth = 15;

        public Worker(int[] src, int start, int length, int[] dst) {
            mSource = src;
            mStart = start;
            mLength = length;
            mDestination = dst;
        }

        @Override
        public void run() {

            int sidePixels = (mBlurWidth - 1) / 2;
            for (int index = mStart; index < mStart + mLength; index++) {
                // Calculate average.
                float rt = 0, gt = 0, bt = 0;
                for (int mi = -sidePixels; mi <= sidePixels; mi++) {
                    int mindex = Math.min(Math.max(mi + index, 0),
                            mSource.length - 1);
                    int pixel = mSource[mindex];
                    rt += (float) ((pixel & 0x00ff0000) >> 16)
                            / mBlurWidth;
                    gt += (float) ((pixel & 0x0000ff00) >> 8)
                            / mBlurWidth;
                    bt += (float) ((pixel & 0x000000ff) >> 0)
                            / mBlurWidth;
                }

                // Reassemble destination pixel.
                int dpixel = (0xff000000) |
                        (((int) rt) << 16) |
                        (((int) gt) << 8) |
                        (((int) bt) << 0);
                mDestination[index] = dpixel;
                dst[index] = dpixel;
            }
        }

    }
}

package com.example.sima.imageblurhandlermnew;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;

/**
 * @author Sima Mehri
 */
public class MainActivity extends AppCompatActivity {

    int[] src, dst;
    int w, h;
    long startTime;
    int numThreads = 8;
    Worker[] pool;
    Bitmap bitmap;
    Bitmap orgBitmap;
    Bitmap dest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startTime = System.currentTimeMillis();
        for(int i=0; i<200; i++)
            doJob();
    }

    void doJob() {
        System.out.println("Start time: " + startTime);
        LinearLayout layout = (LinearLayout) findViewById(R.id.layout);

        String bitmapPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/redrose-2.jpg";
        orgBitmap = BitmapFactory.decodeFile(bitmapPath);
        bitmap = orgBitmap.copy(Bitmap.Config.RGB_565, true);
        dest = orgBitmap.copy(Bitmap.Config.RGB_565, true);

        w = bitmap.getWidth();
        h = bitmap.getHeight();
        int pieceWidth = w*h/numThreads;

        src = new int[w * h];
        dst = new int[w * h];
        bitmap.getPixels(src, 0, w, 0, 0, w, h);

        Handler handler = new Handler();
        pool = new Worker[numThreads];

        for(int i=0; i<numThreads; i++){
            pool[i] = new Worker(src, i*pieceWidth, pieceWidth, dst, handler);
            pool[i].start();
        }

        for(int j=0; j<numThreads; j++){
            if(pool[j].isAlive()) {
                try{
                pool[j].join();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        dest.setPixels(dst, 0, w, 0, 0, w, h);

//        }
        layout.setBackground(new BitmapDrawable(dest));
        System.out.println("Duration: " + (System.currentTimeMillis() - startTime));

    }

    public class Worker extends Thread {
        int[] mSource;
        int mStart;
        int mLength;
        int[] mDestination;
        Handler hand;

        // Processing window size; should be odd.
        private int mBlurWidth = 15;

        public Worker(int[] src, int start, int length, int[] dst, Handler handler) {
            mSource = src;
            mStart = start;
            mLength = length;
            mDestination = dst;
            hand = handler;
        }

        public void run() {
            Message msg;
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
                if (index < mDestination.length) {
                    mDestination[index] = dpixel;
                    dst[index] = dpixel;
                }
            }
        }
    }
}

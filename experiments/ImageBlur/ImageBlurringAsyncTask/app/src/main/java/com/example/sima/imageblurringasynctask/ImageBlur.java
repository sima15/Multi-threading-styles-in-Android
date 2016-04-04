package com.example.sima.imageblurringasynctask;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;

import java.util.concurrent.ExecutionException;

/**
 *
 * @author Sima Mehri
 */

public class ImageBlur extends AppCompatActivity {

    int[] src, dst;
    int w, h;
    int pieceWidth;
    int numThreads = 16;
    Worker[] pool;
    long startTime;
    Bitmap orgBitmap;
    Bitmap bitmap;
    Bitmap dest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_blur);

        startTime = System.currentTimeMillis();
//        for(int i=0; i<35; i++) {
            doJob();
    }


    void doJob(){
        System.out.println("Start time: " + String.valueOf(startTime));
        LinearLayout layout = (LinearLayout)findViewById(R.id.layout);

        String bitmapPath = Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/redrose-2.jpg";
        orgBitmap = BitmapFactory.decodeFile(bitmapPath);
        bitmap = orgBitmap.copy(Bitmap.Config.RGB_565, true);
        dest = orgBitmap.copy(Bitmap.Config.RGB_565, true);

        w = bitmap.getWidth();
        h = bitmap.getHeight();
        pieceWidth = w/numThreads;

        src = new int[w * h];
        dst = new int[w * h];
        bitmap.getPixels(src, 0, w, 0, 0, w, h);

        pool = new Worker[numThreads];
        for (int j = 0; j < numThreads; j++) {
            pool[j] = new Worker(src, j * pieceWidth, w * h, dst);
            pool[j].executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        for(int j=0; j<numThreads; j++){
            try {
                pool[j].get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        dest.setPixels(dst, 0, w, 0, 0, w, h);

        layout.setBackground(new BitmapDrawable(dest));
        System.out.println("Duration: "+ (System.currentTimeMillis()-startTime));

    }
    class Worker extends AsyncTask<Void, Void, Void> {
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
        protected Void doInBackground(Void... v) {
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
            return null;
        }
    }

}


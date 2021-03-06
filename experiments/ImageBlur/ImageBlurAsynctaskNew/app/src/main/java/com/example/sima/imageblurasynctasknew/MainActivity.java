package com.example.sima.imageblurasynctasknew;

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

public class MainActivity extends AppCompatActivity {

    int[] src;
    static int[] dst;
    int w, h;
    int pieceWidth;
    int numThreads = 128;
    Worker[] pool;
    long startTime;

    Bitmap orgBitmap;
    Bitmap bitmap;
    Bitmap dest;
    LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startTime = System.currentTimeMillis();
        System.out.println("Start time: " + String.valueOf(startTime));


        for(int i=0; i<25; i++)
        try {
            doJob();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void doJob() throws InterruptedException {
        layout = (LinearLayout) findViewById(R.id.layout);

        String bitmapPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/redrose-2.jpg";
        orgBitmap = BitmapFactory.decodeFile(bitmapPath);
//        orgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.redrose);
        bitmap = orgBitmap.copy(orgBitmap.getConfig(), true);
        dest = orgBitmap.copy(Bitmap.Config.RGB_565, true);

        w = bitmap.getWidth();
        h = bitmap.getHeight();
        pieceWidth = w*h/numThreads;

        src = new int[w * h];
        dst = new int[w * h];
        bitmap.getPixels(src, 0, w, 0, 0, w, h);

        pool = new Worker[numThreads];
        for (int j = 0; j < numThreads; j++) {
            pool[j] = new Worker(src, j * pieceWidth, pieceWidth, dst);
            pool[j].executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }


        for(int j=0; j<numThreads; j++) {
            try {
                pool[j].get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        dest.setPixels(dst, 0, w, 0, 0, w, h);

        for(int i=0; i<numThreads; i++){
            pool[i] = null;
        }
        layout.setBackground(new BitmapDrawable(dest));
        System.out.println("Duration: "+ (System.currentTimeMillis()-startTime));
    }

    static  class Worker extends AsyncTask<Void, Void, Void> {
        int[] mSource;
        int mStart;
        int mLength;
        int[] mDestination;

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

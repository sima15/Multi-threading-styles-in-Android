package com.example.sima.imageblurforkjoin;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

/**
 * @author Sima Mehri
 */
public class ImageBlur extends AppCompatActivity {


    int[] src, dst;
    int w , h;
    long startTime;
    int numThreads =8;
    Bitmap bitmap;
    Bitmap orgBitmap;
    Bitmap dest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_blur);
        startTime = System.currentTimeMillis();

//        for(int i=0; i<800; i++)
            doJob();
    }

    void doJob(){
        System.out.println("Start time: "+ startTime);
        LinearLayout layout = (LinearLayout)findViewById(R.id.layout);

        String bitmapPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()+"/rose-06.jpg";
        orgBitmap = BitmapFactory.decodeFile(bitmapPath);
        bitmap = orgBitmap.copy(Bitmap.Config.RGB_565, true);
        dest = orgBitmap.copy(Bitmap.Config.RGB_565, true);
        w = bitmap.getWidth();
        h = bitmap.getHeight();

        src = new int[w * h];
        dst = new int[w * h];

//        for(int i=0; i<10; i++) {
            bitmap.getPixels(src, 0, w, 0, 0, w, h);
            // src = bitmap.

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

//        }
        layout.setBackground(new BitmapDrawable(dest));
        System.out.println("Duration: "+(System.currentTimeMillis()-startTime));
    }
    public class ForkBlur extends RecursiveAction {
         int[] mSource;
         int mStart;
         int mLength;
         int[] mDestination;

        // Processing window size; should be odd.
        private int mBlurWidth = 15;

        public ForkBlur(int[] src, int start, int length, int[] dst) {
            mSource = src;
            mStart = start;
            mLength = length;
            mDestination = dst;
        }

        protected void computeDirectly() {
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

        protected  int sThreshold = 100000;

        @Override
        protected void compute() {
            if (mLength < sThreshold) {
                computeDirectly();
                return;
            }

            int split = mLength / 2;

            invokeAll(new ForkBlur(mSource, mStart, split, mDestination),
                    new ForkBlur(mSource, mStart + split, mLength - split,
                            mDestination));
        }
    }
}

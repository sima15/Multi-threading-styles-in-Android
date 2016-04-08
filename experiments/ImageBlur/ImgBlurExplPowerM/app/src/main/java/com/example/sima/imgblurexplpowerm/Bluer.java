package com.example.sima.imgblurexplpowerm;

/**
 * Modified by Sima
 */

import android.os.Handler;

/**
 * Created by daehyeok on 2016. 3. 31..
 */

public class Bluer  {

    static public MainActivity mainActivity;
    int[] mSource;
    int mStart;
    int mLength;
    int[] mDestination;
    private int mBlurWidth = 15;
//    Handler handler;

    Bluer(MainActivity mainActivity, int[] src, int start, int length, int[] dst) {
        this.mainActivity = mainActivity;
        mSource = src;
        mStart = start;
        mLength = length;
        mDestination = dst;
    }
    Bluer(MainActivity mainActivity, int[] src, int start, int length, int[] dst, Handler han) {
        this.mainActivity = mainActivity;
        mSource = src;
        mStart = start;
        mLength = length;
        mDestination = dst;
//        handler = han;
    }


    public void doTask() {
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
//                mDestination[index] = dpixel;
                mainActivity.dst[index] = dpixel;
            }
        }
    }
}
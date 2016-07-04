package com.example.sima.imageforkjoin;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {


    Bitmap bitmap;
    int totalNum = 25;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Long startTime = System.currentTimeMillis();

        for(int i=0; i<200; i++) {
            createImage();
            createThreads();
        }

        Long duration = System.currentTimeMillis()-startTime;
        System.out.println("Duration= " + duration);
    }

    void createImage(){
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() +
//                "/ngc3582-1000.jpg";
                "/Image.jpg";

        bitmap = BitmapFactory.decodeFile(path);

    }

    void createThreads(){
        int numThreads = 128;
        ForkJoinPool pool = new ForkJoinPool(numThreads);
        pool.invoke(new ImageCopy(1,totalNum));
        pool.shutdown();
        try {
            pool.awaitTermination(500, TimeUnit.SECONDS);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public class ImageCopy extends RecursiveAction {
        private static  final long serialVersionUID = 6136927121059165206L;

        int lowerLimit;
        int upperLimit;
        int localCount;

        public ImageCopy(int l, int u){
            lowerLimit = l;
            upperLimit = u;
        }

        @Override
        protected void compute() {
            localCount = (upperLimit-lowerLimit)+1;
            if( localCount <= 5){
                computeDirectly(localCount);
                return;
            }
            else{
                int middle = lowerLimit+  (upperLimit-lowerLimit+1)/2;
                invokeAll(new ImageCopy(lowerLimit, +middle), new ImageCopy(middle+1, upperLimit ));
            }
        }

        public void computeDirectly(int localCount){

            Bitmap[] images = new Bitmap[localCount];
            for(int i=0; i<localCount; i++){
                Bitmap img = bitmap.copy(Bitmap.Config.RGB_565, true);
                images[i] = img;
            }
        }
    }

}

package com.example.sima.imageexecutor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {


    Bitmap bitmap;
    int chunk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Long startTime = System.currentTimeMillis();

        for(int i=0; i<200; i++) {
            createImage();
            try {
                createThreads();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Long duration = System.currentTimeMillis()-startTime;
        System.out.println("Duration= " + duration);
    }

    void createThreads() throws InterruptedException {

        int numThreads = 128;
        chunk = 25/numThreads;
        ImageCopy[] array = new ImageCopy[numThreads];
        ExecutorService pool = Executors.newFixedThreadPool(numThreads);

        for(int j=0; j<numThreads; j++){
            array[j] = new ImageCopy();
            pool.submit(array[j]);
        }

        pool.shutdown();
        pool.awaitTermination(500, TimeUnit.SECONDS);
    }
    void createImage(){
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() +
//                "/ngc3582-1000.jpg";
                    "/Image.jpg";

        bitmap = BitmapFactory.decodeFile(path);

    }

    public class ImageCopy implements  Runnable{

        public void run(){

            Bitmap[] images = new Bitmap[chunk];
            for(int i=1; i<chunk; i++){
                Bitmap img; // = Bitmap.createBitmap(1000, 1000, Bitmap.Config.RGB_565);
                img = bitmap.copy(Bitmap.Config.RGB_565, true);
                images[i] = img;
            }
        }
    }

}

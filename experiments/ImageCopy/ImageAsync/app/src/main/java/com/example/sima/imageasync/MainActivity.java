package com.example.sima.imageasync;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {


    Bitmap bitmap;
    int chunk;
    Object object = new Object();

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

        Long duration = (System.currentTimeMillis() - startTime);
        System.out.println("Duration= "+ duration);
    }


    void createThreads() throws InterruptedException {

        int numThreads = 16;
        chunk = 25/numThreads;
        ImageCopy[] array = new ImageCopy[numThreads];

        for(int j=0; j<numThreads; j++){
            array[j] = new ImageCopy();
            array[j].executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        for(int k=0; k<numThreads; k++){
            synchronized (object) {
                try{
                    while(!array[k].done)object.wait();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
    void createImage(){
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() +
//                "/ngc3582-1000.jpg";
                "/Image.jpg";

        bitmap = BitmapFactory.decodeFile(path);

    }

    public class ImageCopy extends AsyncTask<Void, Void, Void>{

        boolean done = false;

        @Override
        protected Void doInBackground(Void... params){

            Bitmap[] images = new Bitmap[chunk];
            for(int i=1; i<chunk; i++){
                Bitmap img ; //= Bitmap.createBitmap(1000, 1000, Bitmap.Config.RGB_565);
                img = bitmap.copy(Bitmap.Config.RGB_565, true);
                images[i] = img;
            }

            done = true;
            synchronized (object) {
                object.notify();
            }
            return null;
        }

    }

}

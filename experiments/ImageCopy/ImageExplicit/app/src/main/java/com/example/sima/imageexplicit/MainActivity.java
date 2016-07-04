package com.example.sima.imageexplicit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

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
            createThreads();
        }

        Long duration = (System.currentTimeMillis()-startTime);
        System.out.println("Duration= "+ duration);

    }

    void createThreads(){

        int numThreads = 256;
        chunk = 25/numThreads;
        ImageCopy[] array = new ImageCopy[numThreads];

        for(int j=0; j<numThreads; j++){
            array[j] = new ImageCopy();
            array[j].start();
        }

        for(int i=0; i<numThreads; i++){
            try{
                array[i].join();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public class ImageCopy extends  Thread{

        public void run(){
//            createImage();

            Bitmap[] images = new Bitmap[chunk];
            for(int i=0; i<chunk; i++){
                Bitmap img; // = Bitmap.createBitmap(1000, 1000, Bitmap.Config.RGB_565);
                img = bitmap.copy(Bitmap.Config.RGB_565, true);
                images[i] = img;
            }
        }
    }

    void createImage(){
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() +
//                "/ngc3582-1000.jpg";
                    "/Image.jpg";
        bitmap = BitmapFactory.decodeFile(path);



    }
}

package com.example.sima.imgblurexplpowerm;

import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;


public class JobHandler extends Thread {
    int type;
    final int  EXPLICIT = 0;
    final int EXECUTOR = 1;
    final int FORKJOIN = 2;
    final int ASYNCTASK = 3;
    final int HANDLERR = 4;
    final int HANDLERM = 5;

    private MainActivity mainActivity;
    public JobHandler(MainActivity mainActivity, int type) {
        this.mainActivity = mainActivity;
        this.type = type;
    }

    public void run(){
        mainActivity.startTime = System.currentTimeMillis();
        mainActivity.doJob();

        switch (type){
            case EXPLICIT: {
                Explicit[] pool = new Explicit[mainActivity.numThreads];

//                Log.d("Debug","Pool created");
                for (int i = 0; i < mainActivity.numThreads; i++) {
                    pool[i] = new Explicit(mainActivity, mainActivity.src, i * mainActivity.pieceWidth,
                            mainActivity.pieceWidth, mainActivity.dst);
                    pool[i].start();
                }

                for (int j = 0; j < mainActivity.numThreads; j++) {
                    if (pool[j].isAlive()) {
                        try {
//                            Log.d("Debug", "waiting for "+ pool[j]);
                            pool[j].join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
                pool = null;
            }
            System.gc();
            break;
            case EXECUTOR: {
                ExecutorService pool;
                pool = Executors.newFixedThreadPool(mainActivity.numThreads);
                for (int j = 0; j < mainActivity.numThreads; j++) {
                    pool.submit(new ExecutorBlur(mainActivity, mainActivity.src, j*mainActivity.pieceWidth,
                            mainActivity.pieceWidth, mainActivity.dst));
                }

                pool.shutdown();
                try {
                    pool.awaitTermination(500, TimeUnit.SECONDS);
                }catch(Exception e){
                    e.printStackTrace();
                }
                pool = null;
            }
            System.gc();
                break;

            case FORKJOIN: {
                ForkBlur fb = new ForkBlur(mainActivity, mainActivity.src, 0,
                        mainActivity.w * mainActivity.h, mainActivity.dst);
                ForkJoinPool pool = new ForkJoinPool(mainActivity.numThreads);
                pool.invoke(fb);
                pool.shutdown();
                try {
                    pool.awaitTermination(500, TimeUnit.SECONDS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.gc();
            break;

            case ASYNCTASK: {
//                ExecutorService executor = new ThreadPoolExecutor(mainActivity.numThreads, 64, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(64));
                AsyncTaskBlur[] pool = new AsyncTaskBlur[mainActivity.numThreads];
                for (int j = 0; j < mainActivity.numThreads; j++) {
                    pool[j] = new AsyncTaskBlur(mainActivity, mainActivity.src, j * mainActivity.pieceWidth,
                            mainActivity.pieceWidth, mainActivity.dst);
                    pool[j].executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }

                System.gc();

                for(int j=0; j<mainActivity.numThreads; j++) {
                    try {
                        pool[j].get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                pool = null;
            }
            System.gc();
                break;

            case HANDLERR: {
//                Handler handler;
                Thread[] pool = new Thread[mainActivity.numThreads];

                for (int j = 0; j < mainActivity.numThreads; j++) {
                    pool[j] = new Thread(new HandlerR(mainActivity, mainActivity.src, j * mainActivity.pieceWidth,
                            mainActivity.pieceWidth, mainActivity.dst));
                    pool[j].start();
                }

                System.gc();
                for(int j=0; j<mainActivity.numThreads; j++){
                    if(pool[j].isAlive()){
                        try {
                            pool[j].join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                pool = null;
            }
            System.gc();
                break;

            case  HANDLERM:{
//                handler = new Handler();
                Thread[] pool = new Thread[mainActivity.numThreads];

                for(int i=0; i<mainActivity.numThreads; i++){
                    pool[i] =  new Thread(new HandlerM(mainActivity, mainActivity.src, i * mainActivity.pieceWidth,
                            mainActivity.pieceWidth, mainActivity.dst, mainActivity.handler));
                    pool[i].start();
                }

                for(int j=0; j<mainActivity.numThreads; j++){
                    if(pool[j].isAlive()) {
                        try{
                            pool[j].join();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
                pool = null;
            }
            System.gc();
                break;

        }

        mainActivity.dest.setPixels(mainActivity.dst, 0, mainActivity.w, 0, 0, mainActivity.w, mainActivity.h);
        (mainActivity.layout).setBackground(new BitmapDrawable(mainActivity.dest));
        System.out.println("Duration: " + (System.currentTimeMillis() - mainActivity.startTime));

//        mainActivity.bitmap.recycle();
//        mainActivity.dest.recycle();

        mainActivity.src = null;
        mainActivity.dst = null;

    }
}



package com.example.assl_blue.dacapo_benchmark;
import android.os.Handler;
import android.os.Message;

/**
 *
 * @author Sima Mehri
 */

public class MandelbrotHandlerM extends MBase {
    final static int SET_RESULT = 0;

    Handler handler = new Handler();

    MandelbrotHandlerM(){
        super();
    }

    MandelbrotHandlerM(int numThreads){
        super(numThreads);
    }

    @Override
    protected  void doJob(){
        chunk = out.length/numThread;
//        Handler[] handlers = new Handler[numThread];
        Thread[] threads = new Thread[numThread];

        for(int i=0; i<numThread; i++){
            threads[i] = new Thread(new MandelBrotMsgHnd(handler, i*chunk, (i+1)*chunk));
            threads[i].start();
        }

        for (int k=0; k<numThread; k++) {
            try{
                if (threads[k].isAlive()) threads[k].join();
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    public class MandelBrotMsgHnd implements Runnable {
        private final Handler handler;
        int lb;
        int ub;

        MandelBrotMsgHnd(Handler handler, int a, int b) {
            this.handler = handler;
            lb = a;
            ub = b;
        }

        public void run() {
            Message msg;

            int y;
            while ((y = yCt.getAndIncrement()) < out.length) {
                putLine(y, out[y]);
            }
        }
    }

}

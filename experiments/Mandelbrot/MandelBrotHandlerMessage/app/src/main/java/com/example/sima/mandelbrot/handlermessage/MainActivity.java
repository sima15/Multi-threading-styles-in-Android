package com.example.sima.mandelbrot.handlermessage;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {
    final static int SET_PROGRESS_BAR_VISIBILITY = 0;
    final static int PROGRESS_UPDATE = 1;
    final static int SET_RESULT = 2;

    long startTime;
    long endTime;
    double duration;

    Thread[] pool;
//    int input;
    int N;
    byte[][] out;
    static AtomicInteger yCt;
    static double[] Crb;
    static double[] Cib;


    private ProgressBar mProgressBar;
    private EditText textHandler;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case SET_PROGRESS_BAR_VISIBILITY: {
                    mProgressBar.setVisibility((Integer) msg.obj);
                    break;
                }
                case PROGRESS_UPDATE: {
                    mProgressBar.setProgress((Integer) msg.obj);
                    break;
                }
                case SET_RESULT: {
                    textHandler.setText((String) msg.obj);
                    break;
                }
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textHandler = (EditText) findViewById(R.id.input);
        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);
        Button submitButton = (Button) findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTime = System.nanoTime();
                System.out.println("Start time is: " + startTime);

                String inputStr = String.valueOf(textHandler.getText());
                N = Integer.parseInt(inputStr);
                Crb = new double[N + 7];
                Cib = new double[N + 7];
                double invN = 2.0 / N;
                for (int i = 0; i < N; i++) {
                    Cib[i] = i * invN - 1.0;
                    Crb[i] = i * invN - 1.5;
                }
                yCt = new AtomicInteger();
                out = new byte[N][(N + 7) / 8];

//                int loopCount = 10 * Runtime.getRuntime().availableProcessors();
//                System.out.println("Available processors: "+loopCount/10);

                Thread t1 = new Thread(new MandelBrotMsgHnd(handler));
                t1.start();
                try {
                    t1.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }
        });
    }

    static int getByte(int x, int y) {
        int res = 0;
        for (int i = 0; i < 8; i += 2) {
            double Zr1 = Crb[x + i];
            double Zi1 = Cib[y];

            double Zr2 = Crb[x + i + 1];
            double Zi2 = Cib[y];

            int b = 0;
            int j = 49;
            do {
                double nZr1 = Zr1 * Zr1 - Zi1 * Zi1 + Crb[x + i];
                double nZi1 = Zr1 * Zi1 + Zr1 * Zi1 + Cib[y];
                Zr1 = nZr1;
                Zi1 = nZi1;

                double nZr2 = Zr2 * Zr2 - Zi2 * Zi2 + Crb[x + i + 1];
                double nZi2 = Zr2 * Zi2 + Zr2 * Zi2 + Cib[y];
                Zr2 = nZr2;
                Zi2 = nZi2;

                if (Zr1 * Zr1 + Zi1 * Zi1 > 4) {
                    b |= 2;
                    if (b == 3) break;
                }
                if (Zr2 * Zr2 + Zi2 * Zi2 > 4) {
                    b |= 1;
                    if (b == 3) break;
                }
            } while (--j > 0);
            res = (res << 2) + b;
        }
        return res ^ -1;
    }
    static void putLine(int y, byte[] line) {
        for (int xb = 0; xb < line.length; xb++)
            line[xb] = (byte) getByte(xb * 8, y);
    }

//    static void putLine(int y, byte[] line) {
//        for (int xb = 0; xb < line.length; xb++)
//            line[xb] = (byte) getByte(xb * 8, y);
//    }

    public class MandelBrotMsgHnd implements Runnable {
        private final Handler handler;

        MandelBrotMsgHnd(Handler handler) {
            this.handler = handler;
        }

        public void run() {
            Message msg;
            msg = handler.obtainMessage(SET_PROGRESS_BAR_VISIBILITY, ProgressBar.VISIBLE);
            handler.sendMessage(msg);

            int poolLength =8;
            for (int i = 0; i <= 8000; i++) {
                pool = new Thread[poolLength];
                for (int j = 0; j < pool.length; j++) {
                    pool[j] = new Thread() {
                        public void run() {
                            int y;
                            while ((y = yCt.getAndIncrement()) < out.length) {
                                putLine(y, out[y]);
                            }
                        }
                    };
                }
            }
            for (int k=0; k<poolLength; k++) {
                pool[k].start();
            }

            for (int k=0; k<poolLength; k++) {
                try{
                    if (pool[k].isAlive()) pool[k].join();
                    System.out.println("Waiting for thread " + k + " to complete");
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }

            System.out.print(("P4\n" + N + " " + N + "\n").getBytes());
            for(int i=0;i<N;i++) System.out.println(out[i]);

            endTime = System.nanoTime();
            duration = (endTime - startTime)/1000000.00;
            System.out.println("end time: " + endTime);
            System.out.println("Duration: " + duration);
//            System.out.print(duration);


            msg = handler.obtainMessage(SET_RESULT, String.valueOf( (int) duration));
            handler.sendMessage(msg);
        }
    }
}

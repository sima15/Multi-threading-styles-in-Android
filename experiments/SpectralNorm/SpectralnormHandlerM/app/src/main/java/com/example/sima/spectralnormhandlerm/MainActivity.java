package com.example.sima.spectralnormhandlerm;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 *
 * @author Sima Mehri
 */

public class MainActivity extends AppCompatActivity {
    Handler handler = new Handler();
    long startTime;
    int numThread = 4;
    static Approximate[] ap;
    Lock lock = new Lock();
    TextView view;
    long duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = (TextView)findViewById(R.id.textView);

//        NetworkH w1= new NetworkH();
//        w1.start();
        JobHandler hndlr = new JobHandler();
        hndlr.start();

        try {
//            w1.join();
            hndlr.join();

        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public class JobHandler extends  Thread{


        @Override
        public void run() {
            startTime = System.currentTimeMillis();
            System.out.println("Start time: "+ startTime);
            try {
                Thread t = new Thread(new Spectral(handler));
                t.start();
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public class Spectral implements  Runnable{

//        Handler handler;
        public Spectral(Handler han) {
            handler = han;
        }

        public void  run(){
            System.out.println("Just started");
            final NumberFormat formatter = new DecimalFormat("#.000000000");
            int n = 10;
            try {
                for(int i = 0; i < 10; i++) {
                    System.out.println("result is: " + formatter.format(spectralnormGame(n)));
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            duration = System.currentTimeMillis() - startTime;
            System.out.println("total time: "  +duration+  "ms");

//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    view.setText((int) duration);
//                }
//            });
        }

    }
    private final double spectralnormGame(int n) throws InterruptedException {
        // create unit vector
        double[] u = new double[n];
        double[] v = new double[n];
        double[] tmp = new double[n];

        for (int i = 0; i < n; i++)
            u[i] = 1.0;

        // get available processor, then set up syn object
        System.out.println("No. of threads: " + numThread);

        int chunk = n / numThread;
        ap = new Approximate[numThread];

        for (int i = 0; i < numThread; i++) {
            int r1 = i * chunk;
            int r2 = (i < (numThread - 1)) ? r1 + chunk : n;

            ap[i] = new Approximate(u, v, tmp, r1, r2);
        }

        lock.justStarted = true;

        synchronized (lock) {
            int i = 0;
            while (!checkDone()) {
                if (!lock.checkIndex() || lock.justStarted) {
                    lock.justStarted = false;
                    lock.wait();
                }

                //wake all threads
                for (Approximate a : ap) {
                    synchronized (a) {
                        a.notifyAll();
                    }
                }
                lock.justStarted = true;

                i++;
            }
            System.out.println("iteration: " + (i++));
        }

        double vBv = 0, vv = 0;
        for (int i = 0; i < numThread; i++) {
            try {
                ap[i].join();

                vBv += ap[i].m_vBv;
                vv += ap[i].m_vv;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return Math.sqrt(vBv / vv);
    }

    public static synchronized boolean checkDone() {

        for (Approximate a : ap) {
            if (!a.done) return false;
        }
        return true;
    }

    public class Approximate extends Thread {
        private double[] _u;
        private double[] _v;
        private double[] _tmp;

        private int range_begin, range_end;
        double m_vBv = 0, m_vv = 0;

        public boolean done = false;

        public Approximate(double[] u, double[] v, double[] tmp, int rbegin, int rend) {
            super();

            _u = u;
            _v = v;
            _tmp = tmp;

            range_begin = rbegin;
            range_end = rend;

            start();
        }

        public void run() {
            // 20 steps of the power method
            for (int i = 0; i < 20; i++) {
                MultiplyAtAv(_u, _tmp, _v);
                MultiplyAtAv(_v, _tmp, _u);
            }


            for (int i = range_begin; i < range_end; i++) {
                m_vBv += _u[i] * _v[i];
                m_vv += _v[i] * _v[i];
            }

            done = true;
            //done, let's notify to the main
            synchronized (lock) {
                lock.notify();
            }
        }

        /* return element i,j of infinite matrix A */
        private final  double eval_A(int i, int j) {
            int div = (((i + j) * (i + j + 1) >>> 1) + i + 1);
            return 1.0 / div;
        }

        /* multiply vector v by matrix A, each thread evaluate its range only */
        private final void MultiplyAv(final double[] v, double[] Av) throws InterruptedException {
            for (int i = range_begin; i < range_end; i++) {
                double sum = 0;
                for (int j = 0; j < v.length; j++)
                    sum += eval_A(i, j) * v[j];

                Av[i] = sum;
            }

            synchronized (this) {
                lock.increment();
                this.wait();
            }

        }

        /* multiply vector v by matrix A transposed */
        private final void MultiplyAtv(final double[] v, double[] Atv) throws InterruptedException {
            for (int i = range_begin; i < range_end; i++) {
                double sum = 0;
                for (int j = 0; j < v.length; j++)
                    sum += eval_A(j, i) * v[j];

                Atv[i] = sum;
            }

            synchronized (this) {
                lock.increment();
                this.wait();
            }

        }

        /* multiply vector v by matrix A and then by matrix A transposed */
        private final void MultiplyAtAv(final double[] v, double[] tmp, double[] AtAv) {
            try {
                MultiplyAv(v, tmp);
                // all thread must syn at completion
                MultiplyAtv(tmp, AtAv);
                // all thread must syn at completion
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class Lock {
        public int index = 0;
        public boolean justStarted = false;

        synchronized boolean increment() {
            index++;
            if (index % numThread == 0) {
                synchronized(lock) {	//the last thread is done
                    index = 0;
                    lock.notify();
                }
                return true;
            } else {
                return false;
            }
        }

        synchronized boolean checkIndex() {
            if (index % numThread != 0) {
                return false;
            }

            return true;
        }
    }

    class NetworkH extends  Thread{
        private final String USER_AGENT = "Mozilla/5.0";
        URL endMonsoon;

        public void run(){
            System.out.println("Hello from inside the thread");

            System.out.println("Starting the connection");
            URL startMonsoon = null;
            try {
                startMonsoon = new URL("http://129.123.7.199:8000/start");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            for(int i=0; i<=6; i++) {
                try {

                    HttpURLConnection con = (HttpURLConnection) startMonsoon.openConnection();
                    con.setRequestMethod("GET");
                    con.setRequestProperty("User-Agent", USER_AGENT);
                    int responseCode = con.getResponseCode();
                    System.out.println("\nSending 'GET' request to URL : " + startMonsoon);
                    System.out.println("Response Code : " + responseCode);
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(con.getInputStream()));
                    // String inputLine;
                    StringBuffer response = new StringBuffer();
                    System.out.println(response.toString());

                    startTime = System.currentTimeMillis();
                    System.out.println("Started...");
                    numThread =(int) Math.pow(2, i);
                    JobHandler hndlr = new JobHandler();
                    hndlr.start();
                    hndlr.join();

                    in.close();

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                long endTime = System.currentTimeMillis();
                System.out.println("Duration: "+ (endTime-startTime));
                try {
                    StringBuilder sb = new StringBuilder("http://129.123.7.199:8000/save?file=SpectralnormHandlerM");

                    sb.append(String.format("%d%s\n", i, ".pt5"));
                    System.out.println(sb);
                    System.out.println("trying to end the connection");
                    endMonsoon = new URL(String.valueOf(sb));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                try {
                    HttpURLConnection endCon = (HttpURLConnection) endMonsoon.openConnection();
                    endCon.setRequestMethod("GET");
                    endCon.setRequestProperty("User-Agent", USER_AGENT);
                    int responseCode2 = endCon.getResponseCode();
                    System.out.println("\nSending 'GET' request to URL : " + endMonsoon);
                    System.out.println("Response Code : " + responseCode2);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Program finished");
        }
    }

}

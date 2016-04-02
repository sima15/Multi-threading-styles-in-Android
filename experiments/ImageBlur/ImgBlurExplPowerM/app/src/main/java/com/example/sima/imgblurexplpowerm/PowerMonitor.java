package com.example.sima.imgblurexplpowerm;

import android.os.SystemClock;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by daehyeok on 2016. 3. 21..
 */
public class PowerMonitor {
    static boolean usePowerMonitor = true;
    static String baseUrl = "http://129.123.7.34:8000";
    RandomAccessFile reader;
    String load;
    String[] toks;
    long cpu1, cpu2, idle1, idle2;
    long sTime, eTime;

    PowerMonitor() {
        try {
            reader = new RandomAccessFile("/proc/stat", "r");

        } catch (Exception e) {
            Log.e("Proc Exception", "", e);
        }


    }

    ;


    private String sendRequest(final String apiUrl) {


        class RequestThread extends Thread {
            public HttpURLConnection con;
            String body;

            @Override
            public void run() {
                try {
                    URL url = new URL(apiUrl);
                    Log.d("Power Monitoring", "Sending request to URL : " + url);
                    con = (HttpURLConnection) url.openConnection();
                    Log.d("Power Monitoring", "Response Code : " + con.getResponseCode());

                    InputStream in = new BufferedInputStream(con.getInputStream());
                    body = readStream(in);

                } catch (Exception e) {
                    Log.e("Power Monitoring", "exception", e);
                }
            }
        }


        try {
            while (true) {
                RequestThread thread = new RequestThread();
                thread.start();
                thread.join();

                if (thread.con.getResponseCode() == 200) {
                    return thread.body;
                }
                Log.d("Power Monitoring", "Power Monitor have a Problem Waiting 30 sec");
                SystemClock.sleep(10000);
            }
        } catch (Exception e) {
            Log.e("Power Monitoring", "exception", e);
        }

        return null;
    }

    public void setBaseUrl(String url) {
        baseUrl = url;
    }

    public boolean startMonitoring() {
        Log.i("Power Monitoring", "Power Monitor start");
        if (!usePowerMonitor) return true;

        try {
            sendRequest(baseUrl + "/start");
        } catch (Exception e) {
            Log.e("Power Monitoring", "exception", e);
            return false;
        }

        return true;
    }

    public boolean saveMonitoring() {
        Log.i("Power Monitoring", "Power Monitor save : ");
        if (!usePowerMonitor) return true;

        try {
            sendRequest(baseUrl + "/save/");
        } catch (Exception e) {
            Log.e("Power Monitoring", "exception", e);
            return false;
        }

        return true;
    }

    public boolean stopMonitoring(String filename) {
        long duration = eTime-sTime;
        String postfix = String.format("_%d_%d_%d", duration, cpu1, cpu2);
        Log.i("Power Monitoring", "Power Monitor stop : " + filename + postfix);
        if (!usePowerMonitor) return true;

        try {
            sendRequest(baseUrl + "/stop/" + filename + postfix);
        } catch (Exception e) {
            Log.e("Power Monitoring", "exception", e);
            return false;
        }

        return true;
    }

    public String getTarget() {
        Log.i("Power Monitoring", "Trying to get target");

        String body = "";
        if (!usePowerMonitor) return "";

        try {
            body =   sendRequest(baseUrl + "/target");

        } catch (Exception e) {
            Log.e("Power Monitoring", "exception", e);
        }
        return body;
    }

    public void readFirstUsage() {
        try {
            reader.seek(0);
            load = reader.readLine();
            toks = load.split(" ");
            idle1 = Long.parseLong(toks[5]);
            cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);
            sTime = System.currentTimeMillis();
        } catch (Exception e) {
            Log.e("Proc Exception", "", e);
        }

    }

    public float getUsage() {
        return (float) (cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));
    }

    public void readLastUsage() {
        try {
            reader.seek(0);
            load = reader.readLine();
            toks = load.split(" ");

            idle2 = Long.parseLong(toks[5]);
            cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);
            eTime = System.currentTimeMillis();
        } catch (Exception e) {
            Log.e("Proc Exception", "", e);
        }
    }

    private String readStream(InputStream is) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int i = is.read();
            while (i != -1) {
                bo.write(i);
                i = is.read();
            }

            return bo.toString();
        } catch (Exception e) {
            Log.e("Power Monitroing", "Read Stream", e);
            return "";
        }
    }
}

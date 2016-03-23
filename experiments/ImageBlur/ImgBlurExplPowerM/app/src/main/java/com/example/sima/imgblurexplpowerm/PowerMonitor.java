package com.example.sima.imgblurexplpowerm;

import android.os.SystemClock;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by daehyeok on 2016. 3. 21..
 */
public class PowerMonitor {
    static boolean usePowerMonitor = true;
    static String baseUrl = "http://129.123.7.34:8000";

    PowerMonitor() {};


    private HttpURLConnection sendRequest(final String apiUrl){

        class RequestThread extends Thread{
            public HttpURLConnection con;
            @Override
            public void run() {
                try {
                    URL url = new URL(apiUrl);
                    Log.d("Power Monitoring", "Sending request to URL : " + url);
                    con = (HttpURLConnection) url.openConnection();
                    Log.d("Power Monitoring", "Response Code : " + con.getResponseCode());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


        try {
            while(true) {
                RequestThread thread = new RequestThread();
                thread.start();
                thread.join();

                if (thread.con.getResponseCode() == 200){
                    return thread.con;
                }
                Log.d("Power Monitoring", "Power Monitor have a Problem Waiting 30 sec");
                SystemClock.sleep(10000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    public void setBaseUrl(String url) {
        baseUrl = url;
    }

    public boolean  startMonitoring() {
        Log.i("Power Monitoring", "Power Monitor start");
        if (!usePowerMonitor) return true;

        try {
            sendRequest(baseUrl + "/start");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean saveMonitoring(String filename) {
        Log.i("Power Monitoring", "Power Monitor stop : " + filename);
        if (!usePowerMonitor) return true;

        try {
            sendRequest(baseUrl + "/save/" + filename);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public String getTarget() {
        Log.i("Power Monitoring", "Trying to get target");

        String TargetString = "";

        if (!usePowerMonitor) return TargetString;

        try {
            HttpURLConnection con = sendRequest(baseUrl + "/target");

            InputStream in = new BufferedInputStream(con.getInputStream());
            TargetString = readStream(in);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return TargetString;
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
            return "";
        }
    }
}

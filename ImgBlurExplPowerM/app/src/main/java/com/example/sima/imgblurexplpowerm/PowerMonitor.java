package com.example.sima.imgblurexplpowerm;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by daehyeok on 2016. 3. 21..
 */
public class PowerMonitor {
    static boolean usePowerMonitor = false;
    static String baseUrl = "http://10.8.0.1:8000";

    PowerMonitor() {
    }

    ;

    public static void setBaseUrl(String url) {
        baseUrl = url;
    }

    public static boolean startMonitoring() {
        Log.i("Power Monitoring", "Power Monitor start");
        if (!usePowerMonitor) return true;

        URL url;
        try {
            url = new URL(baseUrl + "/start");

            Log.d("Power Monitoring", "Sending Start request to URL : " + url);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            Log.d("Power Monitoring", "Response Code : " + con.getResponseCode());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean saveMonitoring(String filename) {
        Log.i("Power Monitoring", "Power Monitor stop : request " + filename);
        if (!usePowerMonitor) return true;

        URL url;
        try {
            url = new URL(baseUrl + "/save?file=" + filename);

            Log.d("Power Monitoring", "Sending Save request to URL : " + url);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            Log.d("Power Monitoring", "Response Code : " + con.getResponseCode());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }


}

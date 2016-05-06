package com.example.sima.increment;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.math.BigInteger;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView resultView = (TextView)findViewById(R.id.resultView);
        BigInteger num = new BigInteger("0");

        for(int i=0; i<2250000; i++){
            num = num.add(BigInteger.valueOf(i));
        }

        resultView.setText(String.valueOf(num));
    }
}

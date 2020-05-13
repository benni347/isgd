package com.example.isgd;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void test(View view) {
        TextView input = (TextView) findViewById(R.id.inpout);
        String inputurl = input.getText().toString();
        try {
            inputurl = URLEncoder.encode(inputurl, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        TextView output = (TextView) findViewById(R.id.outpout);
        output.setText(inputurl);


    }
}

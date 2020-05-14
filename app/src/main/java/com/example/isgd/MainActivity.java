package com.example.isgd;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
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

        // Does the user want statistics to be logged?
        boolean logStats = ((CheckBox) findViewById(R.id.stats)).isChecked();

        // Read the "long" input URL
        String inputurl = input.getText().toString();
        try {
            // Encode the URL (convert chars like ":" and "&")
            inputurl = URLEncoder.encode(inputurl, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // Build the string with the url parameters (after the "is.gd/create.php")
        String urlParameters = new StringBuilder()
                .append("format=simple")
                .append("&url=").append(inputurl)
                .toString();

        // Are stats to be logged?
        // If so, append "logstats=1" to urlParameters
        if (logStats) urlParameters = new StringBuilder(urlParameters).append("&logstats=1").toString();

        TextView output = (TextView) findViewById(R.id.outpout);
        output.setText(urlParameters);

    }
}

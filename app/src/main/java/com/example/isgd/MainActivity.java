package com.example.isgd;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.baeldung.httprequest.ParameterStringBuilder;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new
                    StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    public static String executePost(String targetURL, String urlParameters) {
        HttpURLConnection connection = null;

        try {
            // Create connection
            URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Length",
                    Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream (
                    connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.close();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String readStream(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(is),1000);
        for (String line = r.readLine(); line != null; line =r.readLine()){
            sb.append(line);
        }
        is.close();
        return sb.toString();
    }

    public void test(View view) throws MalformedURLException {
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

        // Build the string with the url parameters (after the "create.php")
        String urlParameters = new StringBuilder()
                .append("format=simple")
                .append("&url=").append(inputurl)
                .toString();

        // Are stats to be logged?
        // If so, append "logstats=1" to urlParameters
        if (logStats) urlParameters = new StringBuilder(urlParameters).append("&logstats=1").toString();

        // Is there an inputurl which is to be shortened?
        // Only do a http call, IF there's a long URL
        if (inputurl != null && !inputurl.isEmpty()) {
            String shortURL;
            shortURL = MainActivity.executePost(getString(R.string.create_url_base), urlParameters);

            TextView output = (TextView) findViewById(R.id.outpout);
            output.setText(shortURL);
        }
    }
}

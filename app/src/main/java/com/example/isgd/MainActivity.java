package com.example.isgd;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    private static final int DOWNLOAD_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void click_stats(View view) {
        // Find the checkbox "log stats?"
        CheckBox cb_stats = findViewById(R.id.stats);
        // Does the user want statistics to be logged?
        boolean logStats = cb_stats.isChecked();
        // Find the field for the stats URL
        EditText et_stats_url = findViewById(R.id.output_stats_url);

        // Set eb_stats_url to visible or invisible, depending on logStats
        if (logStats)
            et_stats_url.setVisibility(View.VISIBLE);
        else
            et_stats_url.setVisibility(View.INVISIBLE);
    }

    public void click_kuerzen(View view) {
        // Find the "input" field
        TextView input = findViewById(R.id.input);
        // Read the "long" original URL
        String inputUrl = input.getText().toString();

        try {
            // Encode the URL (convert chars like ":" and "&")
            inputUrl = URLEncoder.encode(inputUrl, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // Build the string with the url parameters (after the "create.php")
        String urlParameters = "format=simple" + "&url=" + inputUrl;

        // Are stats to be logged?
        // If so, append "logstats=1" to url
        // Find the checkbox "log stats?"
        CheckBox cb_stats = findViewById(R.id.stats);
        // Does the user want statistics to be logged?
        boolean logStats = cb_stats.isChecked();
        if (logStats) urlParameters = urlParameters + "&logstats=1";

        PendingIntent pendingResult = createPendingResult(
                DOWNLOAD_REQUEST_CODE, new Intent(), 0);

        // Is there an input url which is to be shortened?
        // Only do a http call, IF there's a long URL
        if (inputUrl != null && !inputUrl.isEmpty()) {
            Intent intent = new Intent(getApplicationContext(), ShortenUrlIntentService.class);
            intent.putExtra(ShortenUrlIntentService.URL_PARAMETERS_EXTRA, urlParameters);
            intent.putExtra(ShortenUrlIntentService.TARGET_URL_EXTRA, getString(R.string.create_url_base));
            intent.putExtra(ShortenUrlIntentService.PENDING_RESULT_EXTRA, pendingResult);
            startService(intent);
        }
    }

    /*
     This method gets called when the ShortenUrlIntentService is finished.
     It will either print the shortened URL or show an error message.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DOWNLOAD_REQUEST_CODE) {
            switch (resultCode) {
                case ShortenUrlIntentService.INVALID_URL_CODE:
                    handleInvalidURL();
                    break;
                case ShortenUrlIntentService.ERROR_CODE:
                    handleError(data);
                    break;
                case ShortenUrlIntentService.RESULT_CODE:
                    handleShortURL(data);
                    break;
            }
            handleShortURL(data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleShortURL(Intent data) {
        TextView output;
        String shortURL;
        URL shortURL_URL;
        URL urlBase;
        String statsURL;

        shortURL = data.getStringExtra(ShortenUrlIntentService.URL_RESULT_EXTRA);

        output = findViewById(R.id.outpout);
        output.setText(shortURL);

        // Should stats be logged? If so, print stats URL
        // Find the checkbox "log stats?"
        CheckBox cb_stats = findViewById(R.id.stats);
        // Does the user want statistics to be logged?
        boolean logStats = cb_stats.isChecked();
        // Find the field for the stats URL
        EditText et_stats_url = findViewById(R.id.output_stats_url);

        try {
            // Eg. https://is.gd/HN91Dy
            shortURL_URL = new URL(shortURL);
            urlBase = new URL(getString(R.string.create_url_base));

            // https://is.gd/stats.php?url=HN91Dy
            statsURL = urlBase.getProtocol() + "://"
                    + urlBase.getAuthority() + "/"
                    + "stats.php?url="
                    + shortURL_URL.getPath().substring(1);
            et_stats_url.setText(statsURL);
        } catch (MalformedURLException e) {
            Log.d("shortURL", "The returned Short URL is not valid. " + e.getMessage());
        }
    }

    private void handleError(Intent data) {
        // whatever you want
    }

    private void handleInvalidURL() {
        // whatever you want
    }
}

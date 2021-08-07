package com.example.isgd;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            }
        } /* else {
            // Handle other intents, such as being started from the home screen
        } */
    }
    /*Add ads*/


    final int DOWNLOAD_REQUEST_CODE = 0;


    void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            // Find the "input" field
            TextView input = findViewById(R.id.input);
            // Set the "long" original URL
            input.setText(sharedText);

            // Shorten the URL
            trigger_shorten_url();
        }
    }

    public void click_stats(View view) {
        // Find the checkbox "log stats?"
        CheckBox cb_stats = findViewById(R.id.stats);
        // Does the user want statistics to be logged?
        boolean logStats = cb_stats.isChecked();
        // Find the field for the stats URL
        TextView tv_stats_url = findViewById(R.id.statsUrl);
        // Find the field for the share stats button
        ImageButton ib_share_stats = findViewById(R.id.shareStatsButton);

        // Set eb_stats_url & ib_share_stats to visible or invisible, depending on logStats
        if (logStats) {
            tv_stats_url.setVisibility(View.VISIBLE);
            ib_share_stats.setVisibility(View.VISIBLE);
        } else {
            tv_stats_url.setVisibility(View.INVISIBLE);
            ib_share_stats.setVisibility(View.INVISIBLE);
        }
    }

    public void trigger_shorten_url() {
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

    public void click_kuerzen(View view) {
        trigger_shorten_url();
    }

    public void click_url_share(View view) {
        TextView tv_shortUrl;
        String shortURL;

        tv_shortUrl = findViewById(R.id.shortUrl);
        shortURL = tv_shortUrl.getText().toString();

        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Short URL");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shortURL);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    public void click_stats_share(View view) {
        TextView tv_statsUrl;
        String statsURL;

        tv_statsUrl = findViewById(R.id.statsUrl);
        statsURL = tv_statsUrl.getText().toString();

        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Statistik URL");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, statsURL);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
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
        TextView tv_shortUrl, tv_statsUrl;
        String shortURL;
        URL shortURL_URL;
        URL urlBase;
        String statsURL;

        shortURL = data.getStringExtra(ShortenUrlIntentService.URL_RESULT_EXTRA);

        tv_shortUrl = findViewById(R.id.shortUrl);
        tv_shortUrl.setText(shortURL);

        // Should stats be logged? If so, print stats URL
        // Find the checkbox "log stats?"
        CheckBox cb_stats = findViewById(R.id.stats);
        // Does the user want statistics to be logged?
        boolean logStats = cb_stats.isChecked();
        // Find the field for the stats URL
        tv_statsUrl = findViewById(R.id.statsUrl);

        try {
            // Eg. https://is.gd/HN91Dy
            shortURL_URL = new URL(shortURL);
            urlBase = new URL(getString(R.string.create_url_base));

            // https://is.gd/stats.php?url=HN91Dy
            statsURL = urlBase.getProtocol() + "://"
                    + urlBase.getAuthority() + "/"
                    + "stats.php?url="
                    + shortURL_URL.getPath().substring(1);
            tv_statsUrl.setText(statsURL);
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
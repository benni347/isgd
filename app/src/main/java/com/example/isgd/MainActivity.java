package com.example.isgd;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    private static final int DOWNLOAD_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        shortURL = data.getStringExtra(ShortenUrlIntentService.URL_RESULT_EXTRA);
        output = findViewById(R.id.outpout);
        output.setText(shortURL);
    }

    private void handleError(Intent data) {
        // whatever you want
    }

    private void handleInvalidURL() {
        // whatever you want
    }
}

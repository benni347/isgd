package com.example.isgd;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ShortenUrlIntentService extends IntentService {

    public static final String PENDING_RESULT_EXTRA = "pending_result";
    public static final String TARGET_URL_EXTRA = "target_rrl";
    public static final String URL_PARAMETERS_EXTRA = "url_parameters";
    public static final String URL_RESULT_EXTRA = "url_result";
    public static final int RESULT_CODE = 0;
    public static final int INVALID_URL_CODE = 1;
    public static final int ERROR_CODE = 2;
    private static final String TAG = ShortenUrlIntentService.class.getSimpleName();

    public ShortenUrlIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Initialize local variables
        HttpURLConnection connection = null;
        PendingIntent reply = intent.getParcelableExtra(PENDING_RESULT_EXTRA);
        InputStream is;
        BufferedReader rd;
        String targetUrl;
        String urlParameters;
        int urlParametersLen;
        URL url;
        DataOutputStream wr;

        try {
            targetUrl = intent.getStringExtra(TARGET_URL_EXTRA);
            urlParameters = intent.getStringExtra(URL_PARAMETERS_EXTRA);
            urlParametersLen = urlParameters.getBytes().length;

            try {
                // Create connection
                url = new URL(targetUrl);
                connection = (HttpURLConnection) url.openConnection();

                // Set "parameters" for connection
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("Content-Length", Integer.toString(urlParametersLen));

                connection.setUseCaches(false);
                connection.setDoOutput(true);

                // Send request to remote server
                wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(urlParameters);
                wr.close();

                // Get Response
                is = connection.getInputStream();
                rd = new BufferedReader(new InputStreamReader(is));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                rd.close();

                Intent result = new Intent();
                result.putExtra(URL_RESULT_EXTRA, response.toString());

                reply.send(this, RESULT_CODE, result);
            } catch (MalformedURLException exc) {
                reply.send(INVALID_URL_CODE);
            } catch (Exception exc) {
                // could do better by treating the different sax/xml exceptions individually
                reply.send(ERROR_CODE);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        } catch (PendingIntent.CanceledException exc) {
            Log.i(TAG, "reply cancelled", exc);
        }
    }
}

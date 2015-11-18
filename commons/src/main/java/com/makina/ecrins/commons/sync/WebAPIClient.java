package com.makina.ecrins.commons.sync;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;

/**
 * Basic implementation of HTTP client using {@code HttpURLConnection}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class WebAPIClient {

    private static final String TAG = WebAPIClient.class.getName();

    private final String defaultCharset;
    private final int timeout;

    public WebAPIClient() {

        this.defaultCharset = Charset.defaultCharset()
                                     .name();
        this.timeout = 5000; // in ms
    }

    @NonNull
    public HttpURLConnection post(
            @NonNull final String urlString,
            @NonNull final String token,
            @Nullable final String data) throws IOException {

        final URL url = new URL(urlString);

        String sanitizeData = data;

        if (TextUtils.isEmpty(data)) {
            sanitizeData = "{}";
        }

        final String query = String.format("token=%s&data=%s",
                                           URLEncoder.encode(token,
                                                             defaultCharset),
                                           URLEncoder.encode(sanitizeData,
                                                             defaultCharset));

        final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setConnectTimeout(timeout);
        urlConnection.setRequestMethod("POST");
        urlConnection.setDoOutput(true);
        urlConnection.setRequestProperty("Accept-Charset",
                                         defaultCharset);
        urlConnection.setRequestProperty("Content-Type",
                                         "application/x-www-form-urlencoded;charset=" + defaultCharset);

        OutputStream outputStream = null;

        try {
            outputStream = urlConnection.getOutputStream();
            outputStream.write(query.getBytes(defaultCharset));
        }
        finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }

        return urlConnection;
    }

    @Nullable
    public JSONObject readInputStreamAsJson(@NonNull final InputStream inputStream) {

        try {
            final JSONObject jsonObject = new JSONObject(IOUtils.toString(inputStream));
            inputStream.close();

            return jsonObject;
        }
        catch (JSONException | IOException ge) {
            Log.w(TAG,
                  ge.getMessage());
        }

        return null;
    }

    public boolean checkStatus(@NonNull final JSONObject response) {

        return response.optInt("status_code",
                               -1) == 0;
    }
}

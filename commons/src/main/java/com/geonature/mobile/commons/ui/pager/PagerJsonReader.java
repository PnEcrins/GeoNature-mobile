package com.geonature.mobile.commons.ui.pager;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * Default {@code JsonReader} about reading a {@code JSON} stream and build the corresponding {@code ViewPager} metadata.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 * @see PagerJsonWriter
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class PagerJsonReader {

    private static final String TAG = PagerJsonReader.class.getName();

    /**
     * parse a {@code JSON} string to convert as {@link Pager}.
     *
     * @param json the {@code JSON} string to parse
     *
     * @return a {@link Pager} instance from the {@code JSON} string or {@code null} if something goes wrong
     *
     * @see #read(Reader)
     */
    @Nullable
    public Pager read(@Nullable final String json) {
        if (TextUtils.isEmpty(json)) {
            return null;
        }

        try {
            return read(new StringReader(json));
        }
        catch (IOException ioe) {
            Log.w(TAG,
                  ioe.getMessage());
        }

        return null;
    }

    /**
     * parse a {@code JSON} reader to convert as {@link Pager}.
     *
     * @param in the {@code Reader} to parse
     *
     * @return a {@link Pager} instance from the {@code JSON} reader
     *
     * @throws IOException if something goes wrong
     */
    @NonNull
    public Pager read(@NonNull final Reader in) throws
                                                IOException {
        final JsonReader jsonReader = new JsonReader(in);
        final Pager pager = read(jsonReader);
        jsonReader.close();

        return pager;
    }

    @NonNull
    private Pager read(@NonNull final JsonReader reader) throws
                                                         IOException {
        final Pager pager = new Pager();

        reader.beginObject();

        while (reader.hasNext()) {
            final String keyName = reader.nextName();

            switch (keyName) {
                case "id":
                    pager.setId(reader.nextLong());
                    break;
                case "size":
                    pager.setSize(reader.nextInt());
                    break;
                case "position":
                    pager.setPosition(reader.nextInt());
                    break;
                case "history":
                    final JsonToken jsonToken = reader.peek();

                    switch (jsonToken) {
                        case NULL:
                            reader.nextNull();
                            break;
                        case BEGIN_ARRAY:
                            reader.beginArray();

                            while (reader.hasNext()) {
                                pager.getHistory()
                                     .addLast(reader.nextInt());
                            }

                            reader.endArray();
                    }
                    break;
            }
        }

        reader.endObject();

        return pager;
    }
}

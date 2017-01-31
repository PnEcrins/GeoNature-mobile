package com.makina.ecrins.commons.ui.pager;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.JsonWriter;
import android.util.Log;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Default {@code JsonWriter} about writing an {@link Pager} as {@code JSON}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 * @see PagerJsonReader
 */
public class PagerJsonWriter {

    private static final String TAG = PagerJsonWriter.class.getSimpleName();

    /**
     * Convert the given {@link Pager} as {@code JSON} string.
     *
     * @param pager the {@link Pager} to convert
     *
     * @return a {@code JSON} string representation of the given {@link Pager} or {@code null} if something goes wrong
     *
     * @see #write(Writer, Pager)
     */
    @Nullable
    public String write(@Nullable final Pager pager) {
        if (pager == null) {
            return null;
        }

        final StringWriter writer = new StringWriter();

        try {
            write(writer,
                  pager);
        }
        catch (IOException ioe) {
            Log.w(TAG,
                  ioe.getMessage());

            return null;
        }

        return writer.toString();
    }

    /**
     * Convert the given {@link Pager} as {@code JSON} and write it to the given {@code Writer}.
     *
     * @param out   the {@code Writer} to use
     * @param pager the {@link Pager} to convert
     *
     * @throws IOException if something goes wrong
     */
    public void write(@NonNull final Writer out,
                      @NonNull final Pager pager) throws
                                                  IOException {
        final JsonWriter writer = new JsonWriter(out);
        write(writer,
              pager);
        writer.flush();
        writer.close();
    }

    private void write(@NonNull final JsonWriter writer,
                       @NonNull final Pager pager) throws
                                                   IOException {
        writer.beginObject();

        writer.name("id")
              .value(pager.getId());
        writer.name("size")
              .value(pager.getSize());
        writer.name("position")
              .value(pager.getPosition());

        writer.name("history");
        writer.beginArray();

        for (Integer historyValue : pager.getHistory()) {
            writer.value(historyValue);
        }

        writer.endArray();

        writer.endObject();
    }
}

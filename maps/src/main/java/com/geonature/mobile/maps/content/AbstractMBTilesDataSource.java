package com.geonature.mobile.maps.content;

import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQuery;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.geonature.mobile.maps.settings.LayerSettings;

/**
 * Simple {@link ITilesLayerDataSource} MBTiles SQLite implementation.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
abstract class AbstractMBTilesDataSource
        implements ITilesLayerDataSource {

    private static final String TAG = AbstractMBTilesDataSource.class.getName();

    final LayerSettings mLayerSettings;

    AbstractMBTilesDataSource(LayerSettings mLayerSettings) {
        this.mLayerSettings = mLayerSettings;
    }

    @Nullable
    SQLiteDatabase openDatabase(@NonNull final String path) {
        try {
            return SQLiteDatabase.openDatabase(path,
                                               new LeaklessCursorFactory(),
                                               SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY);
        }
        catch (SQLiteException se) {
            Log.w(TAG,
                  se.getMessage());
        }

        return null;
    }

    @NonNull
    Metadata readMetadata(@NonNull final SQLiteDatabase database) {
        Cursor cursor = database.query("metadata",
                                       new String[] {
                                               "name",
                                               "value"
                                       },
                                       null,
                                       null,
                                       null,
                                       null,
                                       null);

        final Metadata metadata = new Metadata("");

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                final String keyName = cursor.getString(cursor.getColumnIndex("name"));

                switch (keyName) {
                    case "name":
                        metadata.name = cursor.getString(cursor.getColumnIndex("value"));
                        break;
                    case "type":
                        metadata.type = cursor.getString(cursor.getColumnIndex("value"));
                        break;
                    case "version":
                        try {
                            metadata.version = Double.valueOf(cursor.getString(cursor.getColumnIndex("value")));
                        }
                        catch (NumberFormatException nfe) {
                            Log.w(TAG,
                                  nfe.getMessage());
                        }

                        break;
                    case "description":
                        metadata.description = cursor.getString(cursor.getColumnIndex("value"));
                        break;
                    case "format":
                        metadata.format = cursor.getString(cursor.getColumnIndex("value"));
                        break;
                }

                cursor.moveToNext();
            }
        }

        cursor.close();

        return metadata;
    }

    /**
     * Custom implementation of <code>CursorFactory</code> that use {@link MBTilesDataSource.LeaklessCursor} to close
     * automatically database instance.
     *
     * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    private static class LeaklessCursorFactory
            implements SQLiteDatabase.CursorFactory {
        @Override
        public Cursor newCursor(SQLiteDatabase db,
                                SQLiteCursorDriver masterQuery,
                                String editTable,
                                SQLiteQuery query) {
            return new LeaklessCursor(db,
                                      masterQuery,
                                      editTable,
                                      query);
        }
    }

    /**
     * Custom implementation of <code>SQLiteCursor</code> to close automatically database instance.
     *
     * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    private static class LeaklessCursor
            extends SQLiteCursor {

        final SQLiteDatabase mDatabase;

        LeaklessCursor(SQLiteDatabase db,
                       SQLiteCursorDriver driver,
                       String editTable,
                       SQLiteQuery query) {
            super(driver,
                  editTable,
                  query);

            this.mDatabase = db;
        }

        @Override
        public void close() {
            super.close();

            if (mDatabase != null) {
                mDatabase.close();
            }
        }
    }
}

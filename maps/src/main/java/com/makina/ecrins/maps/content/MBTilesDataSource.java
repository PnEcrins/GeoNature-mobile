package com.makina.ecrins.maps.content;

import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQuery;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import com.makina.ecrins.maps.settings.LayerSettings;
import com.makina.ecrins.maps.util.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple {@link ITilesLayerDataSource} MBTiles SQLite implementation.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class MBTilesDataSource
        implements ITilesLayerDataSource {

    private static final String TAG = MBTilesDataSource.class.getSimpleName();

    private final File mMbTiles;
    private final LayerSettings mLayerSettings;
    private Metadata mMetadata;
    private int mMinZoom = Integer.MAX_VALUE;
    private int mMaxZoom = 0;
    private final List<Integer> mZooms = new ArrayList<>();

    public MBTilesDataSource(@NonNull final File sourcePath,
                             @NonNull final LayerSettings pLayerSettings) throws
                                                                          IOException {
        this.mLayerSettings = pLayerSettings;
        this.mMbTiles = FileUtils.getFile(sourcePath,
                                          pLayerSettings.getName());

        final SQLiteDatabase database = openDatabase(this.mMbTiles.getPath());

        if (database == null) {
            throw new IOException("database cannot be opened");
        }

        this.mMetadata = readMetadata(database);

        if (mMbTiles.exists()) {
            Log.d(TAG,
                  "loading MBTiles '" + pLayerSettings.getName() + "'");
        }
        else {
            throw new FileNotFoundException("unable to load MBTiles file from path '" + mMbTiles + "'");
        }
    }

    @NonNull
    @Override
    public Metadata getMetadata() {
        return mMetadata;
    }

    /**
     * (non-Javadoc)
     *
     * @see com.makina.ecrins.maps.content.ITilesLayerDataSource#getMinZoom()
     */
    @Override
    public int getMinZoom() {
        if (mMinZoom == Integer.MAX_VALUE) {
            SQLiteDatabase database = openDatabase(mMbTiles.getPath());

            if (database != null) {
                Cursor cursor = database.rawQuery("SELECT MIN(zoom_level) AS min_zoom FROM tiles",
                                                  null);

                // we should have only one result
                if (cursor.moveToFirst()) {
                    mMinZoom = cursor.getInt(cursor.getColumnIndex("min_zoom"));
                }

                cursor.close();
            }
            else {
                Log.w(TAG,
                      "getMinZoom(): db is null !");
            }
        }

        return mMinZoom;
    }

    /**
     * (non-Javadoc)
     *
     * @see com.makina.ecrins.maps.content.ITilesLayerDataSource#getMaxZoom()
     */
    @Override
    public int getMaxZoom() {
        if (mMaxZoom == 0) {
            SQLiteDatabase database = openDatabase(mMbTiles.getPath());

            if (database != null) {
                Cursor cursor = database.rawQuery("SELECT MAX(zoom_level) AS max_zoom FROM tiles",
                                                  null);

                // we should have only one result
                if (cursor.moveToFirst()) {
                    mMaxZoom = cursor.getInt(cursor.getColumnIndex("max_zoom"));
                }

                cursor.close();
            }
            else {
                Log.w(TAG,
                      "getMaxZoom(): db is null !");
            }
        }

        return mMaxZoom;
    }

    /**
     * (non-Javadoc)
     *
     * @see com.makina.ecrins.maps.content.ITilesLayerDataSource#getZooms()
     */
    @NonNull
    @Override
    public List<Integer> getZooms() {
        if (mZooms.isEmpty()) {
            SQLiteDatabase database = openDatabase(mMbTiles.getPath());

            if (database != null) {
                Cursor cursor = database.rawQuery("SELECT DISTINCT zoom_level AS zooms FROM tiles ORDER BY zoom_level ASC",
                                                  null);

                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        mZooms.add(cursor.getInt(cursor.getColumnIndex("zooms")));

                        cursor.moveToNext();
                    }
                }

                cursor.close();

                Log.d(TAG,
                      mLayerSettings.getName() + " getZooms: " + mZooms.toString());
            }
            else {
                Log.w(TAG,
                      "getZooms(): db is null !");
            }
        }

        return mZooms;
    }

    /**
     * (non-Javadoc)
     *
     * @see com.makina.ecrins.maps.content.ITilesLayerDataSource#getTile(int, int, int)
     */
    @NonNull
    @Override
    public String getTile(int zoomLevel,
                          int column,
                          int row) {
        String tileData = "";

        int currentZoomLevel = zoomLevel;

        // try to load the last zoom level if zoomLevel is too high
        int maxZoom = getMaxZoom();

        if (zoomLevel > maxZoom) {
            currentZoomLevel = maxZoom;
        }

        // invert y axis to top origin
        int yMercator = (1 << currentZoomLevel) - row - 1;

        SQLiteDatabase database = openDatabase(mMbTiles.getPath());

        if (database != null) {
            Cursor cursor = database.query("tiles",
                                           new String[] {"tile_data"},
                                           "zoom_level = ? AND tile_column = ? AND tile_row = ?",
                                           new String[] {
                                                   String.valueOf(currentZoomLevel),
                                                   String.valueOf(column),
                                                   String.valueOf(yMercator)
                                           },
                                           null,
                                           null,
                                           null);

            // we should have only one result
            if (cursor.moveToFirst()) {
                tileData = Base64.encodeToString(cursor.getBlob(cursor.getColumnIndex("tile_data")),
                                                 Base64.DEFAULT);
            }

            cursor.close();
        }
        else {
            Log.w(TAG,
                  "getTile(): db is null !");
        }

        return tileData;
    }

    @NonNull
    protected LayerSettings getLayerSettings() {
        return this.mLayerSettings;
    }

    protected SQLiteDatabase openDatabase(@NonNull final String path) throws
                                                                      SQLiteException {
        return SQLiteDatabase.openDatabase(path,
                                           new LeaklessCursorFactory(),
                                           SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY);
    }

    /**
     * Custom implementation of <code>CursorFactory</code> that use {@link LeaklessCursor} to close
     * automatically database instance.
     *
     * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    public static class LeaklessCursorFactory
            implements CursorFactory {
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
    public static class LeaklessCursor
            extends SQLiteCursor {
        final SQLiteDatabase mDatabase;

        @SuppressWarnings("deprecation")
        public LeaklessCursor(SQLiteDatabase db,
                              SQLiteCursorDriver driver,
                              String editTable,
                              SQLiteQuery query) {
            super(db,
                  driver,
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

    @NonNull
    protected Metadata readMetadata(@NonNull final SQLiteDatabase database) throws
                                                                            IOException {
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
}

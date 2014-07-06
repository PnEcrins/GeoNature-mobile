package com.makina.ecrins.maps.content;

import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQuery;
import android.util.Base64;
import android.util.Log;

import com.makina.ecrins.maps.LayerSettings;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

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
public class MBTilesDataSource implements ITilesLayerDataSource {

    private File mMbtiles;
    private LayerSettings mLayerSettings;

    private JSONObject mMetadata = new JSONObject();
    private int mMinZoom = Integer.MAX_VALUE;
    private int mMaxZoom = 0;
    private final List<Integer> mZooms = new ArrayList<Integer>();

    public MBTilesDataSource(File sourcePath, LayerSettings pLayerSettings) throws IOException {
        this.mLayerSettings = pLayerSettings;
        mMbtiles = FileUtils.getFile(sourcePath, pLayerSettings.getName());

        if (mMbtiles.exists()) {
            Log.d(getClass().getName(), "loading MBTiles '" + pLayerSettings.getName() + "'");
        }
        else {
            throw new FileNotFoundException("unable to load MBTiles file from path '" + mMbtiles + "'");
        }
    }

    /**
     * (non-Javadoc)
     *
     * @see com.makina.ecrins.maps.content.ITilesLayerDataSource#getMetadata()
     */
    @Override
    public JSONObject getMetadata() {
        if (mMetadata.length() == 0) {
            SQLiteDatabase database = openDatabase();

            if (database != null) {
                Cursor cursor = database.query("metadata",
                        new String[]{"name", "value"},
                        null, null, null, null, null);

                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        try {
                            mMetadata.put(cursor.getString(cursor.getColumnIndex("name")), cursor.getString(cursor.getColumnIndex("value")));
                        }
                        catch (JSONException je) {
                            Log.e(getClass().getName(), je.getMessage(), je);
                        }

                        cursor.moveToNext();
                    }
                }

                cursor.close();
            }
            else {
                Log.w(getClass().getName(), "getMetadata() : db is null !");
            }
        }

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
            SQLiteDatabase database = openDatabase();

            if (database != null) {
                Cursor cursor = database.rawQuery("SELECT MIN(zoom_level) AS min_zoom FROM tiles", null);

                // we should have only one result
                if (cursor.moveToFirst()) {
                    mMinZoom = cursor.getInt(cursor.getColumnIndex("min_zoom"));
                }

                cursor.close();
            }
            else {
                Log.w(getClass().getName(), "getMinZoom() : db is null !");
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
            SQLiteDatabase database = openDatabase();

            if (database != null) {
                Cursor cursor = database.rawQuery("SELECT MAX(zoom_level) AS max_zoom FROM tiles", null);

                // we should have only one result
                if (cursor.moveToFirst()) {
                    mMaxZoom = cursor.getInt(cursor.getColumnIndex("max_zoom"));
                }

                cursor.close();
            }
            else {
                Log.w(getClass().getName(), "getMaxZoom() : db is null !");
            }
        }

        return mMaxZoom;
    }

    /**
     * (non-Javadoc)
     *
     * @see com.makina.ecrins.maps.content.ITilesLayerDataSource#getZooms()
     */
    @Override
    public List<Integer> getZooms() {
        if (mZooms.isEmpty()) {
            SQLiteDatabase database = openDatabase();

            if (database != null) {
                Cursor cursor = database.rawQuery("SELECT DISTINCT zoom_level AS zooms FROM tiles ORDER BY zoom_level ASC", null);

                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        mZooms.add(Integer.valueOf(cursor.getInt(cursor.getColumnIndex("zooms"))));

                        cursor.moveToNext();
                    }
                }

                cursor.close();

                Log.d(getClass().getName(), mLayerSettings.getName() + " getZooms : " + mZooms.toString());
            }
            else {
                Log.w(getClass().getName(), "getZooms() : db is null !");
            }
        }

        return mZooms;
    }

    /**
     * (non-Javadoc)
     *
     * @see com.makina.ecrins.maps.content.ITilesLayerDataSource#getTile(int, int, int)
     */
    @Override
    public String getTile(int zoomLevel, int column, int row) {
        String tileData = "";

        int currentZoomLevel = zoomLevel;

        // try to load the last zoom level if zoomLevel is too high
        int maxZoom = getMaxZoom();

        if (zoomLevel > maxZoom) {
            currentZoomLevel = maxZoom;
        }

        // invert y axis to top origin
        int yMercator = (1 << currentZoomLevel) - row - 1;

        //Log.d(getClass().getName(), "getTile [z=" + currentZoomLevel + ", x=" + column + ", y= " + yMercator + "]");

        SQLiteDatabase database = openDatabase();

        if (database != null) {
            Cursor cursor = database.query("tiles",
                    new String[]{"tile_data"},
                    "zoom_level = ? AND tile_column = ? AND tile_row = ?",
                    new String[]{String.valueOf(currentZoomLevel), String.valueOf(column), String.valueOf(yMercator)},
                    null, null, null);

            // we should have only one result
            if (cursor.moveToFirst()) {
                tileData = Base64.encodeToString(cursor.getBlob(cursor.getColumnIndex("tile_data")), Base64.DEFAULT);
            }

            cursor.close();

            //Log.d(getClass().getName(), mLayerSettings.getName() + " getTile size : " + tileData.length());
        }
        else {
            Log.w(getClass().getName(), "getTile() : db is null !");
        }

        //Log.d(getClass().getName(), mLayerSettings.getName() + " getTile data : " + tileData);

        return tileData;
    }

    private SQLiteDatabase openDatabase() throws SQLiteException {
        return SQLiteDatabase.openDatabase(
                mMbtiles.getPath(),
                new LeaklessCursorFactory(),
                SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY);
    }

    /**
     * Custom implementation of <code>CursorFactory</code> that use {@link LeaklessCursor} to close
     * automatically database instance.
     *
     * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    public static class LeaklessCursorFactory implements CursorFactory {
        @Override
        public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver masterQuery, String editTable, SQLiteQuery query) {
            return new LeaklessCursor(db, masterQuery, editTable, query);
        }
    }

    /**
     * Custom implementation of <code>SQLiteCursor</code> to close automatically database instance.
     *
     * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    public static class LeaklessCursor extends SQLiteCursor {
        final SQLiteDatabase mDatabase;

        @SuppressWarnings("deprecation")
        public LeaklessCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
            super(db, driver, editTable, query);
            this.mDatabase = db;
        }

        @Override
        public void close() {
            //Log.d(getClass().getName(), "Closing LeaklessCursor : '" + mDatabase.getPath() + "'");

            super.close();

            if (mDatabase != null) {
                mDatabase.close();
            }
        }
    }
}

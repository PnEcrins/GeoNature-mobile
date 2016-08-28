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
import android.util.SparseArray;

import com.makina.ecrins.maps.settings.LayerSettings;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * {@link ITilesLayerDataSource} implementation to read a set of split MBTiles files by 'x'
 * parameter (the column parameter).
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class MBTilesSplitDataSource implements ITilesLayerDataSource {

    private File mbTilesDirectory = null;
    private final LayerSettings mLayerSettings;
    private final JSONObject mMetadata = new JSONObject();
    private int mMinZoom = Integer.MAX_VALUE;
    private int mMaxZoom = 0;
    private final SortedSet<Integer> mZooms = new TreeSet<>();

    private final SparseArray<String> mMbTilesPath = new SparseArray<>();

    public MBTilesSplitDataSource(File sourcePath, LayerSettings pLayerSettings) throws IOException {
        this.mLayerSettings = pLayerSettings;
        mbTilesDirectory = FileUtils.getFile(sourcePath, pLayerSettings.getName());

        if (mbTilesDirectory.exists() && mbTilesDirectory.isDirectory()) {
            Log.d(getClass().getName(), "loading MBTiles from path '" + pLayerSettings.getName() + "'");
        }
        else {
            throw new FileNotFoundException("unable to load MBTiles from path '" + mbTilesDirectory + "'");
        }
    }

    @Override
    public JSONObject getMetadata() {
        if (mMetadata.length() == 0) {
            SQLiteDatabase database = openDatabase(0);

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

    @Override
    public int getMinZoom() {
        if (mMinZoom == Integer.MAX_VALUE) {
            SQLiteDatabase database = openDatabase(0);

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

    @Override
    public int getMaxZoom() {
        if (mMaxZoom == 0) {
            for (int i = 0; i < 10; i++) {
                SQLiteDatabase database = openDatabase(i);

                if (database != null) {
                    Cursor cursor = database.rawQuery("SELECT MAX(zoom_level) AS max_zoom FROM tiles", null);

                    // we should have only one result
                    if (cursor.moveToFirst()) {
                        int maxZoom = cursor.getInt(cursor.getColumnIndex("max_zoom"));
                        mMaxZoom = (maxZoom > mMaxZoom) ? maxZoom : mMaxZoom;
                    }

                    cursor.close();
                }
                else {
                    Log.w(getClass().getName(), "getMaxZoom() : db is null !");
                }
            }
        }

        return mMaxZoom;
    }

    @Override
    public List<Integer> getZooms() {
        if (mZooms.isEmpty()) {
            for (int i = 0; i < 10; i++) {
                SQLiteDatabase database = openDatabase(i);

                if (database != null) {
                    Cursor cursor = database.rawQuery("SELECT DISTINCT zoom_level AS zooms FROM tiles ORDER BY zoom_level ASC", null);

                    if (cursor.moveToFirst()) {
                        while (!cursor.isAfterLast()) {
                            mZooms.add(cursor.getInt(cursor.getColumnIndex("zooms")));

                            cursor.moveToNext();
                        }
                    }

                    cursor.close();
                }
                else {
                    Log.w(getClass().getName(), "getZooms() : db is null !");
                }
            }

            Log.d(getClass().getName(), mLayerSettings.getName() + " getZooms : " + mZooms.toString());
        }

        return new ArrayList<>(mZooms);
    }

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

        SQLiteDatabase database = openDatabase(column);

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

    private String getMBTilesPath(int column) {
        if (mMbTilesPath.get(column) == null) {
            String columnAsString = Integer.toString(column);
            String mbTilesSuffix = (columnAsString.startsWith("-")) ? columnAsString.substring(1, 2) : columnAsString.substring(0, 1);

            File mbTiles = FileUtils.getFile(mbTilesDirectory, mbTilesDirectory.getName() + "-" + mbTilesSuffix + ".mbtiles");

            if (mbTiles.exists() && mbTiles.isFile()) {
                mMbTilesPath.put(column, mbTiles.getPath());
                return mMbTilesPath.get(column);
            }
            else {
                throw new SQLiteException("unable to load MBTiles for column " + column);
            }
        }
        else {
            return mMbTilesPath.get(column);
        }
    }

    private SQLiteDatabase openDatabase(int column) throws SQLiteException {
        return SQLiteDatabase.openDatabase(
                getMBTilesPath(column),
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

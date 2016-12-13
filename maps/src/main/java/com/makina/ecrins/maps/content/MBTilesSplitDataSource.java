package com.makina.ecrins.maps.content;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;

import com.makina.ecrins.maps.settings.LayerSettings;
import com.makina.ecrins.maps.util.FileUtils;

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
public class MBTilesSplitDataSource
        extends MBTilesDataSource {

    private static final String TAG = MBTilesSplitDataSource.class.getSimpleName();

    private File mbTilesDirectory = null;
    private Metadata mMetadata;
    private int mMinZoom = Integer.MAX_VALUE;
    private int mMaxZoom = 0;
    private final SortedSet<Integer> mZooms = new TreeSet<>();

    private final SparseArray<String> mMbTilesPath = new SparseArray<>();

    public MBTilesSplitDataSource(@NonNull final File sourcePath,
                                  @NonNull final LayerSettings pLayerSettings) throws
                                                                               IOException {
        super(sourcePath,
              pLayerSettings);

        mbTilesDirectory = FileUtils.getFile(sourcePath,
                                             pLayerSettings.getName());

        if (mbTilesDirectory.exists() && mbTilesDirectory.isDirectory()) {
            Log.d(getClass().getName(),
                  "loading MBTiles from path '" + pLayerSettings.getName() + "'");
        }
        else {
            throw new FileNotFoundException("unable to load MBTiles from path '" + mbTilesDirectory + "'");
        }
    }

    @Override
    public int getMinZoom() {
        if (mMinZoom == Integer.MAX_VALUE) {
            SQLiteDatabase database = openDatabase(getMBTilesPath(0));

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
                Log.w(getClass().getName(),
                      "getMinZoom() : db is null !");
            }
        }

        return mMinZoom;
    }

    @Override
    public int getMaxZoom() {
        if (mMaxZoom == 0) {
            for (int i = 0; i < 10; i++) {
                SQLiteDatabase database = openDatabase(getMBTilesPath(i));

                if (database != null) {
                    Cursor cursor = database.rawQuery("SELECT MAX(zoom_level) AS max_zoom FROM tiles",
                                                      null);

                    // we should have only one result
                    if (cursor.moveToFirst()) {
                        int maxZoom = cursor.getInt(cursor.getColumnIndex("max_zoom"));
                        mMaxZoom = (maxZoom > mMaxZoom) ? maxZoom : mMaxZoom;
                    }

                    cursor.close();
                }
                else {
                    Log.w(TAG,
                          "getMaxZoom(): db is null !");
                }
            }
        }

        return mMaxZoom;
    }

    @NonNull
    @Override
    public List<Integer> getZooms() {
        if (mZooms.isEmpty()) {
            for (int i = 0; i < 10; i++) {
                SQLiteDatabase database = openDatabase(getMBTilesPath(i));

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
                }
                else {
                    Log.w(TAG,
                          "getZooms(): db is null !");
                }
            }

            Log.d(TAG,
                  getLayerSettings().getName() + " getZooms : " + mZooms.toString());
        }

        return new ArrayList<>(mZooms);
    }

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

        SQLiteDatabase database = openDatabase(getMBTilesPath(column));

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
                  "getTile(): db is null!");
        }

        return tileData;
    }

    @NonNull
    @Override
    protected Metadata readMetadata(@NonNull SQLiteDatabase database) throws
                                                                      IOException {
        return super.readMetadata(openDatabase(getMBTilesPath(0)));
    }

    @NonNull
    private String getMBTilesPath(int column) {
        if (mMbTilesPath.get(column) == null) {
            String columnAsString = Integer.toString(column);
            String mbTilesSuffix = (columnAsString.startsWith("-")) ? columnAsString.substring(1,
                                                                                               2) : columnAsString.substring(0,
                                                                                                                             1);

            File mbTiles = FileUtils.getFile(mbTilesDirectory,
                                             mbTilesDirectory.getName() + "-" + mbTilesSuffix + ".mbtiles");

            if (mbTiles.exists() && mbTiles.isFile()) {
                mMbTilesPath.put(column,
                                 mbTiles.getPath());
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
}

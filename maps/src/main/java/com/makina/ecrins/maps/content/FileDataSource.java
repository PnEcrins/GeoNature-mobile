package com.makina.ecrins.maps.content;

import android.util.Base64;
import android.util.Log;

import com.makina.ecrins.maps.LayerSettings;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Simple {@link ITilesLayerDataSource} filesytem implementation.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class FileDataSource implements ITilesLayerDataSource {

    private File mapDirectory = null;
    private LayerSettings mLayerSettings;

    private JSONObject mMetadata = new JSONObject();
    private int mMinZoom = Integer.MAX_VALUE;
    private int mMaxZoom = 0;
    private final List<Integer> mZooms = new ArrayList<Integer>();

    public FileDataSource(File sourcePath, LayerSettings pLayerSettings) throws IOException {
        this.mLayerSettings = pLayerSettings;
        mapDirectory = FileUtils.getFile(sourcePath, pLayerSettings.getName());

        if (mapDirectory.exists() && mapDirectory.isDirectory()) {
            Log.d(getClass().getName(), "loading tiles from path '" + pLayerSettings.getName() + "'");
        }
        else {
            throw new FileNotFoundException("unable to load tiles from path '" + mapDirectory + "'");
        }
    }

    @Override
    public JSONObject getMetadata() {
        if (mMetadata.length() == 0) {
            File fileContentMedata = new File(this.mapDirectory.getAbsolutePath() + File.separator + "metadata.json");

            try {
                mMetadata = new JSONObject(FileUtils.readFileToString(fileContentMedata));
            }
            catch (JSONException je) {
                Log.e(getClass().getName(), je.getMessage(), je);
            }
            catch (IOException ioe) {
                Log.e(getClass().getName(), ioe.getMessage(), ioe);
            }
        }

        return mMetadata;
    }

    @Override
    public int getMinZoom() {
        if (mMinZoom == Integer.MAX_VALUE) {
            if (getZooms().isEmpty()) {
                Log.w(getClass().getName(), "getMinZoom : no zooms available for " + mLayerSettings.getName());
            }
            else {
                mMinZoom = getZooms().get(0);
            }
        }

        return mMinZoom;
    }

    @Override
    public int getMaxZoom() {
        if (mMaxZoom == 0) {
            if (getZooms().isEmpty()) {
                Log.w(getClass().getName(), "getMaxZoom : no zooms available for " + mLayerSettings.getName());
            }
            else {
                mMaxZoom = getZooms().get(getZooms().size() - 1);
            }
        }

        return mMaxZoom;
    }

    @Override
    public List<Integer> getZooms() {
        if (mZooms.isEmpty()) {
            try {
                String name = (String) getMetadata().get(KEY_NAME);
                String version = (String) getMetadata().get(KEY_VERSION);

                File contents = new File(this.mapDirectory.getAbsolutePath() + File.separator + version + File.separator + name);

                for (File zoomLevel : contents.listFiles()) {
                    mZooms.add(Integer.valueOf(zoomLevel.getName()));
                }

                Arrays.sort(mZooms.toArray(new Integer[]{}));

                Log.d(getClass().getName(), mLayerSettings.getName() + " getZooms : " + mZooms.toString());
            }
            catch (JSONException je) {
                Log.e(getClass().getName(), je.getMessage(), je);
            }
        }

        return mZooms;
    }

    @Override
    public String getTile(int zoomLevel, int column, int row) {
        String tileData = "";

        try {
            String name = (String) getMetadata().get(KEY_NAME);
            String version = (String) getMetadata().get(KEY_VERSION);
            String format = (String) getMetadata().get(KEY_FORMAT);

            int currentZoomLevel = zoomLevel;

            // try to load the last zoom level if zoomLevel is too high
            int maxZoom = getMaxZoom();

            if (zoomLevel > maxZoom) {
                currentZoomLevel = maxZoom;
            }

            // invert y axis to top origin
            int yMercator = (1 << currentZoomLevel) - row - 1;


            File tileFile = new File(this.mapDirectory.getAbsolutePath() + File.separator + version + File.separator + name + File.separator + String.valueOf(currentZoomLevel) + File.separator + String.valueOf(column) + File.separator + String.valueOf(yMercator) + "." + format);

            //Log.d(getClass().getName(), "try to get tile from path " + tileFile.getAbsolutePath());

            if (tileFile.exists()) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                IOUtils.copy(new FileInputStream(tileFile), baos);
                tileData = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

                baos.close();
            }
        }
        catch (JSONException je) {
            Log.e(getClass().getName(), je.getMessage(), je);
        }
        catch (FileNotFoundException fnfe) {
            Log.e(getClass().getName(), fnfe.getMessage(), fnfe);
        }
        catch (IOException ioe) {
            Log.e(getClass().getName(), ioe.getMessage(), ioe);
        }

        return tileData;
    }
}

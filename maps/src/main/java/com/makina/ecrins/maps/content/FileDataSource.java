package com.makina.ecrins.maps.content;

import android.support.annotation.NonNull;
import android.util.JsonReader;
import android.util.Log;

import com.makina.ecrins.maps.BuildConfig;
import com.makina.ecrins.maps.settings.LayerSettings;
import com.makina.ecrins.maps.util.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Simple {@link ITilesLayerDataSource} filesystem implementation.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class FileDataSource
        implements ITilesLayerDataSource {

    private static final String TAG = FileDataSource.class.getName();

    private File mapDirectory = null;
    private final LayerSettings mLayerSettings;
    private final Metadata mMetadata;
    private int mMinZoom = Integer.MAX_VALUE;
    private int mMaxZoom = 0;
    private final List<Integer> mZooms = new ArrayList<>();

    public FileDataSource(@NonNull final File sourcePath,
                          @NonNull final LayerSettings pLayerSettings) throws
                                                        IOException {
        this.mLayerSettings = pLayerSettings;
        this.mapDirectory = FileUtils.getFile(sourcePath,
                                         pLayerSettings.getName());
        this.mMetadata = readMetadata();

        if (mapDirectory.exists() && mapDirectory.isDirectory()) {
            Log.d(TAG,
                  "loading tiles from path '" + pLayerSettings.getName() + "'");
        }
        else {
            throw new FileNotFoundException("unable to load tiles from path '" + mapDirectory + "'");
        }
    }

    @NonNull
    @Override
    public Metadata getMetadata() {
        return mMetadata;
    }

    @Override
    public int getMinZoom() {
        if (mMinZoom == Integer.MAX_VALUE) {
            if (getZooms().isEmpty()) {
                Log.w(TAG,
                      "getMinZoom: no zooms available for " + mLayerSettings.getName());
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
                Log.w(TAG,
                      "getMaxZoom: no zooms available for " + mLayerSettings.getName());
            }
            else {
                mMaxZoom = getZooms().get(getZooms().size() - 1);
            }
        }

        return mMaxZoom;
    }

    @NonNull
    @Override
    public List<Integer> getZooms() {
        if (mZooms.isEmpty()) {
            final File contents = new File(this.mapDirectory.getAbsolutePath() + File.separator + getMetadata().getVersion() + File.separator + getMetadata().getName());

            for (File zoomLevel : contents.listFiles()) {
                mZooms.add(Integer.valueOf(zoomLevel.getName()));
            }

            Arrays.sort(mZooms.toArray(new Integer[mZooms.size()]));

            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      mLayerSettings.getName() + " getZooms: " + mZooms.toString());
            }
        }

        return mZooms;
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

        File tileFile = new File(this.mapDirectory.getAbsolutePath() + File.separator + getMetadata().getVersion() + File.separator + getMetadata().getName() + File.separator + String.valueOf(currentZoomLevel) + File.separator + String.valueOf(column) + File.separator + String.valueOf(yMercator) + "." + getMetadata().getFormat());

        if (tileFile.exists()) {
            try {
                tileData = FileUtils.toBase64(new FileInputStream(tileFile));
            }
            catch (FileNotFoundException fnfe) {
                Log.w(TAG,
                      fnfe.getMessage());
            }
        }

        return tileData;
    }

    @NonNull
    private Metadata readMetadata() throws
                                    IOException {
        final FileReader fileReader = new FileReader(new File(this.mapDirectory.getAbsolutePath() +
                                                                      File.separator +
                                                                      "metadata.json"));
        final JsonReader jsonReader = new JsonReader(fileReader);
        jsonReader.beginObject();

        final Metadata metadata = new Metadata("");

        while (jsonReader.hasNext()) {
            final String keyName = jsonReader.nextName();

            switch (keyName) {
                case "name":
                    metadata.name = jsonReader.nextString();
                    break;
                case "type":
                    metadata.type = jsonReader.nextString();
                    break;
                case "version":
                    metadata.version = jsonReader.nextDouble();
                    break;
                case "description":
                    metadata.description = jsonReader.nextString();
                    break;
                case "format":
                    metadata.format = jsonReader.nextString();
                    break;
            }
        }

        jsonReader.endObject();
        jsonReader.close();

        return metadata;
    }
}

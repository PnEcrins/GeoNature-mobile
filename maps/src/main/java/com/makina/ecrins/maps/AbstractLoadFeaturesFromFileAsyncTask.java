package com.makina.ecrins.maps;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.makina.ecrins.maps.jts.geojson.Feature;
import com.makina.ecrins.maps.jts.geojson.FeatureCollection;
import com.makina.ecrins.maps.jts.geojson.io.WKTFileReader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code AsyncTask} implementation to load all {@link Feature} from a given file
 * (<a href="http://en.wikipedia.org/wiki/Well-known_text">WKT format</a>).
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public abstract class AbstractLoadFeaturesFromFileAsyncTask
        extends AsyncTask<File, Integer, List<Feature>> {

    private static final String TAG = AbstractLoadFeaturesFromFileAsyncTask.class.getSimpleName();

    private String mFilename;

    public AbstractLoadFeaturesFromFileAsyncTask() {

        super();

        this.mFilename = "";
    }

    protected abstract int whatLoadingStart();

    protected abstract int whatLoading();

    protected abstract int whatLoadingFailed();

    protected abstract int whatLoadingLoaded();

    protected abstract void sendMessage(int what,
                                        Object obj);

    protected abstract void sendProgress(int what,
                                         int progress,
                                         int max);

    /*
     * (non-Javadoc)
     * @see android.os.AsyncTask#doInBackground(Params[])
     */
    @Override
    protected List<Feature> doInBackground(File... params) {

        this.mFilename = params[0].getName();

        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "loading features from '" + params[0] + "' ...");
        }

        final List<Feature> unities = new ArrayList<>();

        if (params[0].exists()) {
            new WKTFileReader().readFeatures(params[0],
                                             new WKTFileReader.OnWKTFileReaderListener() {
                                                 @Override
                                                 public void onStart(int size) {
                                                     if (BuildConfig.DEBUG) {
                                                         Log.d(TAG,
                                                               "number of features to load: " + size);
                                                     }

                                                     sendMessage(whatLoadingStart(),
                                                                 size);
                                                 }

                                                 @Override
                                                 public void onProgress(int progress,
                                                                        int size,
                                                                        @NonNull Feature feature) {
                                                     publishProgress(progress,
                                                                     size);
                                                     unities.add(feature);
                                                 }

                                                 @Override
                                                 public void onProgress(int progress,
                                                                        @NonNull Feature feature) {
                                                 }

                                                 @Override
                                                 public void onFinish(@NonNull FeatureCollection featureCollection) {
                                                     if (BuildConfig.DEBUG) {
                                                         Log.d(TAG,
                                                               unities.size() + " features loaded");
                                                     }
                                                 }

                                                 @Override
                                                 public void onError(Throwable t) {
                                                     Log.w(TAG,
                                                           t.getMessage(),
                                                           t);
                                                 }
                                             });
        }
        else {
            Log.w(TAG,
                  "unable to load features from path '" + params[0].getPath() + "'");
        }

        return unities;
    }

    /*
     * (non-Javadoc)
     * @see android.os.AsyncTask#onProgressUpdate(Progress[])
     */
    @Override
    protected void onProgressUpdate(Integer... values) {

        sendProgress(whatLoading(),
                     values[0],
                     values[1]);
    }

    /*
     * (non-Javadoc)
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute(List<Feature> result) {

        if (result.isEmpty()) {
            sendMessage(whatLoadingFailed(),
                        this.mFilename);
        }
        else {
            sendMessage(whatLoadingLoaded(),
                        result);
        }
    }
}

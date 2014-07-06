package com.makina.ecrins.maps;

import android.os.AsyncTask;
import android.util.Log;

import com.makina.ecrins.maps.geojson.Feature;
import com.makina.ecrins.maps.geojson.GeoJSONType;
import com.makina.ecrins.maps.geojson.geometry.GeoPoint;
import com.makina.ecrins.maps.geojson.geometry.Point;
import com.makina.ecrins.maps.geojson.geometry.Polygon;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <code>AsyncTask</code> implementation to load all {@link Feature} from a given file
 * (<a href="http://en.wikipedia.org/wiki/Well-known_text">WKT format</a>).
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public abstract class AbstractLoadFeaturesFromFileAsyncTask extends AsyncTask<File, Integer, List<Feature>> {

    private String mFilename;

    public AbstractLoadFeaturesFromFileAsyncTask() {
        super();

        this.mFilename = "";
    }

    protected abstract int whatLoadingStart();

    protected abstract int whatLoading();

    protected abstract int whatLoadingFailed();

    protected abstract int whatLoadingLoaded();

    protected abstract void sendMessage(int what, Object obj);

    protected abstract void sendProgress(int what, int progress, int max);

    /*
     * (non-Javadoc)
     * @see android.os.AsyncTask#doInBackground(Params[])
     */
    @Override
    protected List<Feature> doInBackground(File... params) {
        Log.d(getClass().getName(), "loading features from '" + params[0] + "' ...");

        List<Feature> unities = new ArrayList<Feature>();
        BufferedReader input = null;

        try {
            if (params[0].exists()) {
                // count the number of lines (i.e. the number of unities to load)
                int numberOfLines = FileUtils.readLines(params[0])
                        .size();

                if (numberOfLines > 0) {
                    int currentLine = 0;

                    sendMessage(whatLoadingStart(), numberOfLines);

                    Log.d(getClass().getName(), "number of features to load : " + numberOfLines);

                    input = new BufferedReader(new FileReader(params[0]));
                    String unityAsString = null;

                    Pattern p0 = Pattern.compile("^([0-9]+),([A-Z]+)(\\(.+\\))$");
                    Pattern p2 = Pattern.compile("([0-9]+\\.[0-9]+ [0-9]+\\.[0-9]+)");

                    while ((unityAsString = input.readLine()) != null) {
                        publishProgress(currentLine, numberOfLines);

                        Matcher m0 = p0.matcher(unityAsString);

                        if (m0.matches()) {
                            Feature currentUnity = new Feature(m0.group(1));
                            String geometry = m0.group(2);

                            if (geometry.equalsIgnoreCase(GeoJSONType.POLYGON.getValue())) {
                                String[] polygonsAsString = m0.group(3)
                                        .split("\\),\\(");

                                for (int i = 0; i < polygonsAsString.length; i++) {
                                    List<Point> polygon = new ArrayList<Point>();
                                    Matcher m2 = p2.matcher(polygonsAsString[i]);

                                    while (m2.find()) {
                                        String[] coordinates = m2.group()
                                                .split(" ");
                                        polygon.add(new Point(new GeoPoint(Double.valueOf(coordinates[1]), Double.valueOf(coordinates[0]))));
                                    }

                                    // build the main polygon
                                    if (i == 0) {
                                        currentUnity.setGeometry(new Polygon(polygon));
                                    }
                                    else {
                                        // consider other polygons as holes
                                        ((Polygon) currentUnity.getGeometry()).addHole(polygon);
                                    }
                                }
                            }

                            unities.add(currentUnity);
                        }

                        currentLine++;
                    }
                }
            }
            else {
                Log.w(getClass().getName(), "unable to load features from path '" + params[0].getPath() + "'");
            }
        }
        catch (IOException ioe) {
            Log.e(getClass().getName(), ioe.getMessage(), ioe);

            return unities;
        }
        finally {
            if (input != null) {
                try {
                    input.close();
                }
                catch (IOException ioe) {
                    Log.e(getClass().getName(), ioe.getMessage(), ioe);

                    return unities;
                }
            }
        }

        Log.d(getClass().getName(), unities.size() + " features loaded");

        return unities;
    }

    /*
     * (non-Javadoc)
     * @see android.os.AsyncTask#onProgressUpdate(Progress[])
     */
    @Override
    protected void onProgressUpdate(Integer... values) {
        sendProgress(whatLoading(), values[0], values[1]);
    }

    /*
     * (non-Javadoc)
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute(List<Feature> result) {
        if (result.isEmpty()) {
            sendMessage(whatLoadingFailed(), this.mFilename);
        }
        else {
            sendMessage(whatLoadingLoaded(), result);
        }
    }
}

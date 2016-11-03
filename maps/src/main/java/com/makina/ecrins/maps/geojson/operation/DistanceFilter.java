package com.makina.ecrins.maps.geojson.operation;

import android.util.Log;

import com.makina.ecrins.maps.geojson.Feature;
import com.makina.ecrins.maps.geojson.IFeatureFilterVisitor;
import com.makina.ecrins.maps.geojson.geometry.GeoPoint;
import com.makina.ecrins.maps.geojson.geometry.GeometryUtils;
import com.makina.ecrins.maps.geojson.geometry.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Gets an ordered <code>list</code> of nearest {@link Feature}s located at a given distance of a
 * given {@link GeoPoint}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
@Deprecated
public class DistanceFilter implements IFeatureFilterVisitor {

    private final Map<Double, Feature> mFeatures;
    private final GeoPoint mPoint;
    private final double mDistance;

    public DistanceFilter(GeoPoint point, double distance) {
        this.mFeatures = new TreeMap<>();
        this.mPoint = point;
        this.mDistance = distance;
    }

    @Override
    public void filter(Feature feature) {
        double distanceFromFeature = GeometryUtils.distanceTo(new Point(mPoint), feature.getGeometry());

        if (mDistance >= distanceFromFeature) {
            Log.d(getClass().getName(), "feature found '" + feature.getId() + "', distance : " + distanceFromFeature);

            mFeatures.put(distanceFromFeature, feature);
        }
    }

    public List<Feature> getFilteredFeatures() {
        return new ArrayList<>(this.mFeatures.values());
    }
}

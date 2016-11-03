package com.makina.ecrins.maps.jts.geojson.filter;

import android.support.annotation.NonNull;

import com.makina.ecrins.maps.jts.geojson.Feature;
import com.makina.ecrins.maps.jts.geojson.FeatureCollection;
import com.makina.ecrins.maps.jts.geojson.GeoPoint;
import com.makina.ecrins.maps.jts.geojson.GeometryUtils;
import com.makina.ecrins.maps.jts.geojson.IFeatureFilterVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Gets an ordered {@code List} of nearest {@link Feature}s located at a given distance (in meters)
 * of a given {@link GeoPoint}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class NearestFeaturesFilter
        implements IFeatureFilterVisitor {

    private final Map<Double, Feature> features;
    private final GeoPoint geoPoint;
    private final double maxDistance;

    @NonNull
    public static List<Feature> getFilteredFeatures(@NonNull final GeoPoint geoPoint,
                                                    double maxDistance,
                                                    @NonNull final List<Feature> features) {
        final NearestFeaturesFilter nearestFeaturesFilter = new NearestFeaturesFilter(geoPoint,
                                                                                      maxDistance);

        for (Feature feature : features) {
            feature.apply(nearestFeaturesFilter);
        }

        return nearestFeaturesFilter.getFilteredFeatures();
    }

    @NonNull
    public static List<Feature> getFilteredFeatures(@NonNull final GeoPoint geoPoint,
                                                    double maxDistance,
                                                    @NonNull final FeatureCollection featureCollection) {
        final NearestFeaturesFilter nearestFeaturesFilter = new NearestFeaturesFilter(geoPoint,
                                                                                      maxDistance);
        featureCollection.apply(nearestFeaturesFilter);

        return nearestFeaturesFilter.getFilteredFeatures();
    }

    public NearestFeaturesFilter(@NonNull final GeoPoint geoPoint,
                                 double maxDistance) {
        this.features = new TreeMap<>();
        this.geoPoint = geoPoint;
        this.maxDistance = maxDistance;
    }

    @Override
    public void filter(@NonNull Feature feature) {
        final double distanceFromFeature = GeometryUtils.distanceTo(geoPoint.getPoint(),
                                                                    feature.getGeometry());

        if (this.maxDistance >= distanceFromFeature) {
            features.put(distanceFromFeature,
                         feature);
        }
    }

    @NonNull
    public List<Feature> getFilteredFeatures() {
        return new ArrayList<>(this.features.values());
    }
}

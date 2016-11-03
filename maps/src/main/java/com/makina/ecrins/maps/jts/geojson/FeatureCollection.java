package com.makina.ecrins.maps.jts.geojson;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Describes a {@link FeatureCollection} object as a {@code List} of {@link Feature}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class FeatureCollection
        extends AbstractGeoJson
        implements Parcelable {

    private final Map<String, Feature> features = new HashMap<>();

    public FeatureCollection() {
        super();
    }

    public FeatureCollection(Parcel source) {
        final List<Feature> features = new ArrayList<>();
        source.readTypedList(features,
                             Feature.CREATOR);

        for (Feature feature : features) {
            this.features.put(feature.getId(),
                              feature);
        }
    }

    @NonNull
    public List<Feature> getFeatures() {
        return new ArrayList<>(features.values());
    }

    @NonNull
    public Feature getFeature(@NonNull final String featureId) {
        return features.get(featureId);
    }

    public boolean hasFeature(@NonNull final String featureId) {
        return features.containsKey(featureId);
    }

    public void addFeature(@NonNull final Feature feature) {
        features.put(feature.getId(),
                     feature);
    }

    public void removeFeature(@NonNull final String featureId) {
        features.remove(featureId);
    }

    public void clearAllFeatures() {
        features.clear();
    }

    public boolean isEmpty() {
        return features.isEmpty();
    }

    /**
     * Performs an operation on all {@link Feature}s of this collection.
     *
     * @param filter the filter to apply to all these {@link Feature}s
     */
    public void apply(IFeatureFilterVisitor filter) {
        final List<Feature> features = getFeatures();

        for (Feature feature : features) {
            feature.apply(filter);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest,
                              int flags) {
        dest.writeTypedList(new ArrayList<>(features.values()));
    }

    public static final Parcelable.Creator<FeatureCollection> CREATOR = new Parcelable.Creator<FeatureCollection>() {
        @Override
        public FeatureCollection createFromParcel(Parcel source) {
            return new FeatureCollection(source);
        }

        @Override
        public FeatureCollection[] newArray(int size) {
            return new FeatureCollection[size];
        }
    };
}

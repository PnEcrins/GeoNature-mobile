package com.makina.ecrins.maps.geojson;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Describes a {@link FeatureCollection} object as a <code>List</code> of {@link Feature}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
@Deprecated
public class FeatureCollection implements Parcelable {

    private final Map<String, Feature> mFeatures = new HashMap<>();

    public FeatureCollection() {
        super();
    }

    public FeatureCollection(List<Feature> pFeatures) {
        for (Feature feature : pFeatures) {
            this.mFeatures.put(feature.getId(), feature);
        }
    }

    public FeatureCollection(Parcel source) {
        final List<Feature> features = new ArrayList<>();
        source.readTypedList(features, Feature.CREATOR);

        for (Feature feature : features) {
            mFeatures.put(feature.getId(), feature);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(new ArrayList<>(mFeatures.values()));
    }

    public GeoJSONType getType() {
        return GeoJSONType.FEATURE_COLLECTION;
    }

    public List<Feature> getFeatures() {
        return new ArrayList<>(mFeatures.values());
    }

    public Feature getFeature(String featureId) {
        return this.mFeatures.get(featureId);
    }

    public boolean hasFeature(String featureId) {
        return this.mFeatures.containsKey(featureId);
    }

    public void addFeature(Feature pFeature) {
        this.mFeatures.put(pFeature.getId(), pFeature);
    }

    public void removeFeature(String featureId) {
        this.mFeatures.remove(featureId);
    }

    public void clearAllFeatures() {
        this.mFeatures.clear();
    }

    public boolean isEmpty() {
        return this.mFeatures.isEmpty();
    }

    /**
     * Performs an operation on all {@link Feature}s of this collection.
     *
     * @param filter the filter to apply to all these {@link Feature}s
     */
    public void apply(IFeatureFilterVisitor filter) {
        for (Feature feature : mFeatures.values()) {
            feature.apply(filter);
        }
    }

    public JSONObject getJSONObject() throws JSONException {
        JSONObject json = new JSONObject();
        JSONArray features = new JSONArray();

        for (Feature feature : getFeatures()) {
            features.put(feature.getJSONObject());
        }

        json.put("type", getType().getValue());
        json.put("features", features);

        return json;
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

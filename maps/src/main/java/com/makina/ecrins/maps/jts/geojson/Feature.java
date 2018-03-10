package com.makina.ecrins.maps.jts.geojson;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.vividsolutions.jts.geom.Geometry;

/**
 * {@code GeoJSON} {@link Feature} object.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class Feature
        extends AbstractGeoJson
        implements Parcelable {

    private final String id;
    private final Geometry geometry;
    private Bundle properties = new Bundle();

    public Feature(@NonNull final String id,
                   @NonNull final Geometry geometry) {
        this.id = id;
        this.geometry = geometry;
    }

    protected Feature(Parcel source) {
        id = source.readString();
        type = source.readString();
        geometry = (Geometry) source.readSerializable();
        properties = source.readBundle(Bundle.class.getClassLoader());
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public Geometry getGeometry() {
        return geometry;
    }

    @NonNull
    public Bundle getProperties() {
        return properties;
    }

    /**
     * Performs an operation on a given {@link Feature}.
     *
     * @param filter the filter to apply to this {@link Feature}
     */
    public void apply(IFeatureFilterVisitor filter) {
        filter.filter(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest,
                              int flags) {
        dest.writeString(id);
        dest.writeString(type);
        dest.writeSerializable(geometry);
        dest.writeBundle(properties);
    }

    public static final Parcelable.Creator<Feature> CREATOR = new Parcelable.Creator<Feature>() {
        @Override
        public Feature createFromParcel(Parcel source) {
            return new Feature(source);
        }

        @Override
        public Feature[] newArray(int size) {
            return new Feature[size];
        }
    };
}

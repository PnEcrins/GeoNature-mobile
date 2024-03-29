package com.makina.ecrins.maps.jts.geojson;

import android.support.annotation.NonNull;

/**
 * Base {@code GeoJSON} object.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public abstract class AbstractGeoJson {

    String type;

    public AbstractGeoJson() {
        this.type = getClass().getSimpleName();
    }

    @NonNull
    public String getType() {
        return type;
    }
}

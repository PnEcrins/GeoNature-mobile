package com.geonature.mobile.maps.jts.geojson;

import android.support.annotation.NonNull;

/**
 * Visitor pattern to apply a concrete filter to a given {@link Feature}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public interface IFeatureFilterVisitor {

    /**
     * Performs an operation on a given {@link Feature}.
     *
     * @param feature a {@link Feature} instance to which the filter is applied
     */
    void filter(@NonNull final Feature feature);
}

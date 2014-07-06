package com.makina.ecrins.maps.geojson;

/**
 * Visitor pattern to apply a concrete filter to a given {@link Feature}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public interface IFeatureFilterVisitor {

    /**
     * Performs an operation on a given {@link Feature}.
     *
     * @param feature a {@link Feature} instance to which the filter is applied
     */
    public void filter(Feature feature);
}

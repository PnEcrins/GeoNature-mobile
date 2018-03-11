package com.geonature.mobile.maps.content;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * Describes a tiles layer data source.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public interface ITilesLayerDataSource {

    /**
     * Gets all metadata for the given tiles layer source.
     *
     * @return the metadata
     */
    @NonNull
    Metadata getMetadata();

    /**
     * Gets the minimum zoom level.
     *
     * @return the minimum zoom level
     */
    int getMinZoom();

    /**
     * Gets the maximum zoom level.
     *
     * @return the maximum zoom level
     */
    int getMaxZoom();

    /**
     * Gets all available zooms level as {@link List}.
     *
     * @return a {@link List} of available zooms
     */
    @NonNull
    List<Integer> getZooms();

    /**
     * Retrieves the tile as a <code>Base64</code> representation according to given parameters.
     *
     * @param zoomLevel the current zoom level
     * @param column    column index of the tile
     * @param row       row index of the tile
     *
     * @return the tile as a <code>Base64</code> representation
     */
    @NonNull
    String getTile(int zoomLevel,
                   int column,
                   int row);
}
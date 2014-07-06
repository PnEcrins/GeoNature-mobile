package com.makina.ecrins.maps.content;

import org.json.JSONObject;

import java.util.List;

/**
 * Describes a tiles layer data source.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public interface ITilesLayerDataSource {

    static final String KEY_NAME = "name";
    static final String KEY_VERSION = "version";
    static final String KEY_FORMAT = "format";
    static final String KEY_MIN_ZOOM = "description";

    /**
     * Gets all metadata for the given tiles layer source as <code>JSONObject</code>.
     * <p>
     * Five keys are <strong>required</strong> :
     * </p>
     * <ul>
     * <li>name : the plain-English name of the tileset</li>
     * <li>type : overlay or baselayer</li>
     * <li>version : the version of the tileset, as a plain number</li>
     * <li>description : a description of the layer as plain text</li>
     * <li>format : the image file format of the tile data (png as default format or jpg)
     * </ul>
     *
     * @return the metadata as <code>JSONObject</code>
     */
    public JSONObject getMetadata();

    /**
     * Gets the minimum zoom level.
     *
     * @return the minimum zoom level
     */
    public int getMinZoom();

    /**
     * Gets the maximum zoom level.
     *
     * @return the maximum zoom level
     */
    public abstract int getMaxZoom();

    /**
     * Gets all available zooms level as {@link List}.
     *
     * @return a {@link List} of available zooms
     */
    public List<Integer> getZooms();

    /**
     * Retrieves the tile as a <code>Base64</code> representation according to given parameters.
     *
     * @param zoomLevel the current zoom level
     * @param column    column index of the tile
     * @param row       row index of the tile
     * @return the tile as a <code>Base64</code> representation
     */
    public String getTile(int zoomLevel, int column, int row);
}
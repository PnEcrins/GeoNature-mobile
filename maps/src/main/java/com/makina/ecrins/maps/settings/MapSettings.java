package com.makina.ecrins.maps.settings;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.makina.ecrins.maps.jts.geojson.Feature;
import com.makina.ecrins.maps.jts.geojson.GeoPoint;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Default settings for map configuration.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class MapSettings
        implements Parcelable {

    public static final String KEY_DISPLAY_SCALE = "display_scale";
    public static final String KEY_SHOW_UNITIES_LAYER = "show_unities_layer";
    public static final String KEY_CRS = "crs";
    public static final String KEY_MAX_BOUNDS = "max_bounds";
    public static final String KEY_CENTER = "center";
    public static final String KEY_START_ZOOM = "start_zoom";
    public static final String KEY_ZOOM = "zoom";
    public static final String KEY_MIN_ZOOM = "min_zoom";
    public static final String KEY_MAX_ZOOM = "max_zoom";
    public static final String KEY_MIN_ZOOM_POINTING = "min_zoom_pointing";
    public static final String KEY_LAYERS = "layers";
    public static final String KEY_UNITY_LAYER = "unity_layer";

    private boolean mDisplayScale = true;
    private boolean mShowUnitiesLayer = true;
    private CRSSettings mCRSSettings;
    private final List<GeoPoint> mMaxBounds = new ArrayList<>();
    private Polygon mPolygonBounds = null;
    private GeoPoint mCenter;
    private int mZoom = 0;
    private int mMinZoom = 0;
    private int mMaxZoom = 0;
    private int mMinimumZoomPointing = 0;
    private final List<LayerSettings> mLayers = new ArrayList<>();
    private LayerSettings mUnityLayer = null;

    public MapSettings(Parcel source) {
        this.mDisplayScale = source.readByte() == 1;
        this.mShowUnitiesLayer = source.readByte() == 1;
        mCRSSettings = source.readParcelable(CRSSettings.class.getClassLoader());
        source.readTypedList(mMaxBounds,
                             GeoPoint.CREATOR);
        mCenter = source.readParcelable(GeoPoint.class.getClassLoader());
        mZoom = source.readInt();
        mMinZoom = source.readInt();
        mMaxZoom = source.readInt();
        mMinimumZoomPointing = source.readInt();
        source.readTypedList(mLayers,
                             LayerSettings.CREATOR);
        mUnityLayer = source.readParcelable(LayerSettings.class.getClassLoader());

        getPolygonBounds();
    }

    @Deprecated
    public MapSettings(JSONObject json) throws
                                        JSONException {
        if (json.has(KEY_DISPLAY_SCALE)) {
            this.mDisplayScale = json.getBoolean(KEY_DISPLAY_SCALE);
        }

        if (json.has(KEY_SHOW_UNITIES_LAYER)) {
            this.mShowUnitiesLayer = json.getBoolean(KEY_SHOW_UNITIES_LAYER);
        }

        if (json.has(KEY_CRS)) {
            this.mCRSSettings = new CRSSettings(json.getJSONObject(KEY_CRS));
        }

        final JSONArray maxBoundsJsonArray = json.getJSONArray(KEY_MAX_BOUNDS);

        for (int i = 0; i < maxBoundsJsonArray.length(); i++) {
            final JSONArray jsonArray = maxBoundsJsonArray.getJSONArray(i);

            if (jsonArray.length() == 2) {
                mMaxBounds.add(new GeoPoint(jsonArray.getDouble(0),
                                            jsonArray.getDouble(1)));
            }
        }

        final JSONArray centerGeoPointJsonArray = json.getJSONArray(KEY_CENTER);

        if (centerGeoPointJsonArray.length() == 2) {
            mCenter = new GeoPoint(centerGeoPointJsonArray.getDouble(0),
                                   centerGeoPointJsonArray.getDouble(1));
        }

        if (json.has(KEY_START_ZOOM)) {
            this.mZoom = json.getInt(KEY_START_ZOOM);
        }

        if (json.has(KEY_ZOOM)) {
            this.mZoom = json.getInt(KEY_ZOOM);
        }

        if (json.has(KEY_MIN_ZOOM)) {
            this.mMinZoom = json.getInt(KEY_MIN_ZOOM);
        }

        if (json.has(KEY_MAX_ZOOM)) {
            this.mMaxZoom = json.getInt(KEY_MAX_ZOOM);
        }

        if (json.has(KEY_MIN_ZOOM_POINTING)) {
            this.mMinimumZoomPointing = json.getInt(KEY_MIN_ZOOM_POINTING);
        }

        JSONArray layersJsonArray = json.getJSONArray(KEY_LAYERS);

        for (int i = 0; i < layersJsonArray.length(); i++) {
            mLayers.add(new LayerSettings(layersJsonArray.getJSONObject(i)));
        }

        if (json.has(KEY_UNITY_LAYER)) {
            mUnityLayer = new LayerSettings(json.getJSONObject(KEY_UNITY_LAYER));
        }

        getPolygonBounds();
    }

    private MapSettings(boolean displayScale,
                        boolean showUnitiesLayer,
                        @Nullable final CRSSettings crsSettings,
                        @NonNull final List<GeoPoint> maxBounds,
                        @NonNull Polygon polygonBounds,
                        @NonNull final GeoPoint center,
                        int zoom,
                        int minZoom,
                        int maxZoom,
                        int minZoomPointing,
                        @NonNull final List<LayerSettings> layers,
                        @Nullable final LayerSettings unitiesLayer) {
        this.mDisplayScale = displayScale;
        this.mShowUnitiesLayer = showUnitiesLayer;
        this.mCRSSettings = crsSettings;
        this.mMaxBounds.addAll(maxBounds);
        this.mPolygonBounds = polygonBounds;
        this.mCenter = center;
        this.mZoom = zoom;
        this.mMinZoom = minZoom;
        this.mMaxZoom = maxZoom;
        this.mMinimumZoomPointing = minZoomPointing;
        this.mLayers.addAll(layers);
        this.mUnityLayer = unitiesLayer;
    }

    public boolean isDisplayScale() {
        return mDisplayScale;
    }

    public void setDisplayScale(boolean pDisplayScale) {
        this.mDisplayScale = pDisplayScale;
    }

    public boolean isShowUnitiesLayer() {
        return mShowUnitiesLayer;
    }

    public void setShowUnitiesLayer(boolean pShowUnitiesLayer) {
        this.mShowUnitiesLayer = pShowUnitiesLayer;
    }

    @Nullable
    public CRSSettings getCRSSettings() {
        return mCRSSettings;
    }

    @NonNull
    public List<GeoPoint> getMaxBounds() {
        return mMaxBounds;
    }

    /**
     * Builds the {@link Polygon} containing the current map boundaries.
     * This polygon is used to check if a given location is inside the map or not
     *
     * @return the current map boundaries as {@link Polygon}
     */
    @Nullable
    public Polygon getPolygonBounds() {
        if (mPolygonBounds == null) {
            if (mMaxBounds.size() != 2) {
                return null;
            }

            GeoPoint southWest = getMaxBounds().get(0);
            GeoPoint northEast = getMaxBounds().get(1);

            final Geometry geometry = new GeometryFactory().toGeometry(new Envelope(southWest.getPoint()
                                                                                             .getCoordinate(),
                                                                                    northEast.getPoint()
                                                                                             .getCoordinate()));
            if (geometry instanceof Polygon) {
                mPolygonBounds = (Polygon) geometry;
            }
        }

        return mPolygonBounds;
    }

    @NonNull
    public GeoPoint getCenter() {
        return mCenter;
    }

    public void setCenter(@NonNull final GeoPoint pCenter) {
        this.mCenter = pCenter;
    }

    /**
     * Gets the current zoom level.
     *
     * @return the current zoom level
     */
    public int getZoom() {
        return mZoom;
    }

    public void setZoom(int pZoom) {
        this.mZoom = pZoom;
    }

    /**
     * Gets the minimum zoom level.
     *
     * @return the minimum zoom level
     */
    public int getMinZoom() {
        return mMinZoom;
    }

    public void setMinZoom(int pMinZoom) {
        this.mMinZoom = pMinZoom;
    }

    /**
     * Gets the maximum zoom level.
     *
     * @return the maximum zoom level
     */
    public int getMaxZoom() {
        return mMaxZoom;
    }

    public void setMaxZoom(int pMaxZoom) {
        this.mMaxZoom = pMaxZoom;
    }

    public int getMinimumZoomPointing() {
        return mMinimumZoomPointing;
    }

    @NonNull
    public List<LayerSettings> getLayers() {
        return mLayers;
    }

    /**
     * Gets the the features layer if any.
     *
     * @return the features layer as {@link LayerSettings} or <code>null</code>.
     */
    @Nullable
    public LayerSettings getUnityLayer() {
        return mUnityLayer;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest,
                              int flags) {
        dest.writeByte((byte) (mDisplayScale ? 1 : 0)); // as boolean value
        dest.writeByte((byte) (mShowUnitiesLayer ? 1 : 0)); // as boolean value
        dest.writeParcelable(mCRSSettings,
                             0);
        dest.writeTypedList(mMaxBounds);
        dest.writeParcelable(mCenter,
                             0);
        dest.writeInt(mZoom);
        dest.writeInt(mMinZoom);
        dest.writeInt(mMaxZoom);
        dest.writeInt(mMinimumZoomPointing);
        dest.writeTypedList(mLayers);
        dest.writeParcelable(mUnityLayer,
                             0);
    }

    /**
     * Builder implementation used to create new {@link MapSettings}.
     *
     * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
     */
    public static final class Builder {

        private boolean displayScale;
        private boolean showUnitiesLayer;
        private CRSSettings crsSettings;
        private final List<GeoPoint> maxBounds = new ArrayList<>();
        private Polygon polygonBounds;
        private GeoPoint center;
        private int zoom;
        private int minZoom;
        private int maxZoom;
        private int minZoomPointing;
        private final List<LayerSettings> layers = new ArrayList<>();
        private LayerSettings unitiesLayer;

        private Builder() {
            displayScale = true;
            showUnitiesLayer = false;
        }

        @NonNull
        public static Builder newInstance() {
            return new Builder();
        }

        /**
         * Whether to show the scale on the map.
         * Set it to {@code false} to hide it.
         *
         * @param showScale show scale or not (default: true)
         *
         * @return Fluent interface
         */
        @NonNull
        public Builder showScale(boolean showScale) {
            this.displayScale = showScale;

            return this;
        }

        /**
         * Whether to show the unities layer on the map.
         * Set it to {@code false} to hide it.
         *
         * @param showUnitiesLayer show the unities layer not (default: false)
         *
         * @return Fluent interface
         */
        @NonNull
        public Builder showUnitiesLayer(boolean showUnitiesLayer) {
            this.showUnitiesLayer = showUnitiesLayer;

            return this;
        }

        /**
         * Set the Coordinate Reference System (CRS) settings to use.
         *
         * @param crsSettings the Coordinate Reference System (CRS) settings to use
         *
         * @return Fluent interface
         */
        @NonNull
        public Builder setCRSSettings(@Nullable final CRSSettings crsSettings) {
            this.crsSettings = crsSettings;

            return this;
        }

        /**
         * Set the max bounds of the map.
         *
         * @param maxBounds the max bounds as {@code List} of {@link GeoPoint}
         *
         * @return Fluent interface
         */
        @NonNull
        public Builder setMaxBounds(@NonNull final List<GeoPoint> maxBounds) {
            if (maxBounds.size() == 2) {
                this.maxBounds.clear();
                this.maxBounds.addAll(maxBounds);

                GeoPoint southWest = this.maxBounds.get(0);
                GeoPoint northEast = this.maxBounds.get(1);

                final Geometry geometry = new GeometryFactory().toGeometry(new Envelope(southWest.getPoint()
                                                                                                 .getCoordinate(),
                                                                                        northEast.getPoint()
                                                                                                 .getCoordinate()));
                if (geometry instanceof Polygon) {
                    this.polygonBounds = (Polygon) geometry;

                    if (this.center == null) {
                        this.center = new GeoPoint(this.polygonBounds.getCentroid());
                    }
                }
            }

            return this;
        }

        /**
         * Set the max bounds of the map.
         *
         * @param maxBounds the max bounds
         *
         * @return Fluent interface
         */
        @NonNull
        public Builder setMaxBounds(@NonNull final Polygon maxBounds) {
            if (maxBounds.getEnvelope()
                         .getCoordinates().length == 5) {
                this.maxBounds.clear();
                this.maxBounds.addAll(Arrays.asList(new GeoPoint(new GeometryFactory().createPoint(maxBounds.getEnvelope()
                                                                                                            .getCoordinates()[0])),
                                                    new GeoPoint(new GeometryFactory().createPoint(maxBounds.getEnvelope()
                                                                                                            .getCoordinates()[2]))));
                this.polygonBounds = maxBounds;

                if (this.center == null) {
                    this.center = new GeoPoint(this.polygonBounds.getCentroid());
                }
            }

            return this;
        }

        /**
         * Set the {@link GeoPoint} as center of the map.
         *
         * @param center the {@link GeoPoint} as center
         *
         * @return Fluent interface
         */
        @NonNull
        public Builder setCenter(@NonNull final GeoPoint center) {
            this.center = center;

            return this;
        }

        /**
         * Set the current zoom level of the map.
         *
         * @param zoom the current zoom level
         *
         * @return Fluent interface
         */
        @NonNull
        public Builder setZoom(int zoom) {
            this.zoom = zoom;

            return this;
        }

        /**
         * Set the minimum zoom level of the map.
         *
         * @param minZoom the minimum zoom level
         *
         * @return Fluent interface
         */
        @NonNull
        public Builder setMinZoom(int minZoom) {
            this.minZoom = minZoom;

            return this;
        }

        /**
         * Set the maximum zoom level of the map.
         *
         * @param maxZoom the maximum zoom level
         *
         * @return Fluent interface
         */
        @NonNull
        public Builder setMaxZoom(int maxZoom) {
            this.maxZoom = maxZoom;

            return this;
        }

        /**
         * Set the minimum zoom level to add/edit/remove {@link Feature} on the map.
         *
         * @param minZoomPointing the minimum zoom level for editing a {@link Feature} on the map
         *
         * @return Fluent interface
         */
        @NonNull
        public Builder setMinZoomPointing(int minZoomPointing) {
            this.minZoomPointing = minZoomPointing;

            return this;
        }

        /**
         * Add a {@link LayerSettings} on the map.
         *
         * @param layerSettings the {@link LayerSettings} to add
         *
         * @return Fluent interface
         */
        @NonNull
        public Builder addLayerSettings(@NonNull final LayerSettings layerSettings) {
            this.layers.add(layerSettings);

            return this;
        }

        /**
         * Remove all {@link LayerSettings}.
         *
         * @return Fluent interface
         */
        @NonNull
        public Builder removeAllLayerSettings() {
            this.layers.clear();

            return this;
        }

        /**
         * Set the unities layer to display.
         *
         * @param unitiesLayer the {@link LayerSettings} as unities layer
         *
         * @return Fluent interface
         */
        @NonNull
        public Builder setUnitiesLayer(@Nullable final LayerSettings unitiesLayer) {
            this.unitiesLayer = unitiesLayer;
            this.showUnitiesLayer = this.unitiesLayer != null;

            return this;
        }

        /**
         * Builds a new instance of {@link MapSettings}.
         *
         * @return new instance of {@link MapSettings}
         */
        @NonNull
        public MapSettings build() {
            return new MapSettings(displayScale,
                                   showUnitiesLayer,
                                   crsSettings,
                                   maxBounds,
                                   polygonBounds,
                                   center,
                                   zoom,
                                   minZoom,
                                   maxZoom,
                                   minZoomPointing,
                                   layers,
                                   unitiesLayer);
        }
    }

    public static final Parcelable.Creator<MapSettings> CREATOR = new Parcelable.Creator<MapSettings>() {
        @Override
        public MapSettings createFromParcel(Parcel source) {
            return new MapSettings(source);
        }

        @Override
        public MapSettings[] newArray(int size) {
            return new MapSettings[size];
        }
    };
}

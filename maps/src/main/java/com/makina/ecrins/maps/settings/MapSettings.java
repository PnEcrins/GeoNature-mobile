package com.makina.ecrins.maps.settings;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.makina.ecrins.maps.RenderQualityEnum;
import com.makina.ecrins.maps.jts.geojson.GeoPoint;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Default settings for map configuration.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class MapSettings
        implements Parcelable {

    public static final String KEY_DISPLAY_SCALE = "display_scale";
    public static final String KEY_RENDER_QUALITY = "render_quality";
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
    private RenderQualityEnum mRenderQuality = RenderQualityEnum.AUTO;
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
        this.mRenderQuality = RenderQualityEnum.asRenderQuality(source.readString());
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

    public MapSettings(JSONObject json) throws
                                        JSONException {
        if (json.has(KEY_DISPLAY_SCALE)) {
            this.mDisplayScale = json.getBoolean(KEY_DISPLAY_SCALE);
        }

        if (json.has(KEY_RENDER_QUALITY)) {
            this.mRenderQuality = RenderQualityEnum.asRenderQuality(json.getString(KEY_RENDER_QUALITY));
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

    public boolean isDisplayScale() {
        return mDisplayScale;
    }

    public void setDisplayScale(boolean pDisplayScale) {
        this.mDisplayScale = pDisplayScale;
    }

    public RenderQualityEnum getRenderQuality() {
        return mRenderQuality;
    }

    public void setRenderQuality(RenderQualityEnum pRenderQuality) {
        this.mRenderQuality = pRenderQuality;
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

    public List<GeoPoint> getMaxBounds() {
        return mMaxBounds;
    }

    /**
     * Builds the {@link Polygon} containing the current map boundaries.
     * This polygon is used to check if a given location is inside the map or not
     *
     * @return the current map boundaries as {@link Polygon}
     */
    @NonNull
    public Polygon getPolygonBounds() {
        if (mPolygonBounds == null) {
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

    public GeoPoint getCenter() {
        return mCenter;
    }

    public void setCenter(GeoPoint pCenter) {
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

    public List<LayerSettings> getLayers() {
        return mLayers;
    }

    /**
     * Gets the the features layer if any.
     *
     * @return the features layer as {@link LayerSettings} or <code>null</code>.
     */
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
        dest.writeString(mRenderQuality.getValueAsString());
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

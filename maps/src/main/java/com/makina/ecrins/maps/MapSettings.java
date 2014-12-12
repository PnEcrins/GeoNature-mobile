package com.makina.ecrins.maps;

import android.os.Parcel;
import android.os.Parcelable;

import com.makina.ecrins.maps.geojson.geometry.GeoPoint;
import com.makina.ecrins.maps.geojson.geometry.Point;
import com.makina.ecrins.maps.geojson.geometry.Polygon;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Default settings for map configuration.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class MapSettings implements Parcelable {

    public static final String KEY_DISPLAY_SCALE = "display_scale";
    public static final String KEY_RENDER_QUALITY = "render_quality";
    public static final String KEY_SHOW_UNITIES_LAYER = "show_unities_layer";
    public static final String KEY_BBOX = "bbox";
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
    private final List<Integer> mBbox = new ArrayList<>();
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
        source.readList(mBbox, Integer.class.getClassLoader());
        source.readTypedList(mMaxBounds, GeoPoint.CREATOR);
        mCenter = source.readParcelable(GeoPoint.class.getClassLoader());
        mZoom = source.readInt();
        mMinZoom = source.readInt();
        mMaxZoom = source.readInt();
        mMinimumZoomPointing = source.readInt();
        source.readTypedList(mLayers, LayerSettings.CREATOR);
        mUnityLayer = source.readParcelable(LayerSettings.class.getClassLoader());

        getPolygonBounds();
    }

    public MapSettings(JSONObject json) throws JSONException {
        if (json.has(KEY_DISPLAY_SCALE)) {
            this.mDisplayScale = json.getBoolean(KEY_DISPLAY_SCALE);
        }

        if (json.has(KEY_RENDER_QUALITY)) {
            this.mRenderQuality = RenderQualityEnum.asRenderQuality(json.getString(KEY_RENDER_QUALITY));
        }

        if (json.has(KEY_SHOW_UNITIES_LAYER)) {
            this.mShowUnitiesLayer = json.getBoolean(KEY_SHOW_UNITIES_LAYER);
        }

        JSONArray bboxJsonArray = json.getJSONArray(KEY_BBOX);

        for (int i = 0; i < bboxJsonArray.length(); i++) {
            mBbox.add(bboxJsonArray.getInt(i));
        }

        JSONArray maxBoundsJsonArray = json.getJSONArray(KEY_MAX_BOUNDS);

        for (int i = 0; i < maxBoundsJsonArray.length(); i++) {
            mMaxBounds.add(
                    new GeoPoint(
                            maxBoundsJsonArray.getJSONArray(i),
                            GeoPoint.LAT_LON
                    )
            );
        }

        mCenter = new GeoPoint(
                json.getJSONArray(KEY_CENTER),
                GeoPoint.LAT_LON
        );

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

    public List<Integer> getBbox() {
        return mBbox;
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
    public Polygon getPolygonBounds() {
        if (mPolygonBounds == null) {
            GeoPoint southWest = getMaxBounds().get(0);
            GeoPoint northEast = getMaxBounds().get(1);

            mPolygonBounds = new Polygon(
                    Arrays.asList(
                            new Point(southWest),
                            new Point(
                                    new GeoPoint(
                                            northEast.getLatitudeE6(),
                                            southWest.getLongitudeE6()
                                    )
                            ),
                            new Point(northEast),
                            new Point(
                                    new GeoPoint(
                                            southWest.getLatitudeE6(),
                                            northEast.getLongitudeE6()
                                    )
                            )
                    )
            );
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

    public int getLayerSettingsIndex(LayerSettings pLayerSettings) {
        return mLayers.indexOf(pLayerSettings);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (mDisplayScale ? 1 : 0)); // as boolean value
        dest.writeString(mRenderQuality.getValueAsString());
        dest.writeByte((byte) (mShowUnitiesLayer ? 1 : 0)); // as boolean value
        dest.writeList(mBbox);
        dest.writeTypedList(mMaxBounds);
        dest.writeParcelable(mCenter, 0);
        dest.writeInt(mZoom);
        dest.writeInt(mMinZoom);
        dest.writeInt(mMaxZoom);
        dest.writeInt(mMinimumZoomPointing);
        dest.writeTypedList(mLayers);
        dest.writeParcelable(mUnityLayer, 0);
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

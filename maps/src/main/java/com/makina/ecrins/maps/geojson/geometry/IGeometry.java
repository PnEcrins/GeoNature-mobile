package com.makina.ecrins.maps.geojson.geometry;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.Parcelable;

import com.makina.ecrins.maps.geojson.GeoJSONType;

/**
 * The base class for all geometric objects.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
@SuppressLint("ParcelCreator")
public interface IGeometry extends Parcelable {

    public GeoJSONType getType();

    public List<IGeometry> getCoordinates();

    public JSONObject getJSONObject() throws JSONException;
}
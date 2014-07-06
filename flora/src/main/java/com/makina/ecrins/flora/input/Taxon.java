package com.makina.ecrins.flora.input;

import android.os.BadParcelableException;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.makina.ecrins.commons.input.AbstractTaxon;
import com.makina.ecrins.maps.geojson.Feature;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Describes a taxon from {@link Input}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class Taxon extends AbstractTaxon {

    public static final String KEY_AREAS = "areas";
    public static final String KEY_PROSPECTING_AREA = "prospecting_area";
    public static final String KEY_PROSPECTING_AREA_FEATURE = "feature";

    private Map<String, Area> mAreas;
    private String mCurrentSelectedAreaId;
    private Feature mProspectingArea;

    public Taxon(long pTaxonId) {
        super(pTaxonId);

        mAreas = new TreeMap<String, Area>();
        mCurrentSelectedAreaId = null;
        mProspectingArea = null;
    }

    public Taxon(Parcel source) {
        super(source);

        mCurrentSelectedAreaId = null;

        List<Area> areas = new ArrayList<Area>();
        source.readTypedList(areas, Area.CREATOR);
        mAreas = new TreeMap<String, Area>();

        for (Area area : areas) {
            mAreas.put(area.getFeature()
                    .getId(), area);
        }

        try {
            mProspectingArea = source.readParcelable(Feature.class.getClassLoader());
        }
        catch (BadParcelableException bpe) {
            mProspectingArea = null;

            Log.w(Taxon.class.getName(), bpe.getMessage());
        }
    }

    public Map<String, Area> getAreas() {
        return mAreas;
    }

    /**
     * Gets the currently selected {@link Area} for this {@link Taxon}.
     *
     * @return the selected {@link Area}
     * @see Feature#getId()
     */
    public String getCurrentSelectedAreaId() {
        return mCurrentSelectedAreaId;
    }

    /**
     * Sets the currently selected {@link Area} for this {@link Taxon}.
     *
     * @param pCurrentSelectedAreaId the selected {@link AbstractTaxon}
     * @see Feature#getId()
     */
    public void setCurrentSelectedAreaId(String pCurrentSelectedAreaId) {
        this.mCurrentSelectedAreaId = pCurrentSelectedAreaId;
    }

    /**
     * Gets the last inserted {@link Area} for this {@link Taxon} or <code>null</code> if none was added.
     *
     * @return the last inserted {@link Area}
     * @see Feature#getId()
     */
    public String getLastInsertedAreaId() {
        if (this.mAreas.isEmpty()) {
            return null;
        }
        else {
            return ((TreeMap<String, Area>) this.mAreas).lastKey();
        }
    }

    /**
     * Gets the currently selected {@link Area} for this {@link Taxon}.
     *
     * @return the selected {@link Area} or <code>null</code> if none was selected.
     */
    public Area getCurrentSelectedArea() {
        return (getCurrentSelectedAreaId() == null) ? null : getAreas().get(getCurrentSelectedAreaId());
    }

    public Feature getProspectingArea() {
        return mProspectingArea;
    }

    public void setProspectingArea(Feature pProspectingArea) {
        this.mProspectingArea = pProspectingArea;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject json = super.getJSONObject();

        JSONArray jsonAreas = new JSONArray();

        for (Area area : mAreas.values()) {
            jsonAreas.put(area.getJSONObject());
        }

        json.put(KEY_AREAS, jsonAreas);

        JSONObject jsonProspectingArea = new JSONObject();
        jsonProspectingArea.put(KEY_PROSPECTING_AREA_FEATURE, mProspectingArea.getJSONObject());

        json.put(KEY_PROSPECTING_AREA, jsonProspectingArea);

        return json;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeTypedList(new ArrayList<Area>(mAreas.values()));
    }

    public static final Parcelable.Creator<Taxon> CREATOR = new Parcelable.Creator<Taxon>() {
        @Override
        public Taxon createFromParcel(Parcel source) {
            return new Taxon(source);
        }

        @Override
        public Taxon[] newArray(int size) {
            return new Taxon[size];
        }
    };
}

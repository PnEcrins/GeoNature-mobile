package com.makina.ecrins.commons.input;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Describes a current input.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
@SuppressLint("ParcelCreator")
public abstract class AbstractInput
        implements Parcelable {

    public static final String KEY_ID = "id";
    public static final String KEY_INITIAL_INPUT = "initial_input";
    public static final String KEY_TAXA = "taxons";

    private long mInputId;
    private String mFeatureId;
    private final Map<Long, AbstractTaxon> mTaxa;
    private long mCurrentSelectedTaxonId;

    public AbstractInput() {

        mInputId = generateId();
        mFeatureId = null;
        mTaxa = new TreeMap<>();
        mCurrentSelectedTaxonId = -1;
    }

    public AbstractInput(Parcel source) {

        mInputId = source.readLong();
        mFeatureId = source.readString();

        final List<AbstractTaxon> taxa = getTaxaFromParcel(source);
        mTaxa = new TreeMap<>();

        for (AbstractTaxon taxon : taxa) {
            mTaxa.put(taxon.getId(),
                      taxon);
        }

        mCurrentSelectedTaxonId = -1;
    }

    public long getInputId() {

        return mInputId;
    }

    public String getFeatureId() {

        return mFeatureId;
    }

    public void setFeatureId(String pFeatureId) {

        this.mFeatureId = pFeatureId;
    }

    /**
     * Gets a {@code Map} of all registered {@link AbstractTaxon} for this
     * {@link com.makina.ecrins.commons.input.AbstractInput}.
     *
     * @return a {@code Map} of all registered {@link AbstractTaxon}
     *
     * @see AbstractTaxon#getId()
     */
    public Map<Long, AbstractTaxon> getTaxa() {

        return mTaxa;
    }

    /**
     * Gets the currently selected {@link AbstractTaxon} for this input.
     *
     * @return the selected {@link AbstractTaxon}
     *
     * @see AbstractTaxon#getId()
     */
    public long getCurrentSelectedTaxonId() {

        return mCurrentSelectedTaxonId;
    }

    /**
     * Sets the currently selected {@link AbstractTaxon} for this input.
     *
     * @param pCurrentSelectedTaxonId the selected {@link AbstractTaxon}
     *
     * @see AbstractTaxon#getId()
     */
    public void setCurrentSelectedTaxonId(long pCurrentSelectedTaxonId) {

        this.mCurrentSelectedTaxonId = pCurrentSelectedTaxonId;
    }

    /**
     * Gets the last inserted {@link AbstractTaxon} for this input or {@code -1} if none was added.
     *
     * @return the last inserted {@link AbstractTaxon}
     *
     * @see AbstractTaxon#getId()
     */
    public long getLastInsertedTaxonId() {

        if (this.mTaxa.isEmpty()) {
            return -1;
        }
        else {
            return ((TreeMap<Long, AbstractTaxon>) this.mTaxa).lastKey();
        }
    }

    /**
     * Gets the currently selected {@link AbstractTaxon} for this input.
     *
     * @return the selected {@link AbstractTaxon} or {@code null} if none was selected.
     */
    public AbstractTaxon getCurrentSelectedTaxon() {

        return getTaxa().get(getCurrentSelectedTaxonId());
    }

    public JSONObject getJSONObject() throws JSONException {

        JSONObject json = new JSONObject();

        json.put(KEY_ID,
                 mInputId);
        json.put(KEY_INITIAL_INPUT,
                 "nomade");

        JSONArray jsonTaxa = new JSONArray();

        for (AbstractTaxon taxon : mTaxa.values()) {
            jsonTaxa.put(taxon.getJSONObject());
        }

        json.put(KEY_TAXA,
                 jsonTaxa);

        return json;
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(
            Parcel dest,
            int flags) {

        dest.writeLong(mInputId);
        dest.writeString(mFeatureId);
        dest.writeTypedList(new ArrayList<>(mTaxa.values()));
    }

    public abstract List<AbstractTaxon> getTaxaFromParcel(Parcel source);

    /**
     * Generates a pseudo unique ID. The value is the number of seconds since Jan. 1, 2000, midnight.
     *
     * @return an unique ID
     */
    public static long generateId() {

        final Calendar now = Calendar.getInstance();
        now.set(Calendar.MILLISECOND,
                     0);

        final Calendar start = Calendar.getInstance();
        start.set(2000,
                     Calendar.JANUARY,
                     1,
                     0,
                     0,
                     0);
        start.set(Calendar.MILLISECOND,
                     0);

        return (now.getTimeInMillis() - start.getTimeInMillis()) / 1000;
    }
}

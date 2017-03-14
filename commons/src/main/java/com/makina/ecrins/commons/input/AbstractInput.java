package com.makina.ecrins.commons.input;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

    private static final String KEY_TYPE = "input_type";
    private static final String KEY_ID = "id";
    private static final String KEY_INITIAL_INPUT = "initial_input";
    private static final String KEY_PROTOCOL = "protocol";
    private static final String KEY_DATE_OBS = "dateobs";
    private static final String KEY_OBSERVERS_ID = "observers_id";
    private static final String KEY_TAXA = "taxons";

    InputType mType;
    long mInputId;
    private String mFeatureId;
    private Date mDate;
    private Protocol mProtocol;
    private final Map<Long, Observer> mObservers;
    private final Map<Long, AbstractTaxon> mTaxa;
    private long mCurrentSelectedTaxonId;

    public AbstractInput(@NonNull final InputType type) {
        mType = type;
        mInputId = generateId();
        mFeatureId = null;
        mDate = new Date();
        mProtocol = null;
        mObservers = new TreeMap<>();
        mTaxa = new TreeMap<>();
        mCurrentSelectedTaxonId = -1;
    }

    public AbstractInput(Parcel source) {
        mType = (InputType) source.readSerializable();
        mInputId = source.readLong();
        mFeatureId = source.readString();
        mDate = (Date) source.readSerializable();

        if (mDate == null) {
            mDate = new Date();
        }

        mProtocol = source.readParcelable(Protocol.class.getClassLoader());

        final List<Observer> observers = new ArrayList<>();
        source.readTypedList(observers,
                             Observer.CREATOR);
        mObservers = new TreeMap<>();

        for (Observer observer : observers) {
            mObservers.put(observer.getObserverId(),
                           observer);
        }

        final List<AbstractTaxon> taxa = getTaxaFromParcel(source);
        mTaxa = new TreeMap<>();

        for (AbstractTaxon taxon : taxa) {
            mTaxa.put(taxon.getId(),
                      taxon);
        }

        mCurrentSelectedTaxonId = -1;
    }

    @NonNull
    public InputType getType() {
        return mType;
    }

    public long getInputId() {
        return mInputId;
    }

    @Nullable
    public String getFeatureId() {
        return mFeatureId;
    }

    public void setFeatureId(String pFeatureId) {
        this.mFeatureId = pFeatureId;
    }

    @NonNull
    public Date getDate() {
        return mDate;
    }

    public void setDate(@NonNull final Date pDate) {
        this.mDate = pDate;
    }

    @Nullable
    public Protocol getProtocol() {
        return mProtocol;
    }

    public void setProtocol(@Nullable final Protocol protocol) {
        this.mProtocol = protocol;
    }

    /**
     * Gets a {@code Map} of all {@link Observer}s declared for this {@link AbstractInput}.
     *
     * @return a {@code Map} of all declared {@link Observer}
     */
    @NonNull
    public Map<Long, Observer> getObservers() {
        return mObservers;
    }

    /**
     * Gets a {@code Map} of all registered {@link AbstractTaxon} for this {@link AbstractInput}.
     *
     * @return a {@code Map} of all registered {@link AbstractTaxon}
     */
    @NonNull
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
    @Nullable
    public AbstractTaxon getCurrentSelectedTaxon() {
        return getTaxa().get(getCurrentSelectedTaxonId());
    }

    /**
     * @deprecated use {@link InputJsonReader} instead
     */
    @Deprecated
    public JSONObject getJSONObject() throws
                                      JSONException {
        final JSONObject json = new JSONObject();

        json.put(KEY_TYPE,
                 getType().getValue());
        json.put(KEY_ID,
                 mInputId);
        json.put(KEY_INITIAL_INPUT,
                 "nomade");

        json.put(KEY_DATE_OBS,
                 DateFormat.format(getDateFormat(),
                                   mDate));

        if (mProtocol != null) {
            json.put(KEY_PROTOCOL,
                     mProtocol.getJSONObject());
        }

        final JSONArray jsonObservers = new JSONArray();

        for (Observer observer : mObservers.values()) {
            jsonObservers.put(observer.getObserverId());
        }

        json.put(KEY_OBSERVERS_ID,
                 jsonObservers);

        final JSONArray jsonTaxa = new JSONArray();

        for (AbstractTaxon taxon : mTaxa.values()) {
            jsonTaxa.put(taxon.getJSONObject());
        }

        json.put(KEY_TAXA,
                 jsonTaxa);

        return json;
    }

    @Deprecated
    @NonNull
    public String getDateFormat() {
        return "yyyy/MM/dd";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest,
                              int flags) {
        dest.writeSerializable(mType);
        dest.writeLong(mInputId);
        dest.writeString(mFeatureId);
        dest.writeSerializable(mDate);
        dest.writeParcelable(mProtocol,
                             0);
        dest.writeTypedList(new ArrayList<>(mObservers.values()));
        dest.writeTypedList(new ArrayList<>(mTaxa.values()));
    }

    public abstract List<AbstractTaxon> getTaxaFromParcel(Parcel source);

    /**
     * Generates a pseudo unique ID. The value is the number of seconds since Jan. 1, 2000, midnight.
     *
     * @return an unique ID
     *
     * @deprecated see {@link InputHelper#generateId()}
     */
    @Deprecated
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

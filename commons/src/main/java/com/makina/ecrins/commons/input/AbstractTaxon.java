package com.makina.ecrins.commons.input;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Describes a taxon from {@link AbstractInput}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
@SuppressLint("ParcelCreator")
public abstract class AbstractTaxon implements Parcelable {

    public static final String KEY_ID = "id";
    public static final String KEY_TAXON_ID = "id_taxon";
    public static final String KEY_NAME_ENTERED = "name_entered";
    public static final String KEY_OBSERVATION = "observation";
    public static final String KEY_OBSERVATION_CRITERION = "criterion";
    public static final String KEY_COMMENT = "comment";

    private long mId;
    private long mTaxonId;
    private long mClassId;
    private int mClassCount;
    private String mNameEntered;
    private long mCriterionId;
    private String mCriterionLabel;
    private String mComment;

    public AbstractTaxon(long pTaxonId) {
        this.mId = AbstractInput.generateId();
        this.mTaxonId = pTaxonId;
        mCriterionId = -1;
        setNameEntered("");
        setComment("");
    }

    public AbstractTaxon(Parcel source) {
        this.mId = source.readLong();
        this.mTaxonId = source.readLong();
        this.mClassId = source.readLong();
        this.mNameEntered = source.readString();
        setCriterionId(source.readLong());
        setComment(source.readString());
    }

    public long getId() {
        return mId;
    }

    public long getTaxonId() {
        return mTaxonId;
    }

    public long getClassId() {
        return mClassId;
    }

    public void setClassId(long pClassId) {
        this.mClassId = pClassId;
    }

    public int getClassCount() {
        return mClassCount;
    }

    public void setClassCount(int pClassCount) {
        this.mClassCount = pClassCount;
    }

    public String getNameEntered() {
        return mNameEntered;
    }

    public void setNameEntered(String pNameEntered) {
        this.mNameEntered = pNameEntered;
    }

    public long getCriterionId() {
        return mCriterionId;
    }

    public void setCriterionId(long pCriterionId) {
        this.mCriterionId = pCriterionId;
    }

    public String getCriterionLabel() {
        return mCriterionLabel;
    }

    public void setCriterionLabel(String pCriterionLabel) {
        this.mCriterionLabel = pCriterionLabel;
    }

    public String getComment() {
        return mComment;
    }

    public void setComment(String pComment) {
        this.mComment = pComment;
    }

    public JSONObject getJSONObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(KEY_ID, mId);
        json.put(KEY_TAXON_ID, mTaxonId);
        json.put(KEY_NAME_ENTERED, mNameEntered);

        JSONObject jsonObservation = new JSONObject();
        jsonObservation.put(KEY_OBSERVATION_CRITERION, mCriterionId);

        json.put(KEY_OBSERVATION, jsonObservation);
        json.put(KEY_COMMENT, mComment);

        return json;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeLong(mTaxonId);
        dest.writeLong(mClassId);
        dest.writeString(mNameEntered);
        dest.writeLong(mCriterionId);
        dest.writeString(mComment);
    }
}

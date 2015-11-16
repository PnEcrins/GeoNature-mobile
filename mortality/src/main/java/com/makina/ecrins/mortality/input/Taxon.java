package com.makina.ecrins.mortality.input;

import android.os.Parcel;
import android.os.Parcelable;

import com.makina.ecrins.commons.input.AbstractTaxon;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Describes a taxon from {@link Input}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class Taxon
        extends AbstractTaxon {

    public static final String KEY_MORTALITY = "mortality";
    public static final String KEY_MORTALITY_ADULT_MALE = "adult_male";
    public static final String KEY_MORTALITY_ADULT_FEMALE = "adult_female";
    public static final String KEY_MORTALITY_ADULT_UNDETERMINED = "adult";
    public static final String KEY_MORTALITY_NOT_ADULT = "not_adult";
    public static final String KEY_MORTALITY_YOUNG = "young";
    public static final String KEY_MORTALITY_YEARLING = "yearling";
    public static final String KEY_MORTALITY_UNDETERMINED = "sex_age_unspecified";
    public static final String KEY_MORTALITY_SAMPLE = "sample";

    private int mMortalityAdultMale;
    private int mMortalityAdultFemale;
    private int mMortalityAdultUndetermined;
    private int mMortalityNotAdult;
    private int mMortalityYoung;
    private int mMortalityYearling;
    private int mMortalityUndetermined;
    private boolean mMortalitySample;

    public Taxon(long pTaxonId) {

        super(pTaxonId);

        // as default criterion for mortality input
        setCriterionId(2);
    }

    public Taxon(Parcel source) {

        super(source);

        setMortalityAdultMale(source.readInt());
        setMortalityAdultFemale(source.readInt());
        setMortalityAdultUndetermined(source.readInt());
        setMortalityNotAdult(source.readInt());
        setMortalityYoung(source.readInt());
        setMortalityYearling(source.readInt());
        setMortalityUndetermined(source.readInt());
        setMortalitySample(source.readByte() == 1);
    }

    public int getMortalityAdultMale() {

        return mMortalityAdultMale;
    }

    public void setMortalityAdultMale(int pMortalityAdultMale) {

        this.mMortalityAdultMale = pMortalityAdultMale;
    }

    public int getMortalityAdultFemale() {

        return mMortalityAdultFemale;
    }

    public void setMortalityAdultFemale(int pMortalityAdultFemale) {

        this.mMortalityAdultFemale = pMortalityAdultFemale;
    }

    public int getMortalityAdultUndetermined() {

        return mMortalityAdultUndetermined;
    }

    public void setMortalityAdultUndetermined(int pMortalityAdultUndetermined) {

        this.mMortalityAdultUndetermined = pMortalityAdultUndetermined;
    }

    public int getMortalityNotAdult() {

        return mMortalityNotAdult;
    }

    public void setMortalityNotAdult(int pMortalityNotAdult) {

        this.mMortalityNotAdult = pMortalityNotAdult;
    }

    public int getMortalityYoung() {

        return mMortalityYoung;
    }

    public void setMortalityYoung(int pMortalityYoung) {

        this.mMortalityYoung = pMortalityYoung;
    }

    public int getMortalityYearling() {

        return mMortalityYearling;
    }

    public void setMortalityYearling(int pMortalityYearling) {

        this.mMortalityYearling = pMortalityYearling;
    }

    public int getMortalityUndetermined() {

        return mMortalityUndetermined;
    }

    public void setMortalityUndetermined(int pMortalityUndetermined) {

        this.mMortalityUndetermined = pMortalityUndetermined;
    }

    public boolean isMortalitySample() {

        return mMortalitySample;
    }

    public void setMortalitySample(boolean pMortalitySample) {

        this.mMortalitySample = pMortalitySample;
    }

    public int counting() {

        return getMortalityAdultMale() + getMortalityAdultFemale() + getMortalityAdultUndetermined() + getMortalityNotAdult() + getMortalityYoung() + getMortalityYearling() + getMortalityUndetermined();
    }

    public JSONObject getJSONObject() throws JSONException {

        JSONObject json = super.getJSONObject();

        JSONObject jsonMortality = new JSONObject();
        jsonMortality.put(KEY_MORTALITY_ADULT_MALE,
                          mMortalityAdultMale);
        jsonMortality.put(KEY_MORTALITY_ADULT_FEMALE,
                          mMortalityAdultFemale);
        jsonMortality.put(KEY_MORTALITY_ADULT_UNDETERMINED,
                          mMortalityAdultUndetermined);
        jsonMortality.put(KEY_MORTALITY_NOT_ADULT,
                          mMortalityNotAdult);
        jsonMortality.put(KEY_MORTALITY_YOUNG,
                          mMortalityYoung);
        jsonMortality.put(KEY_MORTALITY_YEARLING,
                          mMortalityYearling);
        jsonMortality.put(KEY_MORTALITY_UNDETERMINED,
                          mMortalityUndetermined);
        jsonMortality.put(KEY_MORTALITY_SAMPLE,
                          mMortalitySample);

        json.put(KEY_MORTALITY,
                 jsonMortality);

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

        super.writeToParcel(dest,
                            flags);

        dest.writeInt(mMortalityAdultMale);
        dest.writeInt(mMortalityAdultFemale);
        dest.writeInt(mMortalityAdultUndetermined);
        dest.writeInt(mMortalityNotAdult);
        dest.writeInt(mMortalityYoung);
        dest.writeInt(mMortalityYearling);
        dest.writeInt(mMortalityUndetermined);
        dest.writeByte((byte) (mMortalitySample ? 1 : 0)); // as boolean value
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

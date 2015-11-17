package com.makina.ecrins.invertebrate.inputs;

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

    public static final String KEY_COUNTING = "counting";
    public static final String KEY_COUNTING_ADULT_MALE = "adult_male";
    public static final String KEY_COUNTING_ADULT_FEMALE = "adult_female";
    public static final String KEY_COUNTING_ADULT_UNDETERMINED = "adult";
    public static final String KEY_COUNTING_NOT_ADULT = "not_adult";
    public static final String KEY_COUNTING_YOUNG = "young";
    public static final String KEY_COUNTING_YEARLING = "yearling";
    public static final String KEY_COUNTING_UNDETERMINED = "sex_age_unspecified";

    private int mCountAdultMale;
    private int mCountAdultFemale;
    private int mCountAdultUndetermined;
    private int mCountNotAdult;
    private int mCountYoung;
    private int mCountYearling;
    private int mCountUndetermined;

    public Taxon(long pTaxonId) {

        super(pTaxonId);
    }

    public Taxon(Parcel source) {

        super(source);

        setCountAdultMale(source.readInt());
        setCountAdultFemale(source.readInt());
        setCountAdultUndetermined(source.readInt());
        setCountNotAdult(source.readInt());
        setCountYoung(source.readInt());
        setCountYearling(source.readInt());
        setCountUndetermined(source.readInt());
    }

    public int getCountAdultMale() {

        return mCountAdultMale;
    }

    public void setCountAdultMale(int pCountAdultMale) {

        this.mCountAdultMale = pCountAdultMale;
    }

    public int getCountAdultFemale() {

        return mCountAdultFemale;
    }

    public void setCountAdultFemale(int pCountAdultFemale) {

        this.mCountAdultFemale = pCountAdultFemale;
    }

    public int getCountAdultUndetermined() {

        return mCountAdultUndetermined;
    }

    public void setCountAdultUndetermined(int pCountAdultUndetermined) {

        this.mCountAdultUndetermined = pCountAdultUndetermined;
    }

    public int getCountNotAdult() {

        return mCountNotAdult;
    }

    public void setCountNotAdult(int pCountNotAdult) {

        this.mCountNotAdult = pCountNotAdult;
    }

    public int getCountYoung() {

        return mCountYoung;
    }

    public void setCountYoung(int pCountYoung) {

        this.mCountYoung = pCountYoung;
    }

    public int getCountYearling() {

        return mCountYearling;
    }

    public void setCountYearling(int pCountYearling) {

        this.mCountYearling = pCountYearling;
    }

    public int getCountUndetermined() {

        return mCountUndetermined;
    }

    public void setCountUndetermined(int pCountUndetermined) {

        this.mCountUndetermined = pCountUndetermined;
    }

    /**
     * Compute the total counting from {@link #getCountAdultMale()}, {@link #getCountAdultFemale()},
     * {@link #getCountAdultUndetermined()}, {@link #getCountNotAdult()}, {@link #getCountYoung()},
     * {@link #getCountYearling()} and {@link #getCountUndetermined()}.
     *
     * @return the total counting for this taxon
     */
    public int counting() {

        return getCountAdultMale() + getCountAdultFemale() + getCountAdultUndetermined() + getCountNotAdult() + getCountYoung() + getCountYearling() + getCountUndetermined();
    }

    public JSONObject getJSONObject() throws JSONException {

        JSONObject json = super.getJSONObject();

        JSONObject jsonCounting = new JSONObject();
        jsonCounting.put(KEY_COUNTING_ADULT_MALE,
                         mCountAdultMale);
        jsonCounting.put(KEY_COUNTING_ADULT_FEMALE,
                         mCountAdultFemale);
        jsonCounting.put(KEY_COUNTING_ADULT_UNDETERMINED,
                         mCountAdultUndetermined);
        jsonCounting.put(KEY_COUNTING_NOT_ADULT,
                         mCountNotAdult);
        jsonCounting.put(KEY_COUNTING_YOUNG,
                         mCountYoung);
        jsonCounting.put(KEY_COUNTING_YEARLING,
                         mCountYearling);
        jsonCounting.put(KEY_COUNTING_UNDETERMINED,
                         mCountUndetermined);

        json.put(KEY_COUNTING,
                 jsonCounting);

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

        dest.writeInt(mCountAdultMale);
        dest.writeInt(mCountAdultFemale);
        dest.writeInt(mCountAdultUndetermined);
        dest.writeInt(mCountNotAdult);
        dest.writeInt(mCountYoung);
        dest.writeInt(mCountYearling);
        dest.writeInt(mCountUndetermined);
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

package com.geonature.mobile.commons.settings;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Qualification metadata settings.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class QualificationSettings
        implements Parcelable {

    private static final String KEY_ORGANISM = "organism";
    private static final String KEY_PROTOCOL = "protocol";
    private static final String KEY_LOT = "lot";

    private int mOrganism;
    private int mProtocol;
    private int mLot;

    private QualificationSettings(@NonNull final Parcel source) {
        this.mOrganism = source.readInt();
        this.mProtocol = source.readInt();
        this.mLot = source.readInt();
    }

    QualificationSettings(int organism,
                          int protocol,
                          int lot) {
        this.mOrganism = organism;
        this.mProtocol = protocol;
        this.mLot = lot;
    }

    @Deprecated
    QualificationSettings(JSONObject json) throws
                                      JSONException {
        this.mOrganism = json.getInt(KEY_ORGANISM);
        this.mProtocol = json.getInt(KEY_PROTOCOL);
        this.mLot = json.getInt(KEY_LOT);
    }

    public int getOrganism() {
        return mOrganism;
    }

    public int getProtocol() {
        return mProtocol;
    }

    public int getLot() {
        return mLot;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest,
                              int flags) {
        dest.writeInt(mOrganism);
        dest.writeInt(mProtocol);
        dest.writeInt(mLot);
    }

    public static final Creator<QualificationSettings> CREATOR = new Creator<QualificationSettings>() {
        @Override
        public QualificationSettings createFromParcel(Parcel source) {

            return new QualificationSettings(source);
        }

        @Override
        public QualificationSettings[] newArray(int size) {

            return new QualificationSettings[size];
        }
    };
}

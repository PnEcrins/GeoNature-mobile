package com.makina.ecrins.commons.settings;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Protocol settings.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class ProtocolSettings
        implements Parcelable {

    private static final String KEY_PROTOCOL_ORGANISM = "organism";
    private static final String KEY_PROTOCOL_PROTOCOL = "protocol";
    private static final String KEY_PROTOCOL_LOT = "lot";

    private int mOrganism;
    private int mProtocol;
    private int mLot;

    private ProtocolSettings(@NonNull final Parcel source) {
        this.mOrganism = source.readInt();
        this.mProtocol = source.readInt();
        this.mLot = source.readInt();
    }

    ProtocolSettings(int organism,
                     int protocol,
                     int lot) {
        this.mOrganism = organism;
        this.mProtocol = protocol;
        this.mLot = lot;
    }

    @Deprecated
    ProtocolSettings(JSONObject json) throws
                                      JSONException {
        this.mOrganism = json.getInt(KEY_PROTOCOL_ORGANISM);
        this.mProtocol = json.getInt(KEY_PROTOCOL_PROTOCOL);
        this.mLot = json.getInt(KEY_PROTOCOL_LOT);
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

    public static final Creator<ProtocolSettings> CREATOR = new Creator<ProtocolSettings>() {
        @Override
        public ProtocolSettings createFromParcel(Parcel source) {

            return new ProtocolSettings(source);
        }

        @Override
        public ProtocolSettings[] newArray(int size) {

            return new ProtocolSettings[size];
        }
    };
}

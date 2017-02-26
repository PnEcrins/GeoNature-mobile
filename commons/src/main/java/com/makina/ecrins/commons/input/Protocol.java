package com.makina.ecrins.commons.input;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * The protocol to use for each {@link AbstractInput}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class Protocol
        implements Parcelable {

    private static final String KEY_PROTOCOL_ORGANISM = "organism";
    private static final String KEY_PROTOCOL_PROTOCOL = "protocol";
    private static final String KEY_PROTOCOL_LOT = "lot";

    private int mOrganism;
    private int mProtocol;
    private int mLot;

    private Protocol(@NonNull final Parcel source) {
        this.mOrganism = source.readInt();
        this.mProtocol = source.readInt();
        this.mLot = source.readInt();
    }

    public Protocol(int organism,
                    int protocol,
                    int lot) {
        this.mOrganism = organism;
        this.mProtocol = protocol;
        this.mLot = lot;
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

    /**
     * @deprecated use {@link InputJsonReader} instead
     */
    @Deprecated
    public JSONObject getJSONObject() throws
                                      JSONException {
        final JSONObject json = new JSONObject();
        json.put(KEY_PROTOCOL_ORGANISM,
                 mOrganism);
        json.put(KEY_PROTOCOL_PROTOCOL,
                 mProtocol);
        json.put(KEY_PROTOCOL_LOT,
                 mLot);

        return json;
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

    public static final Creator<Protocol> CREATOR = new Creator<Protocol>() {
        @Override
        public Protocol createFromParcel(Parcel source) {

            return new Protocol(source);
        }

        @Override
        public Protocol[] newArray(int size) {

            return new Protocol[size];
        }
    };
}

package com.makina.ecrins.invertebrate.inputs;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.makina.ecrins.commons.input.AbstractInput;
import com.makina.ecrins.commons.input.AbstractTaxon;
import com.makina.ecrins.commons.input.InputType;
import com.makina.ecrins.maps.location.Geolocation;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes a current input.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class Input
        extends AbstractInput {

    private static final String KEY_GEOLOCATION = "geolocation";
    private static final String KEY_ENVIRONMENT = "environment";

    private Geolocation mGeolocation;
    private long mEnvironmentId;

    public Input() {
        super(InputType.INVERTEBRATE);

        mGeolocation = null;
        mEnvironmentId = 0;
    }

    public Input(Parcel source) {
        super(source);

        mGeolocation = source.readParcelable(Geolocation.class.getClassLoader());
        mEnvironmentId = source.readLong();
    }

    public Geolocation getGeolocation() {
        return mGeolocation;
    }

    public void setGeolocation(Geolocation pGeolocation) {
        this.mGeolocation = pGeolocation;
    }

    public long getEnvironmentId() {
        return mEnvironmentId;
    }

    public void setEnvironmentId(long pEnvironmentId) {
        this.mEnvironmentId = pEnvironmentId;
    }

    public JSONObject getJSONObject() throws
                                      JSONException {
        final JSONObject json = super.getJSONObject();

        json.put(KEY_GEOLOCATION,
                 mGeolocation.getJSONObject());
        json.put(KEY_ENVIRONMENT,
                 mEnvironmentId);

        return json;
    }

    @NonNull
    @Override
    public String getDateFormat() {
        return "yyyy/MM/dd kk:mm";
    }

    @Override
    public void writeToParcel(Parcel dest,
                              int flags) {
        super.writeToParcel(dest,
                            flags);

        dest.writeParcelable(mGeolocation,
                             0);
        dest.writeLong(mEnvironmentId);
    }

    @Override
    public List<AbstractTaxon> getTaxaFromParcel(Parcel source) {
        final List<Taxon> taxa = new ArrayList<>();
        source.readTypedList(taxa,
                             Taxon.CREATOR);

        return new ArrayList<AbstractTaxon>(taxa);
    }

    public static final Parcelable.Creator<Input> CREATOR = new Parcelable.Creator<Input>() {
        @Override
        public Input createFromParcel(Parcel source) {

            return new Input(source);
        }

        @Override
        public Input[] newArray(int size) {

            return new Input[size];
        }
    };
}

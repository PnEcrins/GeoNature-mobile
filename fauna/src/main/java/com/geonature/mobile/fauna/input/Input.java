package com.geonature.mobile.fauna.input;

import android.os.Parcel;
import android.os.Parcelable;

import com.geonature.mobile.commons.input.AbstractInput;
import com.geonature.mobile.commons.input.AbstractTaxon;
import com.geonature.mobile.commons.input.InputType;
import com.geonature.mobile.maps.location.Geolocation;

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

    private Geolocation mGeolocation;

    public Input() {
        super(InputType.FAUNA);

        mGeolocation = null;
    }

    public Input(Parcel source) {
        super(source);

        mGeolocation = source.readParcelable(Geolocation.class.getClassLoader());
    }

    public Geolocation getGeolocation() {
        return mGeolocation;
    }

    public void setGeolocation(Geolocation pGeolocation) {
        this.mGeolocation = pGeolocation;
    }

    public JSONObject getJSONObject() throws
                                      JSONException {
        final JSONObject json = super.getJSONObject();

        json.put(KEY_GEOLOCATION,
                 mGeolocation.getJSONObject());

        return json;
    }

    @Override
    public void writeToParcel(Parcel dest,
                              int flags) {
        super.writeToParcel(dest,
                            flags);

        dest.writeParcelable(mGeolocation,
                             0);
    }

    @Override
    public List<AbstractTaxon> getTaxaFromParcel(Parcel source) {
        List<Taxon> taxa = new ArrayList<>();
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

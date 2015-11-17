package com.makina.ecrins.fauna.input;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateFormat;

import com.makina.ecrins.commons.input.AbstractInput;
import com.makina.ecrins.commons.input.AbstractTaxon;
import com.makina.ecrins.commons.input.InputType;
import com.makina.ecrins.commons.input.Observer;
import com.makina.ecrins.maps.location.Geolocation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Describes a current input.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class Input
        extends AbstractInput {

    public static final String KEY_OBSERVERS_ID = "observers_id";
    public static final String KEY_DATE_OBS = "dateobs";
    public static final String KEY_INPUT_TYPE = "input_type";
    public static final String KEY_GEOLOCATION = "geolocation";
    public static final String KEY_ENVIRONMENT = "environment";

    private Map<Long, Observer> mObservers;
    private Date mDate;
    private InputType mType;
    private Geolocation mGeolocation;

    public Input() {

        super();

        mDate = new Date();
        mType = InputType.FAUNA;
        mObservers = new TreeMap<>();
        mGeolocation = null;
    }

    public Input(Parcel source) {

        super(source);

        List<Observer> observers = new ArrayList<>();
        source.readTypedList(observers,
                             Observer.CREATOR);
        mObservers = new TreeMap<>();

        for (Observer observer : observers) {
            mObservers.put(observer.getObserverId(),
                           observer);
        }

        mDate = (Date) source.readSerializable();
        mType = (InputType) source.readSerializable();
        mGeolocation = source.readParcelable(Geolocation.class.getClassLoader());
    }

    public Map<Long, Observer> getObservers() {

        return mObservers;
    }

    public Date getDate() {

        return mDate;
    }

    public void setDate(Date pDate) {

        this.mDate = pDate;
    }

    public InputType getType() {

        return mType;
    }

    public Geolocation getGeolocation() {

        return mGeolocation;
    }

    public void setGeolocation(Geolocation pGeolocation) {

        this.mGeolocation = pGeolocation;
    }

    public JSONObject getJSONObject() throws JSONException {

        JSONObject json = super.getJSONObject();

        json.put(KEY_INPUT_TYPE,
                 mType.getValue());
        json.put(KEY_DATE_OBS,
                 DateFormat.format("yyyy/MM/dd",
                                   mDate));

        JSONArray jsonObservers = new JSONArray();

        for (Observer observer : mObservers.values()) {
            jsonObservers.put(observer.getObserverId());
        }

        json.put(KEY_OBSERVERS_ID,
                 jsonObservers);
        json.put(KEY_GEOLOCATION,
                 mGeolocation.getJSONObject());

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

        List<Observer> observers = new ArrayList<>();

        for (Observer observer : mObservers.values()) {
            observers.add(observer);
        }

        dest.writeTypedList(observers);

        dest.writeSerializable(mDate);
        dest.writeSerializable(mType);
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

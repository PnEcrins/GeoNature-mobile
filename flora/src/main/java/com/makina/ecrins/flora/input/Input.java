package com.makina.ecrins.flora.input;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateFormat;

import com.makina.ecrins.commons.input.AbstractInput;
import com.makina.ecrins.commons.input.AbstractTaxon;
import com.makina.ecrins.commons.input.InputType;
import com.makina.ecrins.commons.input.Observer;

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
public class Input extends AbstractInput {

    public static final String KEY_OBSERVERS_ID = "observers_id";
    public static final String KEY_DATE_OBS = "dateobs";
    public static final String KEY_INPUT_TYPE = "input_type";

    private final Map<Long, Observer> mObservers;
    private Date mDate;
    private InputType mType;

    public Input() {
        super();

        mDate = new Date();
        mType = InputType.FLORA;
        mObservers = new TreeMap<>();
    }

    public Input(Parcel source) {
        super(source);

        List<Observer> observers = new ArrayList<>();
        source.readTypedList(observers, Observer.CREATOR);
        mObservers = new TreeMap<>();

        for (Observer observer : observers) {
            mObservers.put(observer.getObserverId(), observer);
        }

        mDate = (Date) source.readSerializable();
        mType = (InputType) source.readSerializable();
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

    public JSONObject getJSONObject() throws JSONException {
        JSONObject json = super.getJSONObject();

        json.put(KEY_INPUT_TYPE, mType.getValue());
        json.put(KEY_DATE_OBS, DateFormat.format("yyyy/MM/dd", mDate));

        JSONArray jsonObservers = new JSONArray();

        for (Observer observer : mObservers.values()) {
            jsonObservers.put(observer.getObserverId());
        }

        json.put(KEY_OBSERVERS_ID, jsonObservers);

        return json;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeTypedList(new ArrayList<>(mObservers.values()));
        dest.writeSerializable(mDate);
        dest.writeSerializable(mType);
    }

    @Override
    public List<AbstractTaxon> getTaxaFromParcel(Parcel source) {
        final List<Taxon> taxa = new ArrayList<>();
        source.readTypedList(taxa, Taxon.CREATOR);

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

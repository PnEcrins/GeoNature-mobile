package com.geonature.mobile.commons.input;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Dummy implementation of {@link AbstractInput}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class DummyInput
        extends AbstractInput {

    public DummyInput(@NonNull InputType type) {
        super(type);
    }

    public DummyInput(Parcel source) {
        super(source);
    }

    @Override
    public List<AbstractTaxon> getTaxaFromParcel(Parcel source) {
        final List<DummyTaxon> taxa = new ArrayList<>();
        source.readTypedList(taxa,
                             DummyTaxon.CREATOR);

        return new ArrayList<AbstractTaxon>(taxa);
    }

    public static final Parcelable.Creator<DummyInput> CREATOR = new Parcelable.Creator<DummyInput>() {
        @Override
        public DummyInput createFromParcel(Parcel source) {

            return new DummyInput(source);
        }

        @Override
        public DummyInput[] newArray(int size) {

            return new DummyInput[size];
        }
    };
}

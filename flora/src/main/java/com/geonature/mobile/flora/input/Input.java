package com.geonature.mobile.flora.input;

import android.os.Parcel;
import android.os.Parcelable;

import com.geonature.mobile.commons.input.AbstractInput;
import com.geonature.mobile.commons.input.AbstractTaxon;
import com.geonature.mobile.commons.input.InputType;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes a current input.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class Input
        extends AbstractInput {

    public Input() {
        super(InputType.FLORA);
    }

    public Input(Parcel source) {
        super(source);
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

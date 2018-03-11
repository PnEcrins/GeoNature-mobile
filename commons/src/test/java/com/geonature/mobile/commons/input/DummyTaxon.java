package com.geonature.mobile.commons.input;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Dummy implementation of {@link AbstractTaxon}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class DummyTaxon
        extends AbstractTaxon {

    public DummyTaxon(long pTaxonId) {
        super(pTaxonId);
    }

    public DummyTaxon(Parcel source) {
        super(source);
    }

    public static final Parcelable.Creator<DummyTaxon> CREATOR = new Parcelable.Creator<DummyTaxon>() {
        @Override
        public DummyTaxon createFromParcel(Parcel source) {

            return new DummyTaxon(source);
        }

        @Override
        public DummyTaxon[] newArray(int size) {

            return new DummyTaxon[size];
        }
    };
}

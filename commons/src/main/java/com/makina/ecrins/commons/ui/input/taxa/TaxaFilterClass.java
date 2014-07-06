package com.makina.ecrins.commons.ui.input.taxa;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;

/**
 * <code>List</code> of Classes filter.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class TaxaFilterClass implements Parcelable {

    private SparseArray<TaxonFilterClass> mFilterClasses;

    public TaxaFilterClass() {
        this.mFilterClasses = new SparseArray<TaxonFilterClass>();
    }

    public TaxaFilterClass(TaxonFilterClass... filterClasses) {
        this.mFilterClasses = new SparseArray<TaxonFilterClass>();

        for (TaxonFilterClass filterClass : filterClasses) {
            this.mFilterClasses.put(filterClass.getResourceId(), filterClass);
        }
    }

    public SparseArray<TaxonFilterClass> getFilterClasses() {
        return mFilterClasses;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        List<TaxonFilterClass> filterClasses = new ArrayList<TaxonFilterClass>();

        for (int i = 0; i < mFilterClasses.size(); i++) {
            filterClasses.add(mFilterClasses.valueAt(i));
        }

        // writes parcel as a single list
        dest.writeTypedList(filterClasses);
    }

    public static final Parcelable.Creator<TaxaFilterClass> CREATOR = new Parcelable.Creator<TaxaFilterClass>() {
        @Override
        public TaxaFilterClass createFromParcel(Parcel source) {
            TaxaFilterClass taxaFilterClass = new TaxaFilterClass();
            List<TaxonFilterClass> filterClasses = new ArrayList<TaxonFilterClass>();
            source.readTypedList(filterClasses, TaxonFilterClass.CREATOR);

            return taxaFilterClass;
        }

        @Override
        public TaxaFilterClass[] newArray(int size) {
            return new TaxaFilterClass[size];
        }
    };

    /**
     * Describes a class filter.
     *
     * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    public static class TaxonFilterClass implements Parcelable {
        private long mId;
        private String mLabel;
        private int mResourceId;
        private boolean mSelected;

        public TaxonFilterClass(long pId, String pLabel, int pResourceId, boolean pSelected) {
            this.mId = pId;
            this.mLabel = pLabel;
            this.mResourceId = pResourceId;
            this.mSelected = pSelected;
        }

        public long getId() {
            return mId;
        }

        public String getLabel() {
            return mLabel;
        }

        public int getResourceId() {
            return mResourceId;
        }

        public boolean isSelected() {
            return mSelected;
        }

        public void setSelected(boolean pSelected) {
            this.mSelected = pSelected;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(mId);
            dest.writeString(mLabel);
            dest.writeInt(mResourceId);
            dest.writeByte((byte) (mSelected ? 1 : 0)); // as boolean value
        }

        public static final Parcelable.Creator<TaxonFilterClass> CREATOR = new Parcelable.Creator<TaxonFilterClass>() {
            @Override
            public TaxonFilterClass createFromParcel(Parcel source) {
                return new TaxonFilterClass(source.readLong(), source.readString(), source.readInt(), source.readByte() == 1);
            }

            @Override
            public TaxonFilterClass[] newArray(int size) {
                return new TaxonFilterClass[size];
            }
        };
    }
}

package com.makina.ecrins.commons.ui.input.taxa;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * Describes the current status of a taxon.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class TaxonStatus implements Parcelable {

    public static final String STATUS_SEARCH = "red";
    public static final String STATUS_NEW = "orange";
    public static final String STATUS_OPTIONAL = "gray";

    private final String mStatus;
    private boolean mSelected;
    private final int mResourceLabelId;
    private final int mResourceColorId;

    public TaxonStatus(String pStatus, boolean psSelected, int pResourceLabelId, int pResourceColorId) {
        this.mStatus = pStatus;
        this.mSelected = psSelected;
        this.mResourceLabelId = pResourceLabelId;
        this.mResourceColorId = pResourceColorId;
    }

    public String getStatus() {
        return mStatus;
    }

    public boolean isSelected() {
        return mSelected;
    }

    public void setSelected(boolean pSelected) {
        this.mSelected = pSelected;
    }

    public int getResourceLabelId() {
        return mResourceLabelId;
    }

    public int getResourceColorId() {
        return mResourceColorId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mStatus);
        dest.writeByte((byte) (mSelected ? 1 : 0)); // as boolean value
        dest.writeInt(mResourceLabelId);
        dest.writeInt(mResourceColorId);
    }

    public static final Parcelable.Creator<TaxonStatus> CREATOR = new Parcelable.Creator<TaxonStatus>() {
        @Override
        public TaxonStatus createFromParcel(Parcel source) {
            return new TaxonStatus(source.readString(), source.readByte() == 1, source.readInt(), source.readInt());
        }

        @Override
        public TaxonStatus[] newArray(int size) {
            return new TaxonStatus[size];
        }
    };
}

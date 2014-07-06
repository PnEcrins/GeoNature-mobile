package com.makina.ecrins.commons.input;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Describes an observer from {@link com.makina.ecrins.commons.input.AbstractInput}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class Observer implements Parcelable, Comparable<Observer> {

    private long mObserverId;
    private String mLastname;
    private String mFirstname;

    public Observer(long pObserverId, String pLastname, String pFirstname) {
        this.mObserverId = pObserverId;
        this.mLastname = pLastname;
        this.mFirstname = pFirstname;
    }

    public Observer(Parcel source) {
        this.mObserverId = source.readLong();
        this.mLastname = source.readString();
        this.mFirstname = source.readString();
    }

    public long getObserverId() {
        return mObserverId;
    }

    public String getLastname() {
        return mLastname;
    }

    public void setLastname(String pLastname) {
        this.mLastname = pLastname;
    }

    public String getFirstname() {
        return mFirstname;
    }

    public void setFirstname(String pFirstname) {
        this.mFirstname = pFirstname;
    }

    @Override
    public boolean equals(Object o) {
        if ((o != null) && (o.getClass().equals(Observer.class))) {
            return this.mObserverId == ((Observer) o).getObserverId();
        }
        else {
            return false;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mObserverId);
        dest.writeString(mLastname);
        dest.writeString(mFirstname);
    }

    @Override
    public int compareTo(Observer another) {
        return this.mLastname.compareToIgnoreCase(another.getLastname()) + this.mFirstname.compareToIgnoreCase(another.getFirstname());
    }

    public static final Creator<Observer> CREATOR = new Creator<Observer>() {
        @Override
        public Observer createFromParcel(Parcel source) {
            return new Observer(source);
        }

        @Override
        public Observer[] newArray(int size) {
            return new Observer[size];
        }
    };
}

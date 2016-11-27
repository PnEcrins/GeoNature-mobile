package com.makina.ecrins.commons.ui.pager;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

/**
 * Describes a {@code ViewPager} metadata:
 * <ul>
 * <li>the {@code ViewPager} size</li>
 * <li>the {@code ViewPager} current position</li>
 * <li>the {@code ViewPager} navigation history if any</li>
 * </ul>
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class Pager
        implements Parcelable {

    long mId;
    private int mSize;
    private int mPosition;
    private final Deque<Integer> mHistory = new ArrayDeque<>();

    public Pager(long id) {
        mId = id;
    }

    public Pager(Parcel source) {
        mId = source.readLong();
        mSize = source.readInt();
        mPosition = source.readInt();

        final List<Integer> navigationHistoryList = new ArrayList<>();
        source.readList(navigationHistoryList,
                        Long.class.getClassLoader());

        mHistory.addAll(navigationHistoryList);
    }

    public long getId() {
        return mId;
    }

    public int getSize() {
        return mSize;
    }

    public void setSize(int size) {
        this.mSize = size;
    }

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        this.mPosition = position;
    }

    @NonNull
    public Deque<Integer> getHistory() {
        return mHistory;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest,
                              int flags) {
        dest.writeLong(mId);
        dest.writeInt(mSize);
        dest.writeInt(mPosition);
        dest.writeList(new ArrayList<>(mHistory));
    }

    @Override
    public String toString() {
        return "Pager{" +
                "id=" + mId +
                ", size=" + mSize +
                ", position=" + mPosition +
                ", history=" + mHistory +
                '}';
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Pager pager = (Pager) o;

        if (mId != pager.mId) {
            return false;
        }

        if (mSize != pager.mSize) {
            return false;
        }

        // noinspection SimplifiableIfStatement
        if (mPosition != pager.mPosition) {
            return false;
        }

        return Arrays.equals(mHistory.toArray(),
                             pager.mHistory.toArray());
    }

    @Override
    public int hashCode() {
        int result = (int) (mId ^ (mId >>> 32));
        result = 31 * result + mSize;
        result = 31 * result + mPosition;
        result = 31 * result + mHistory.hashCode();

        return result;
    }

    public static final Creator<Pager> CREATOR = new Creator<Pager>() {
        @Override
        public Pager createFromParcel(Parcel source) {

            return new Pager(source);
        }

        @Override
        public Pager[] newArray(int size) {

            return new Pager[size];
        }
    };
}

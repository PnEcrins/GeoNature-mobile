package com.makina.ecrins.commons.sync;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Indicates the current status of {@link SyncService}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class SyncStatus implements Parcelable {

    private final Status mStatus;
    private final String mMessage;

    public SyncStatus(Status pStatus, String pMessage) {
        super();
        this.mStatus = pStatus;
        this.mMessage = pMessage;
    }

    public SyncStatus(Parcel source) {
        mStatus = (Status) source.readSerializable();
        mMessage = source.readString();
    }

    public Status getStatus() {
        return mStatus;
    }

    public String getMessage() {
        return mMessage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(mStatus);
        dest.writeString(mMessage);
    }

    public static final Creator<SyncStatus> CREATOR = new Creator<SyncStatus>() {
        @Override
        public SyncStatus createFromParcel(Parcel source) {
            return new SyncStatus(source);
        }

        @Override
        public SyncStatus[] newArray(int size) {
            return new SyncStatus[size];
        }
    };

    public enum Status {
        /**
         * Indicates that the service has not been executed yet.
         */
        PENDING,

        /**
         * Indicates that the service is still running.
         */
        RUNNING,

        /**
         * Indicates that the service has been canceled.
         */
        ABORTED,

        /**
         * Indicates that the service has finished successfully.
         */
        FINISHED,

        /**
         * Indicates that the service has finished with errors.
         */
        FINISHED_WITH_ERRORS
    }
}

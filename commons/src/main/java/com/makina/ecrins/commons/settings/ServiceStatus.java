package com.makina.ecrins.commons.settings;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Indicates the current status of {@link AbstractSettingsService}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 * @deprecated see {@link AbstractAppSettingsIntentService}
 */
@SuppressWarnings("ALL")
@Deprecated
public class ServiceStatus implements Parcelable {

    private Status mStatus;
    private String mMessage;

    public ServiceStatus(Status pStatus, String pMessage) {
        super();

        this.mStatus = pStatus;
        this.mMessage = pMessage;
    }

    public ServiceStatus(Parcel source) {
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

    public static final Creator<ServiceStatus> CREATOR = new Creator<ServiceStatus>() {
        @Override
        public ServiceStatus createFromParcel(Parcel source) {
            return new ServiceStatus(source);
        }

        @Override
        public ServiceStatus[] newArray(int size) {
            return new ServiceStatus[size];
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

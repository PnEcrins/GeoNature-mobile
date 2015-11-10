package com.makina.ecrins.commons.service;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Indicates the current status of {@link com.makina.ecrins.commons.service.AbstractRequestHandler}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class RequestHandlerStatus
        implements Parcelable {

    private Status mStatus;
    private String mMessage;

    public RequestHandlerStatus(
            Status pStatus,
            String pMessage) {
        super();

        this.mStatus = pStatus;
        this.mMessage = pMessage;
    }

    public RequestHandlerStatus(Parcel source) {
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

    public static final Creator<RequestHandlerStatus> CREATOR = new Creator<RequestHandlerStatus>() {
        @Override
        public RequestHandlerStatus createFromParcel(Parcel source) {
            return new RequestHandlerStatus(source);
        }

        @Override
        public RequestHandlerStatus[] newArray(int size) {
            return new RequestHandlerStatus[size];
        }
    };

    public enum Status {

        /**
         * Indicates that the {@link com.makina.ecrins.commons.service.AbstractRequestHandler} has not been executed yet.
         */
        PENDING,

        /**
         * Indicates that the {@link com.makina.ecrins.commons.service.AbstractRequestHandler} is still running.
         */
        RUNNING,

        /**
         * Indicates that the {@link com.makina.ecrins.commons.service.AbstractRequestHandler} has been canceled.
         */
        ABORTED,

        /**
         * Indicates that the {@link com.makina.ecrins.commons.service.AbstractRequestHandler} has finished successfully.
         */
        FINISHED,

        /**
         * Indicates that the {@link com.makina.ecrins.commons.service.AbstractRequestHandler} has finished with errors.
         */
        FINISHED_WITH_ERRORS
    }
}

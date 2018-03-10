package com.makina.ecrins.commons.sync;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Describes a message sent by {@link SyncService}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class SyncMessage implements Parcelable {

    private final MessageType mMessageType;
    private final SyncStatus mSyncStatus;

    public SyncMessage(MessageType pMessageType, SyncStatus pSyncStatus) {
        super();
        this.mMessageType = pMessageType;
        this.mSyncStatus = pSyncStatus;
    }

    public SyncMessage(Parcel source) {
        mMessageType = (MessageType) source.readSerializable();
        mSyncStatus = source.readParcelable(SyncStatus.class.getClassLoader());
    }

    public MessageType getMessageType() {
        return mMessageType;
    }

    public SyncStatus getSyncStatus() {
        return mSyncStatus;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(mMessageType);
        dest.writeParcelable(mSyncStatus, 0);
    }

    public static final Creator<SyncMessage> CREATOR = new Creator<SyncMessage>() {
        @Override
        public SyncMessage createFromParcel(Parcel source) {
            return new SyncMessage(source);
        }

        @Override
        public SyncMessage[] newArray(int size) {
            return new SyncMessage[size];
        }
    };

    public enum MessageType {
        /**
         * Provides informative messages.
         */
        INFO,

        /**
         * Provides warnings.
         */
        WARNING,

        /**
         * Provides failure messages.
         */
        ERROR,

        /**
         * Provides the status of the current download.
         */
        DOWNLOAD_STATUS,

        /**
         * Provides the status of the current upload.
         */
        UPLOAD_STATUS
    }
}

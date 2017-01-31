package com.makina.ecrins.commons.settings;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Dummy implementation of {@link AbstractAppSettings}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class DummyAppSettings
        extends AbstractAppSettings {

    public DummyAppSettings() {
        super();
    }

    public DummyAppSettings(Parcel source) {
        super(source);
    }

    public static final Parcelable.Creator<DummyAppSettings> CREATOR = new Parcelable.Creator<DummyAppSettings>() {
        @Override
        public DummyAppSettings createFromParcel(Parcel source) {

            return new DummyAppSettings(source);
        }

        @Override
        public DummyAppSettings[] newArray(int size) {

            return new DummyAppSettings[size];
        }
    };
}

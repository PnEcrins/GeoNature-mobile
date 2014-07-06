package com.makina.ecrins.commons.sync;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Default settings for synchronizing data.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class SyncSettings implements Parcelable {

    public static final String KEY_SERVER_URL = "url";
    public static final String KEY_TOKEN = "token";
    public static final String KEY_STATUS_URL = "status_url";
    public static final String KEY_IMPORT_URL = "import_url";
    public static final String KEY_EXPORTS = "exports";

    private String mServerUrl;
    private String mToken;
    private String mStatusUrl;
    private String mImportUrl;
    private List<ExportSettings> mExports = new ArrayList<ExportSettings>();

    public SyncSettings(Parcel source) {
        mServerUrl = source.readString();
        mToken = source.readString();
        mStatusUrl = source.readString();
        mImportUrl = source.readString();
        source.readTypedList(mExports, ExportSettings.CREATOR);
    }

    public SyncSettings(JSONObject json) throws JSONException {
        mServerUrl = json.getString(KEY_SERVER_URL);
        mToken = json.getString(KEY_TOKEN);
        mStatusUrl = json.getString(KEY_STATUS_URL);
        mImportUrl = json.getString(KEY_IMPORT_URL);

        JSONArray exportsJsonArray = json.getJSONArray(KEY_EXPORTS);

        for (int i = 0; i < exportsJsonArray.length(); i++) {
            mExports.add(new ExportSettings(exportsJsonArray.getJSONObject(i)));
        }
    }

    public String getServerUrl() {
        return mServerUrl;
    }

    public String getToken() {
        return mToken;
    }

    public String getStatusUrl() {
        return mStatusUrl;
    }

    public String getImportUrl() {
        return mImportUrl;
    }

    public List<ExportSettings> getExports() {
        return mExports;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mServerUrl);
        dest.writeString(mToken);
        dest.writeString(mStatusUrl);
        dest.writeString(mImportUrl);
        dest.writeTypedList(mExports);
    }

    public static final Creator<SyncSettings> CREATOR = new Creator<SyncSettings>() {
        @Override
        public SyncSettings createFromParcel(Parcel source) {
            return new SyncSettings(source);
        }

        @Override
        public SyncSettings[] newArray(int size) {
            return new SyncSettings[size];
        }
    };
}

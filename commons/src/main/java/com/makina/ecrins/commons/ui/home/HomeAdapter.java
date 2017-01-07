package com.makina.ecrins.commons.ui.home;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.makina.ecrins.commons.R;
import com.makina.ecrins.commons.model.MountPoint;
import com.makina.ecrins.commons.settings.AbstractAppSettings;
import com.makina.ecrins.commons.util.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Date;

/**
 * Default {@code Adapter} about home screen.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
class HomeAdapter
        extends RecyclerView.Adapter<HomeAdapter.AbstractViewHolder> {

    private static final String TAG = HomeAdapter.class.getName();

    private static final int VIEW_TYPE_SYNC = 0;

    private final Context mContext;
    private final OnHomeAdapterListener mOnHomeAdapterListener;
    private AbstractAppSettings mAppSettings;

    HomeAdapter(@NonNull final Context context,
                @NonNull final OnHomeAdapterListener onHomeAdapterListener) {
        this.mContext = context;
        this.mOnHomeAdapterListener = onHomeAdapterListener;
    }

    @Override
    public AbstractViewHolder onCreateViewHolder(ViewGroup parent,
                                                 int viewType) {
        switch (viewType) {
            case VIEW_TYPE_SYNC:
                return new SyncViewHolder(LayoutInflater.from(parent.getContext())
                                                        .inflate(R.layout.list_item_home_sync,
                                                                 parent,
                                                                 false));
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(AbstractViewHolder holder,
                                 int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void setAppSettings(@NonNull final AbstractAppSettings appSettings) {
        this.mAppSettings = appSettings;

        notifyDataSetChanged();
    }

    private class SyncViewHolder
            extends AbstractViewHolder {
        private TextView mTextViewLastSync;
        private TextView mTextViewInputsToSync;
        private Button mButtonStartSync;

        SyncViewHolder(View itemView) {
            super(itemView);

            mTextViewLastSync = (TextView) itemView.findViewById(R.id.textViewLastSync);
            mTextViewInputsToSync = (TextView) itemView.findViewById(R.id.textViewInputsToSync);
            mButtonStartSync = (Button) itemView.findViewById(R.id.buttonStartSync);
        }

        @Override
        public void bind(int position) {
            // enabled only if AbstractAppSettings is loaded
            mButtonStartSync.setEnabled(mAppSettings != null);
            mButtonStartSync.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnHomeAdapterListener.onStartSync();
                }
            });

            // get the last synchronization date using the last modified date of local database file
            new AsyncTask<Void, Void, Date>() {

                @Override
                protected Date doInBackground(Void... params) {
                    if (mAppSettings == null) {
                        return null;
                    }

                    try {
                        final File dbFile = FileUtils.getFile(FileUtils.getDatabaseFolder(mContext,
                                                                                          MountPoint.StorageType.INTERNAL),
                                                              mAppSettings.getDbSettings()
                                                                          .getName());

                        if (dbFile == null) {
                            return null;
                        }

                        if (dbFile.lastModified() == 0) {
                            return null;
                        }

                        return new Date(dbFile.lastModified());
                    }
                    catch (IOException ioe) {
                        Log.w(TAG,
                              ioe.getMessage());
                    }

                    return null;
                }

                @Override
                protected void onPostExecute(Date date) {
                    if (date == null) {
                        mTextViewLastSync.setText(R.string.synchro_last_synchronization_never);
                    }
                    else {
                        mTextViewLastSync.setText(DateFormat.format(mContext.getString(R.string.synchro_last_synchronization_date),
                                                                    date));
                    }
                }
            }.execute();

            // get the number of inputs not synchronized
            new AsyncTask<Void, Void, Integer>() {
                @Override
                protected Integer doInBackground(Void... params) {
                    try {
                        File inputDir = FileUtils.getInputsFolder(mContext);

                        if (inputDir.exists()) {
                            return inputDir.listFiles(new FileFilter() {
                                @Override
                                public boolean accept(File pathname) {
                                    return pathname.getName()
                                                   .startsWith("input_") && pathname.getName()
                                                                                    .endsWith(".json");
                                }
                            }).length;
                        }
                    }
                    catch (IOException ioe) {
                        Log.w(TAG,
                              ioe.getMessage());
                    }

                    return 0;
                }

                @Override
                protected void onPostExecute(Integer integer) {
                    mTextViewInputsToSync.setText(NumberFormat.getInstance()
                                                              .format(integer));
                }
            }.execute();
        }
    }

    abstract class AbstractViewHolder
            extends RecyclerView.ViewHolder {

        AbstractViewHolder(View itemView) {
            super(itemView);
        }

        public abstract void bind(int position);
    }

    /**
     * Callback for {@link HomeAdapter}.
     *
     * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
     */
    interface OnHomeAdapterListener {
        void onStartSync();
    }
}

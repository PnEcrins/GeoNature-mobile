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
import java.lang.ref.WeakReference;
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

    private final Context mContext;
    private final OnHomeAdapterListener mOnHomeAdapterListener;
    private AbstractAppSettings mAppSettings;

    HomeAdapter(@NonNull final Context context,
                @NonNull final OnHomeAdapterListener onHomeAdapterListener) {
        this.mContext = context;
        this.mOnHomeAdapterListener = onHomeAdapterListener;
    }

    @NonNull
    @Override
    public AbstractViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                 int viewType) {
        return new SyncViewHolder(LayoutInflater.from(parent.getContext())
                                                .inflate(R.layout.list_item_home_sync,
                                                         parent,
                                                         false));
    }

    @Override
    public void onBindViewHolder(@NonNull AbstractViewHolder holder,
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
        private final TextView mTextViewLastSync;
        private final TextView mTextViewInputsToSync;
        private final Button mButtonStartSync;

        SyncViewHolder(View itemView) {
            super(itemView);

            mTextViewLastSync = itemView.findViewById(R.id.textViewLastSync);
            mTextViewInputsToSync = itemView.findViewById(R.id.textViewInputsToSync);
            mButtonStartSync = itemView.findViewById(R.id.buttonStartSync);
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
            new LastSynchronizationDateAsyncTask(mContext,
                                                 mTextViewLastSync).execute(mAppSettings);

            // get the number of inputs not synchronized
            new NumberOfInputsNotSynchronizedAsyncTask(mContext,
                                                       mTextViewInputsToSync).execute();
        }
    }

    abstract class AbstractViewHolder
            extends RecyclerView.ViewHolder {

        AbstractViewHolder(View itemView) {
            super(itemView);
        }

        public abstract void bind(int position);
    }

    static class LastSynchronizationDateAsyncTask
            extends AsyncTask<AbstractAppSettings, Void, Date> {

        private final WeakReference<Context> mContext;
        private final WeakReference<TextView> mTextView;

        LastSynchronizationDateAsyncTask(@NonNull final Context pContext,
                                         @NonNull final TextView pTextView) {
            this.mContext = new WeakReference<>(pContext);
            this.mTextView = new WeakReference<>(pTextView);
        }

        @Override
        protected Date doInBackground(AbstractAppSettings... params) {
            final Context context = mContext.get();

            if (context == null) {
                return null;
            }

            if (params == null || params.length == 0) {
                return null;
            }

            final AbstractAppSettings appSettings = params[0];

            if (appSettings == null) {
                return null;
            }

            try {
                final File dbFile = FileUtils.getFile(FileUtils.getDatabaseFolder(context,
                                                                                  MountPoint.StorageType.INTERNAL),
                                                      appSettings.getDbSettings()
                                                                 .getName());

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
            final Context context = mContext.get();
            final TextView textView = mTextView.get();

            if (context == null) {
                return;
            }

            if (textView == null) {
                return;
            }

            if (date == null) {
                textView.setText(R.string.synchro_last_synchronization_never);
            }
            else {
                textView.setText(DateFormat.format(context.getString(R.string.synchro_last_synchronization_date),
                                                   date));
            }
        }
    }

    static class NumberOfInputsNotSynchronizedAsyncTask
            extends AsyncTask<Void, Void, Integer> {

        private final WeakReference<Context> mContext;
        private final WeakReference<TextView> mTextView;

        NumberOfInputsNotSynchronizedAsyncTask(@NonNull final Context pContext,
                                               @NonNull final TextView pTextView) {
            this.mContext = new WeakReference<>(pContext);
            this.mTextView = new WeakReference<>(pTextView);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            final Context context = mContext.get();

            if (context == null) {
                return null;
            }

            try {
                final File inputDir = FileUtils.getInputsFolder(context);

                if (inputDir.exists()) {
                    final File[] listFiles = inputDir.listFiles(new FileFilter() {
                        @Override
                        public boolean accept(File pathname) {
                            return pathname.getName()
                                           .startsWith("input_") && pathname.getName()
                                                                            .endsWith(".json");
                        }
                    });
                    return (listFiles == null) ? 0 : listFiles.length;
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
            final TextView textView = mTextView.get();

            if (textView == null) {
                return;
            }

            textView.setText(NumberFormat.getInstance()
                                         .format(integer));
        }
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

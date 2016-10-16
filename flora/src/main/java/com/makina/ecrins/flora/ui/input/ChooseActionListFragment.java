package com.makina.ecrins.flora.ui.input;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.makina.ecrins.commons.BuildConfig;
import com.makina.ecrins.commons.ui.dialog.AlertDialogFragment;
import com.makina.ecrins.commons.ui.input.OnInputFragmentListener;
import com.makina.ecrins.commons.ui.pager.IValidateWithNavigationControlFragment;
import com.makina.ecrins.flora.R;
import com.makina.ecrins.flora.input.Input;
import com.makina.ecrins.flora.input.Taxon;


/**
 * Step 12: The user can choose an action within a list.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class ChooseActionListFragment
        extends ListFragment
        implements IValidateWithNavigationControlFragment {

    private static final String TAG = ChooseActionListFragment.class.getName();

    private static final String ALERT_DIALOG_DELETE_AREA_FRAGMENT = "alert_dialog_delete_area";

    private static final int DEFAULT_SCREEN_OFF_TIMEOUT = 15000;
    private static final String KEY_DEFAULT_SCREEN_OFF_TIMEOUT = "default_screen_off_timeout";

    private ActionItemArrayAdapter mAdapter = null;

    private Input mInput;
    private int mDefaultScreenOffTimeOut;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDefaultScreenOffTimeOut = (savedInstanceState == null) ? Settings.System.getInt(getActivity().getContentResolver(),
                                                                                         Settings.System.SCREEN_OFF_TIMEOUT,
                                                                                         DEFAULT_SCREEN_OFF_TIMEOUT) : savedInstanceState.getInt(KEY_DEFAULT_SCREEN_OFF_TIMEOUT,
                                                                                                                                                 DEFAULT_SCREEN_OFF_TIMEOUT);

        final AlertDialogFragment alertDialogFragment = (AlertDialogFragment) getActivity().getSupportFragmentManager()
                                                                                           .findFragmentByTag(ALERT_DIALOG_DELETE_AREA_FRAGMENT);

        if (alertDialogFragment != null) {
            alertDialogFragment.dismiss();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_choose_action,
                                container,
                                false);
    }

    @Override
    public void onViewCreated(View view,
                              Bundle savedInstanceState) {
        super.onViewCreated(view,
                            savedInstanceState);

        if (getListAdapter() == null) {
            mAdapter = new ActionItemArrayAdapter(getActivity(),
                                                  android.R.layout.simple_list_item_1);
            mAdapter.add(new ActionItem() {
                @Override
                public String getName() {
                    return getString(R.string.choose_action_pause);
                }

                @Override
                public void performAction() {
                    Settings.System.putInt(getActivity().getContentResolver(),
                                           Settings.System.SCREEN_OFF_TIMEOUT,
                                           1000);
                }
            });
            mAdapter.add(new ActionItem() {
                @Override
                public String getName() {
                    return getString(R.string.choose_action_area);
                }

                @Override
                public void performAction() {
                    if (mInput.getCurrentSelectedTaxon() != null) {
                        ((Taxon) mInput.getCurrentSelectedTaxon()).setCurrentSelectedAreaId(null);
                    }

                    ((PagerFragmentActivity) getActivity()).goToPageByKey(R.string.pager_fragment_webview_ap_title);
                }
            });
            mAdapter.add(new ActionItem() {
                @Override
                public String getName() {
                    return getString(R.string.choose_action_finish);
                }

                @Override
                public void performAction() {
                    ((PagerFragmentActivity) getActivity()).goToPageByKey(R.string.pager_fragment_webview_pa_title);
                }
            });

            setListAdapter(mAdapter);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnInputFragmentListener) {
            final OnInputFragmentListener onInputFragmentListener = (OnInputFragmentListener) context;
            mInput = (Input) onInputFragmentListener.getInput();
        }
        else {
            throw new RuntimeException(getContext().toString() + " must implement OnInputFragmentListener");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_DEFAULT_SCREEN_OFF_TIMEOUT,
                        mDefaultScreenOffTimeOut);
    }

    @Override
    public void onListItemClick(ListView l,
                                View v,
                                int position,
                                long id) {
        if (mAdapter != null) {
            final ActionItem actionItem = mAdapter.getItem(position);

            if (actionItem != null) {
                actionItem.performAction();
            }
        }
    }

    @Override
    public int getResourceTitle() {
        return R.string.pager_fragment_choose_action_title;
    }

    @Override
    public boolean getPagingEnabled() {
        return true;
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public void refreshView() {
        ((AppCompatActivity) getActivity()).getSupportActionBar()
                                           .setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        // restore default settings
        Settings.System.putInt(getActivity().getContentResolver(),
                               Settings.System.SCREEN_OFF_TIMEOUT,
                               mDefaultScreenOffTimeOut);

        if ((mInput.getCurrentSelectedTaxon() != null) && (((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea() == null)) {
            // restore the previously added Area for this Taxon
            ((Taxon) mInput.getCurrentSelectedTaxon()).setCurrentSelectedAreaId(((Taxon) mInput.getCurrentSelectedTaxon()).getLastInsertedAreaId());

            if (((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea() == null) {
                // should never occur !
                // TODO: go back to previous and valid page in navigation history
                Log.w(TAG,
                      "refreshView: no area found for taxon " +
                              mInput.getCurrentSelectedTaxon()
                                    .getId() + " !");
            }
        }
        else {
            // an Area was already added and edited for this Taxon, so perform a full consistency check to keep or not this Area.
            if (((Taxon) mInput.getCurrentSelectedTaxon()).getAreas()
                                                          .size() > (((PagerFragmentActivity) getActivity()).countPagesInHistory(getResourceTitle()) + 1)) {
                confirmBeforeDeleteArea(((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedAreaId());
            }
        }
    }

    @Override
    public boolean getPagingToForwardEnabled() {
        return false;
    }

    @Override
    public boolean getPagingToPreviousEnabled() {
        return true;
    }

    /**
     * Describes an action.
     *
     * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    public interface ActionItem {

        /**
         * Returns the action name
         *
         * @return the action name
         */
        String getName();

        /**
         * Performs a custom action
         */
        void performAction();
    }

    private class ActionItemArrayAdapter
            extends ArrayAdapter<ActionItem> {

        private final LayoutInflater mInflater;
        private final int mResourceId;

        public ActionItemArrayAdapter(Context context,
                                      int resource) {
            super(context,
                  resource);

            mResourceId = resource;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @NonNull
        @Override
        public View getView(int position,
                            View convertView,
                            @NonNull ViewGroup parent) {
            View view;

            if (convertView == null) {
                view = mInflater.inflate(mResourceId,
                                         parent,
                                         false);
            }
            else {
                view = convertView;
            }

            ((TextView) view.findViewById(android.R.id.text1)).setText(getItem(position).getName());

            return view;
        }
    }

    private void confirmBeforeDeleteArea(final String areaId) {
        final AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance(R.string.alert_dialog_confirm_delete_area_title,
                                                                                        R.string.alert_dialog_confirm_delete_area_message);
        alertDialogFragment.setOnAlertDialogListener(new AlertDialogFragment.OnAlertDialogListener() {
            @Override
            public void onPositiveButtonClick(DialogInterface dialog) {
                // deletes this area from the current taxon
                if (BuildConfig.DEBUG) {
                    Log.d(TAG,
                          "delete area '" + areaId + "'");
                }

                ((Taxon) mInput.getCurrentSelectedTaxon()).getAreas()
                                                          .remove(areaId);
                ((Taxon) mInput.getCurrentSelectedTaxon()).setCurrentSelectedAreaId(((Taxon) mInput.getCurrentSelectedTaxon()).getLastInsertedAreaId());

                if (BuildConfig.DEBUG) {
                    Log.d(TAG,
                          "restore previously added area '" +
                                  ((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedAreaId() + "'");
                }
            }

            @Override
            public void onNegativeButtonClick(DialogInterface dialog) {
                // nothing to do ...
            }
        });
        alertDialogFragment.show(getActivity().getSupportFragmentManager(),
                                 ALERT_DIALOG_DELETE_AREA_FRAGMENT);
    }
}

package com.makina.ecrins.flora.ui.input;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.makina.ecrins.commons.BuildConfig;
import com.makina.ecrins.commons.ui.dialog.AlertDialogFragment;
import com.makina.ecrins.commons.ui.pager.IValidateWithNavigationControlFragment;
import com.makina.ecrins.flora.MainApplication;
import com.makina.ecrins.flora.R;
import com.makina.ecrins.flora.input.Taxon;


/**
 * Step 12: The user can choose an action within a list.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class ChooseActionListFragment extends ListFragment implements IValidateWithNavigationControlFragment {

    private static final String TAG = ChooseActionListFragment.class.getName();

    private static final String ALERT_DIALOG_DELETE_AREA_FRAGMENT = "alert_dialog_delete_area";

    private static final int DEFAULT_SCREEN_OFF_TIMEOUT = 15000;
    private static final String KEY_DEFAULT_SCREEN_OFF_TIMEOUT = "default_screen_off_timeout";

    private ActionItemArrayAdapter mAdapter = null;
    private Bundle mSavedState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            Log.d(TAG, "onCreate, savedInstanceState null");

            mSavedState = new Bundle();
            mSavedState.putInt(KEY_DEFAULT_SCREEN_OFF_TIMEOUT, Settings.System.getInt(getActivity()
                    .getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, DEFAULT_SCREEN_OFF_TIMEOUT));
        }
        else {
            Log.d(TAG, "onCreate, savedInstanceState initialized");

            mSavedState = savedInstanceState;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_choose_action, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getListAdapter() == null) {
            mAdapter = new ActionItemArrayAdapter(getActivity(), android.R.layout.simple_list_item_1);
            mAdapter.add(new ActionItem() {
                @Override
                public String getName() {
                    return getString(R.string.choose_action_pause);
                }

                @Override
                public void performAction() {
                    Settings.System.putInt(getActivity()
                            .getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 1000);
                }
            });
            mAdapter.add(new ActionItem() {
                @Override
                public String getName() {
                    return getString(R.string.choose_action_area);
                }

                @Override
                public void performAction() {
                    if (((MainApplication) getActivity().getApplication()).getInput()
                            .getCurrentSelectedTaxon() != null) {
                        ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                .getCurrentSelectedTaxon()).setCurrentSelectedAreaId(null);
                    }

                    ((PagerFragmentActivity) getActivity())
                            .goToPageByKey(R.string.pager_fragment_webview_ap_title);
                }
            });
            mAdapter.add(new ActionItem() {
                @Override
                public String getName() {
                    return getString(R.string.choose_action_finish);
                }

                @Override
                public void performAction() {
                    ((PagerFragmentActivity) getActivity())
                            .goToPageByKey(R.string.pager_fragment_webview_pa_title);
                }
            });

            setListAdapter(mAdapter);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");

        outState.putAll(mSavedState);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (mAdapter != null) {
            mAdapter.getItem(position).performAction();
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
        Log.d(TAG, "refreshView");

        ((ActionBarActivity) getActivity()).getSupportActionBar()
                .setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        // restore default settings
        Settings.System.putInt(getActivity()
                .getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, mSavedState
                .getInt(KEY_DEFAULT_SCREEN_OFF_TIMEOUT, DEFAULT_SCREEN_OFF_TIMEOUT));

        if ((((MainApplication) getActivity().getApplication()).getInput()
                .getCurrentSelectedTaxon() != null) &&
                (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon()).getCurrentSelectedArea() == null)) {
            // restore the previously added Area for this Taxon
            ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                    .getCurrentSelectedTaxon())
                    .setCurrentSelectedAreaId(((Taxon) ((MainApplication) getActivity()
                            .getApplication()).getInput().getCurrentSelectedTaxon())
                            .getLastInsertedAreaId());

            if (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                    .getCurrentSelectedTaxon()).getCurrentSelectedArea() == null) {
                // should never occur !
                // TODO: go back to previous and valid page in navigation history
                Log.w(TAG,
                        "refreshView : no area found for taxon " +
                                ((MainApplication) getActivity().getApplication())
                                        .getInput().getCurrentSelectedTaxon()
                                        .getId() + " !");
            }
        }
        else {
            // an Area was already added and edited for this Taxon, so perform a full consistency check to keep or not this Area.
            if (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                    .getCurrentSelectedTaxon()).getAreas()
                    .size() > (((PagerFragmentActivity) getActivity())
                    .countPagesInHistory(getResourceTitle()) + 1)) {
                confirmBeforeDeleteArea(((Taxon) ((MainApplication) getActivity().getApplication())
                        .getInput().getCurrentSelectedTaxon()).getCurrentSelectedAreaId());
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
        public String getName();

        /**
         * Performs a custom action
         */
        public void performAction();
    }

    private class ActionItemArrayAdapter extends ArrayAdapter<ActionItem> {

        private final LayoutInflater mInflater;
        private final int mResourceId;

        public ActionItemArrayAdapter(Context context, int resource) {
            super(context, resource);

            mResourceId = resource;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                view = mInflater.inflate(mResourceId, parent, false);
            }
            else {
                view = convertView;
            }

            ((TextView) view.findViewById(android.R.id.text1)).setText(getItem(position).getName());

            return view;
        }
    }

    private void confirmBeforeDeleteArea(final String areaId) {
        final DialogFragment dialogFragment = AlertDialogFragment.newInstance(
                R.string.alert_dialog_confirm_delete_area_title,
                R.string.alert_dialog_confirm_delete_area_message,
                new AlertDialogFragment.OnAlertDialogListener() {
                    @Override
                    public void onPositiveButtonListener(DialogInterface dialog) {
                        // deletes this area from the current taxon

                        if (BuildConfig.DEBUG) {
                            Log.d(
                                    TAG,
                                    "delete area '" + areaId + "'"
                            );
                        }

                        ((Taxon) ((MainApplication) getActivity().getApplication())
                                .getInput().getCurrentSelectedTaxon()).getAreas()
                                .remove(areaId);
                        ((Taxon) ((MainApplication) getActivity().getApplication())
                                .getInput().getCurrentSelectedTaxon()).setCurrentSelectedAreaId(
                                ((Taxon) ((MainApplication) getActivity().getApplication())
                                        .getInput().getCurrentSelectedTaxon()).getLastInsertedAreaId()
                        );

                        if (BuildConfig.DEBUG) {
                            Log.d(
                                    TAG,
                                    "restore previously added area '" +
                                            ((Taxon) ((MainApplication) getActivity()
                                                    .getApplication()).getInput()
                                                    .getCurrentSelectedTaxon())
                                                    .getCurrentSelectedAreaId() + "'"
                            );
                        }
                    }

                    @Override
                    public void onNegativeButtonListener(DialogInterface dialog) {
                        // nothing to do ...
                    }
                }
        );
        dialogFragment.show(
                getActivity().getSupportFragmentManager(),
                ALERT_DIALOG_DELETE_AREA_FRAGMENT);
    }
}

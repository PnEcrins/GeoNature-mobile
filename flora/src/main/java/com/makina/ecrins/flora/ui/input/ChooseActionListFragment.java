package com.makina.ecrins.flora.ui.input;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
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

import com.makina.ecrins.commons.input.AbstractInput;
import com.makina.ecrins.commons.ui.dialog.AlertDialogFragment;
import com.makina.ecrins.commons.ui.input.IInputFragment;
import com.makina.ecrins.commons.ui.input.OnInputFragmentListener;
import com.makina.ecrins.commons.ui.pager.IValidateWithNavigationControlFragment;
import com.makina.ecrins.flora.BuildConfig;
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
        implements IValidateWithNavigationControlFragment,
                   IInputFragment {

    private static final String TAG = ChooseActionListFragment.class.getName();

    private static final String ALERT_DIALOG_DELETE_AREA_FRAGMENT = "alert_dialog_delete_area";

    private OnInputFragmentListener mOnInputFragmentListener;
    private ActionItemArrayAdapter mAdapter = null;

    private Input mInput;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final FragmentActivity activity = getActivity();

        if (activity == null) {
            return;
        }

        final AlertDialogFragment alertDialogFragment = (AlertDialogFragment) activity.getSupportFragmentManager()
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
    public void onViewCreated(@NonNull View view,
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
                    mOnInputFragmentListener.onCloseApplication();
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
            mOnInputFragmentListener = (OnInputFragmentListener) context;
        }
        else {
            throw new RuntimeException(context.toString() + " must implement OnInputFragmentListener");
        }
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
        final AppCompatActivity activity = (AppCompatActivity) getActivity();

        if (activity == null) {
            return;
        }

        activity.getSupportActionBar()
                .setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        mOnInputFragmentListener.onSaveInput();

        if (mInput == null) {
            Log.w(TAG,
                  "refreshView: null input");
            return;
        }

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

    @Override
    public void setInput(@NonNull AbstractInput input) {
        this.mInput = (Input) input;
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

        ActionItemArrayAdapter(Context context,
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

            final ActionItem actionItem = getItem(position);

            if (actionItem == null) {
                return view;
            }

            ((TextView) view.findViewById(android.R.id.text1)).setText(actionItem.getName());

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

                if (mInput == null) {
                    return;
                }

                final Taxon currentSelectedTaxon = (Taxon) mInput.getCurrentSelectedTaxon();

                currentSelectedTaxon.getAreas()
                                    .remove(areaId);
                currentSelectedTaxon.setCurrentSelectedAreaId(currentSelectedTaxon.getLastInsertedAreaId());

                if (BuildConfig.DEBUG) {
                    Log.d(TAG,
                          "restore previously added area '" +
                                  currentSelectedTaxon.getCurrentSelectedAreaId() + "'");
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

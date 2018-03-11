package com.geonature.mobile.commons.ui.input.results;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.view.ActionMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.geonature.mobile.commons.R;
import com.geonature.mobile.commons.input.AbstractInput;
import com.geonature.mobile.commons.input.AbstractTaxon;
import com.geonature.mobile.commons.ui.dialog.AlertDialogFragment;
import com.geonature.mobile.commons.ui.dialog.CommentDialogFragment;
import com.geonature.mobile.commons.ui.pager.AbstractPagerFragmentActivity;
import com.geonature.mobile.commons.ui.pager.IValidateFragment;

import java.text.MessageFormat;

/**
 * Results view for the current input.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public abstract class AbstractResultsInputFragment extends Fragment
        implements
        IValidateFragment,
        OnClickListener,
        OnItemClickListener,
        OnItemLongClickListener {

    private static final String ALERT_DIALOG_DELETE_TAXON_FRAGMENT = "alert_dialog_delete_taxon";
    private static final String ALERT_DIALOG_COMMENT_FRAGMENT = "alert_dialog_comment";

    protected ResultsInputTaxaArrayAdapter mTaxaAdapter;

    private AbstractTaxon mSelectedTaxonForAction;
    private TextView mTextViewTaxaAdded;

    private final CommentDialogFragment.OnCommentDialogValidateListener mOnCommentDialogValidateListener = new CommentDialogFragment.OnCommentDialogValidateListener() {
        @Override
        public void onPositiveButtonClick(
                DialogInterface dialog,
                String message) {
            mSelectedTaxonForAction.setComment(message);
            getInput().getTaxa()
                    .put(
                            mSelectedTaxonForAction.getId(),
                            mSelectedTaxonForAction
                    );
        }

        @Override
        public void onNegativeButtonClick(DialogInterface dialog) {
            // nothing to do ...
        }
    };

    private final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            int commentStringResourceId = R.string.action_comment_add;
            int commentDrawableResourceId = R.drawable.ic_action_comment_add;

            if (mSelectedTaxonForAction != null) {
                commentStringResourceId = (TextUtils.isEmpty(mSelectedTaxonForAction.getComment())) ? R.string.action_comment_add : R.string.action_comment_edit;
                commentDrawableResourceId = (mSelectedTaxonForAction.getComment().isEmpty()) ? R.drawable.ic_action_comment_add : R.drawable.ic_action_comment;
            }

            final MenuItem menuItemAddOrEditComment = menu.add(Menu.NONE, 1, Menu.NONE, commentStringResourceId).setIcon(commentDrawableResourceId);
            menuItemAddOrEditComment.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            final MenuItem menuItemDeleteTaxon = menu.add(Menu.NONE, 0, Menu.NONE, R.string.action_delete_taxon).setIcon(R.drawable.ic_action_delete);
            menuItemDeleteTaxon.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case 0:
                    if (mSelectedTaxonForAction != null) {
                        confirmBeforeDeleteTaxon(mSelectedTaxonForAction);
                    }

                    return true;
                case 1:
                    final FragmentActivity activity = getActivity();

                    if (mSelectedTaxonForAction != null && activity != null) {
                        final CommentDialogFragment dialogFragment = CommentDialogFragment.newInstance(mSelectedTaxonForAction.getComment());
                        dialogFragment.setOnCommentDialogValidateListener(mOnCommentDialogValidateListener);
                        dialogFragment.show(
                                activity.getSupportFragmentManager(),
                                ALERT_DIALOG_COMMENT_FRAGMENT
                        );
                    }

                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // clear current selection
            mSelectedTaxonForAction = null;
            mTaxaAdapter.notifyDataSetChanged();
            mMode = null;
        }
    };

    private ActionMode mMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final FragmentActivity activity = getActivity();

        if (activity == null) {
            return;
        }

        final AlertDialogFragment alertDialogFragment = (AlertDialogFragment) activity.getSupportFragmentManager().findFragmentByTag(ALERT_DIALOG_DELETE_TAXON_FRAGMENT);

        if (alertDialogFragment != null) {
            if (mSelectedTaxonForAction == null) {
                alertDialogFragment.dismiss();
            }
        }

        // restore CommentDialogFragment state after resume if needed
        final CommentDialogFragment commentDialogFragment = (CommentDialogFragment) activity.getSupportFragmentManager().findFragmentByTag(ALERT_DIALOG_COMMENT_FRAGMENT);

        if (commentDialogFragment != null) {
            commentDialogFragment.setOnCommentDialogValidateListener(mOnCommentDialogValidateListener);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(AbstractResultsInputFragment.class.getName(), "onCreateView");

        View view = inflater.inflate(R.layout.fragment_results_input, container, false);

        // clear selection
        mSelectedTaxonForAction = null;

        mTaxaAdapter = new ResultsInputTaxaArrayAdapter(getActivity(), R.layout.list_item_selected_taxon);
        mTaxaAdapter.setSelectedTaxonId(getInput().getLastInsertedTaxonId());

        mTextViewTaxaAdded = view.findViewById(R.id.textViewTaxaAdded);
        ListView listSelectedTaxaView = view.findViewById(R.id.listSelectedTaxa);
        listSelectedTaxaView.setAdapter(mTaxaAdapter);
        listSelectedTaxaView.setOnItemClickListener(this);
        listSelectedTaxaView.setOnItemLongClickListener(this);

        view.findViewById(R.id.buttonAddTaxon).setOnClickListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        // Make sure that we are currently visible
        if (this.isVisible()) {
            if (!isVisibleToUser) {
                if (mMode != null) {
                    mMode.finish();
                }
            }
        }
    }

    @Override
    public int getResourceTitle() {
        return R.string.pager_fragment_results_input_title;
    }

    @Override
    public boolean getPagingEnabled() {
        return true;
    }

    @Override
    public boolean validate() {
        return !getInput().getTaxa().isEmpty();
    }

    @Override
    public void refreshView() {
        mTextViewTaxaAdded.setText(MessageFormat.format(getText(R.string.results_input_taxa_added).toString(), getInput().getTaxa().size()));

        mTaxaAdapter.clear();

        for (AbstractTaxon taxon : getInput().getTaxa().values()) {
            mTaxaAdapter.add(taxon);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mSelectedTaxonForAction = mTaxaAdapter.getItem(position);
        // sets this taxon as the current selected taxon for this input
        getInput().setCurrentSelectedTaxonId(mSelectedTaxonForAction.getId());

        mTaxaAdapter.setSelectedTaxonId(getInput().getCurrentSelectedTaxonId());
        mTaxaAdapter.notifyDataSetChanged();

        final FragmentActivity activity = getActivity();

        if (mMode == null && activity != null) {
            mMode = ((AbstractPagerFragmentActivity) activity).startSupportActionMode(mActionModeCallback);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, final long id) {
        confirmBeforeDeleteTaxon(mTaxaAdapter.getItem(position));

        return true;
    }

    public abstract AbstractInput getInput();

    private void confirmBeforeDeleteTaxon(final AbstractTaxon taxon) {
        final AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance(
                R.string.alert_dialog_confirm_delete_taxon_title,
                R.string.alert_dialog_confirm_delete_taxon_message);
        alertDialogFragment.setOnAlertDialogListener(
                new AlertDialogFragment.OnAlertDialogListener() {
                    @Override
                    public void onPositiveButtonClick(DialogInterface dialog) {
                        // deletes this taxon from the current input
                        getInput().getTaxa().remove(taxon.getId());

                        // clear the previous selected taxon if this taxon is the last added to this input
                        if (getInput().getCurrentSelectedTaxonId() == taxon.getId()) {
                            getInput().setCurrentSelectedTaxonId(-1);
                        }

                        // clear current selection
                        mSelectedTaxonForAction = null;

                        if (mMode != null) {
                            mMode.finish();
                        }

                        refreshView();

                        final FragmentActivity activity = getActivity();

                        if (activity != null) {
                            ((AbstractPagerFragmentActivity) activity).validateCurrentPage();
                        }
                    }

                    @Override
                    public void onNegativeButtonClick(DialogInterface dialog) {
                        // nothing to do ...
                    }
                }
        );

        final FragmentActivity activity = getActivity();

        if (activity != null) {
            alertDialogFragment.show(activity.getSupportFragmentManager(), ALERT_DIALOG_DELETE_TAXON_FRAGMENT);
        }
    }
}

package com.makina.ecrins.commons.ui.input.results;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.view.ActionMode;
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

import com.makina.ecrins.commons.R;
import com.makina.ecrins.commons.input.AbstractInput;
import com.makina.ecrins.commons.input.AbstractTaxon;
import com.makina.ecrins.commons.ui.dialog.AlertDialogFragment;
import com.makina.ecrins.commons.ui.dialog.CommentDialogFragment;
import com.makina.ecrins.commons.ui.pager.AbstractPagerFragmentActivity;
import com.makina.ecrins.commons.ui.pager.IValidateFragment;

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

    private final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            int commentStringResourceId = R.string.action_comment_add;
            int commentDrawableResourceId = R.drawable.ic_action_comment_add;

            if (mSelectedTaxonForAction != null) {
                commentStringResourceId = (mSelectedTaxonForAction.getComment().isEmpty()) ? R.string.action_comment_add : R.string.action_comment_edit;
                commentDrawableResourceId = (mSelectedTaxonForAction.getComment().isEmpty()) ? R.drawable.ic_action_comment_add : R.drawable.ic_action_comment;
            }

            final MenuItem menuItemAddOrEditComment = menu.add(Menu.NONE, 1, Menu.NONE, commentStringResourceId).setIcon(commentDrawableResourceId);
            MenuItemCompat.setShowAsAction(menuItemAddOrEditComment, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
            final MenuItem menuItemDeleteTaxon = menu.add(Menu.NONE, 0, Menu.NONE, R.string.action_delete_taxon).setIcon(R.drawable.ic_action_delete);
            MenuItemCompat.setShowAsAction(menuItemDeleteTaxon, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);

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
                    if (mSelectedTaxonForAction != null) {
                        CommentDialogFragment dialogFragment = CommentDialogFragment.newInstance(
                                mSelectedTaxonForAction.getComment(),
                                new CommentDialogFragment.OnCommentDialogValidateListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which, String message) {
                                        mSelectedTaxonForAction.setComment(message);
                                        getInput().getTaxa().put(mSelectedTaxonForAction.getId(), mSelectedTaxonForAction);
                                    }
                                }, null
                        );
                        dialogFragment.show(getActivity().getSupportFragmentManager(), ALERT_DIALOG_COMMENT_FRAGMENT);
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(AbstractResultsInputFragment.class.getName(), "onCreateView");

        View view = inflater.inflate(R.layout.fragment_results_input, container, false);

        // clear selection
        mSelectedTaxonForAction = null;

        mTaxaAdapter = new ResultsInputTaxaArrayAdapter(getActivity(), R.layout.list_item_selected_taxon);
        mTaxaAdapter.setSelectedTaxonId(getInput().getLastInsertedTaxonId());

        mTextViewTaxaAdded = (TextView) view.findViewById(R.id.textViewTaxaAdded);
        ListView listSelectedTaxaView = (ListView) view.findViewById(R.id.listSelectedTaxa);
        listSelectedTaxaView.setAdapter(mTaxaAdapter);
        listSelectedTaxaView.setOnItemClickListener(this);
        listSelectedTaxaView.setOnItemLongClickListener(this);

        view.findViewById(R.id.buttonAddTaxon).setOnClickListener(this);

        return view;
    }

    @Override
    public void onPause() {
        // FIXME: Careful we dismiss dialog, cause of error after screen rotate, we lost the information of fragment (Activity, tag)
        DialogFragment fragment = (DialogFragment) getActivity().getSupportFragmentManager().findFragmentByTag(ALERT_DIALOG_DELETE_TAXON_FRAGMENT);

        if (fragment != null) {
            fragment.dismiss();
        }

        fragment = (DialogFragment) getActivity().getSupportFragmentManager().findFragmentByTag(ALERT_DIALOG_COMMENT_FRAGMENT);

        if (fragment != null) {
            fragment.dismiss();
        }

        super.onPause();
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

        if (mMode == null) {
            mMode = ((AbstractPagerFragmentActivity) getActivity()).startSupportActionMode(mActionModeCallback);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, final long id) {
        confirmBeforeDeleteTaxon(mTaxaAdapter.getItem(position));

        return true;
    }

    public abstract AbstractInput getInput();

    private void confirmBeforeDeleteTaxon(final AbstractTaxon taxon) {
        DialogFragment dialogFragment = AlertDialogFragment.newInstance(
                R.string.alert_dialog_confirm_delete_taxon_title,
                R.string.alert_dialog_confirm_delete_taxon_message,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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

                        ((AbstractPagerFragmentActivity) getActivity()).validateCurrentPage();
                    }
                },
                null
        );

        dialogFragment.show(getActivity().getSupportFragmentManager(), ALERT_DIALOG_DELETE_TAXON_FRAGMENT);
    }
}

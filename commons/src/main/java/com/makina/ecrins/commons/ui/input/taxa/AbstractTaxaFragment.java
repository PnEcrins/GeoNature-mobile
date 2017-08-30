package com.makina.ecrins.commons.ui.input.taxa;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.makina.ecrins.commons.BuildConfig;
import com.makina.ecrins.commons.R;
import com.makina.ecrins.commons.content.MainDatabaseHelper;
import com.makina.ecrins.commons.input.AbstractInput;
import com.makina.ecrins.commons.input.AbstractTaxon;
import com.makina.ecrins.commons.ui.pager.AbstractPagerFragmentActivity;
import com.makina.ecrins.commons.ui.pager.IValidateFragment;
import com.makina.ecrins.commons.ui.widget.AlphabetSectionIndexerCursorAdapter;
import com.makina.ecrins.commons.ui.widget.PinnedSectionListView;
import com.makina.ecrins.commons.util.ThemeUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Lists all taxa from database.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 * @deprecated use {@link AbstractTaxaInputListFragment} instead
 */
@Deprecated
public abstract class AbstractTaxaFragment
        extends ListFragment
        implements IValidateFragment,
                   LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = AbstractTaxaFragment.class.getName();

    public static final int HANDLER_TAXA_FILTER_STATUS = 0;

    protected static final String KEY_SELECTED_UNITY = "selected_unity";
    protected static final String KEY_SELECTED_TAXON = "selected_taxon";
    protected static final String KEY_SWITCH_LABEL = "switch_label";
    protected static final String KEY_FILTER = "filter";

    protected static final String KEY_DISPLAY_TAXON_STATUS = "display_taxon_status";
    protected static final String KEY_DISPLAY_TAXON_HERITAGE = "display_taxon_heritage";
    protected static final String KEY_DISPLAY_TAXON_DETAILS = "display_taxon_details";

    protected AlphabetSectionIndexerCursorAdapter mAdapter;
    protected Bundle mSavedState;
    protected ViewGroup mSecondActionBarView;

    private boolean mListShown;
    private View mProgressContainer;
    private View mListContainer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {

            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "onCreate, savedInstanceState null");
            }

            mSavedState = new Bundle();
            mSavedState.putString(KEY_SELECTED_UNITY,
                                  getInput().getFeatureId());
            mSavedState.putSerializable(KEY_SWITCH_LABEL,
                                        LabelSwitcher.FRENCH);
            mSavedState.putBoolean(KEY_DISPLAY_TAXON_STATUS,
                                   false);
            mSavedState.putBoolean(KEY_DISPLAY_TAXON_HERITAGE,
                                   false);
            mSavedState.putBoolean(KEY_DISPLAY_TAXON_DETAILS,
                                   PreferenceManager.getDefaultSharedPreferences(getActivity())
                                                    .getBoolean("taxa_display_details",
                                                                true));
        }
        else {
            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "onCreate, savedInstanceState initialized");
            }

            mSavedState = savedInstanceState;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_taxa,
                                     container,
                                     false);

        mListContainer = view.findViewById(R.id.listContainer);
        mProgressContainer = view.findViewById(R.id.progressContainer);
        mSecondActionBarView = (ViewGroup) view.findViewById(R.id.secondActionBarView);

        mListShown = true;

        return view;
    }

    @Override
    public void onViewCreated(View view,
                              Bundle savedInstanceState) {
        super.onViewCreated(view,
                            savedInstanceState);

        // give some text to display if there is no data
        getListView().setEmptyView(view.findViewById(R.id.internalEmpty));

        if (getListView() instanceof PinnedSectionListView) {
            ((PinnedSectionListView) getListView()).setShadowVisible(false);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putAll(mSavedState);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "onPause");
        }

        getLoaderManager().destroyLoader(0);
        mAdapter = null;

        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu,
                                    MenuInflater inflater) {
        final MenuItem menuItemSearch = menu.add(Menu.NONE,
                                                 0,
                                                 Menu.NONE,
                                                 R.string.action_search);
        menuItemSearch.setIcon(R.drawable.ic_action_search);
        MenuItemCompat.setShowAsAction(menuItemSearch,
                                       MenuItemCompat.SHOW_AS_ACTION_ALWAYS | MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        SearchView searchView = new SearchView(((AbstractPagerFragmentActivity) getActivity()).getSupportActionBar()
                                                                                              .getThemedContext());
        searchView.setQueryHint(getString(R.string.taxa_search_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.RESULT_HIDDEN,
                                    0);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (mAdapter == null) {
                    return false;
                }

                mAdapter.getFilter()
                        .filter(!TextUtils.isEmpty(newText) ? newText : null);
                return true;
            }
        });

        MenuItemCompat.setOnActionExpandListener(menuItemSearch,
                                                 new MenuItemCompat.OnActionExpandListener() {
                                                     @Override
                                                     public boolean onMenuItemActionExpand(MenuItem item) {
                                                         return true;
                                                     }

                                                     @Override
                                                     public boolean onMenuItemActionCollapse(MenuItem item) {
                                                         if (BuildConfig.DEBUG) {
                                                             Log.d(TAG,
                                                                   "onMenuItemActionCollapse");
                                                         }

                                                         // clear the search filter on collapse
                                                         mAdapter.getFilter()
                                                                 .filter(null);
                                                         return true;
                                                     }
                                                 });

        MenuItemCompat.setActionView(menuItemSearch,
                                     searchView);

        menu.add(Menu.NONE,
                 1,
                 Menu.NONE,
                 (mSavedState.getSerializable(KEY_SWITCH_LABEL)
                             .equals(LabelSwitcher.FRENCH)) ? R.string.action_switch_label_latin : R.string.action_switch_label_french)
            .setIcon((mSavedState.getSerializable(KEY_SWITCH_LABEL)
                                 .equals(LabelSwitcher.FRENCH)) ? R.drawable.ic_action_label_switcher_la : R.drawable.ic_action_label_switcher_fr);
        MenuItemCompat.setShowAsAction(menuItemSearch,
                                       MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                LabelSwitcher labelSwitcher = (LabelSwitcher) mSavedState.getSerializable(KEY_SWITCH_LABEL);

                switch (labelSwitcher) {
                    case FRENCH:
                        mSavedState.putSerializable(KEY_SWITCH_LABEL,
                                                    LabelSwitcher.LATIN);
                        break;
                    default:
                        mSavedState.putSerializable(KEY_SWITCH_LABEL,
                                                    LabelSwitcher.FRENCH);
                        break;
                }

                ActivityCompat.invalidateOptionsMenu(getActivity());
                refreshView();

                return true;
            default:
                return false;
        }
    }

    @Override
    public void onListItemClick(ListView l,
                                View v,
                                int position,
                                long id) {
        long selectedTaxonId = mAdapter.getItemId(position);

        Cursor cursor = (Cursor) mAdapter.getItem(position);
        long taxonClassId = cursor.getLong(cursor.getColumnIndex(MainDatabaseHelper.TaxaColumns.CLASS_ID));
        int classCount = cursor.getInt(cursor.getColumnIndex(MainDatabaseHelper.TaxaColumns.NUMBER));

        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "onListItemClick: " + selectedTaxonId + ", selected class:" + taxonClassId);
        }

        // replace the previous selection by this one
        if (mSavedState.getParcelable(KEY_SELECTED_TAXON) != null) {
            getInput().getTaxa()
                      .remove(((AbstractTaxon) mSavedState.getParcelable(KEY_SELECTED_TAXON)).getId());
        }

        // creates a new taxon for this input
        AbstractTaxon selectedTaxon = createTaxon(selectedTaxonId);
        selectedTaxon.setClassId(taxonClassId);
        selectedTaxon.setClassCount(classCount);

        // apply selection to this taxon
        getInput().getTaxa()
                  .put(selectedTaxon.getId(),
                       selectedTaxon);
        mSavedState.putParcelable(KEY_SELECTED_TAXON,
                                  selectedTaxon);
        getInput().setCurrentSelectedTaxonId(selectedTaxon.getId());
        v.findViewById(R.id.textViewTaxonObservers)
         .setVisibility(View.VISIBLE);
        mAdapter.notifyDataSetChanged();

        Log.d(AbstractTaxaFragment.class.getName(),
              "number of taxa : " + getInput().getTaxa()
                                              .size());

        ((AbstractPagerFragmentActivity) getActivity()).validateCurrentPage();
    }

    @Override
    public int getResourceTitle() {
        return R.string.pager_fragment_taxa_title;
    }

    @Override
    public boolean getPagingEnabled() {
        return true;
    }

    @Override
    public boolean validate() {
        return getInput().getCurrentSelectedTaxonId() != -1;
    }

    @Override
    public void refreshView() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "refreshView");
        }

        ((AbstractPagerFragmentActivity) getActivity()).getSupportActionBar()
                                                       .setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        // clear all filters, restore previously selected taxon
        if (mSavedState.containsKey(KEY_SELECTED_TAXON) && getInput().getCurrentSelectedTaxonId() == -1) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "clear filters");
            }

            mSavedState.remove(KEY_SELECTED_TAXON);
            mSavedState.putSerializable(KEY_SWITCH_LABEL,
                                        LabelSwitcher.FRENCH);
            mSavedState.remove(KEY_FILTER);

            clearFilters();

            ActivityCompat.invalidateOptionsMenu(getActivity());
        }

        // restore previously selected taxon
        if (getInput().getTaxa()
                      .get(getInput().getCurrentSelectedTaxonId()) != null) {
            mSavedState.putParcelable(KEY_SELECTED_TAXON,
                                      getInput().getTaxa()
                                                .get(getInput().getCurrentSelectedTaxonId()));

            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "restore selected taxon: " + ((AbstractTaxon) mSavedState.getParcelable(KEY_SELECTED_TAXON)).getTaxonId());
            }
        }

        // remove 'KEY_SELECTED_UNITY' key if no feature was selected
        if (getInput().getFeatureId() == null) {
            mSavedState.remove(KEY_SELECTED_UNITY);
        }
        else {
            mSavedState.putString(KEY_SELECTED_UNITY,
                                  getInput().getFeatureId());
        }

        // prepare the loader, either re-connect with an existing one, or start a new one
        getLoaderManager().restartLoader(0,
                                         mSavedState,
                                         this);

        // start out with a progress indicator
        setListShown(false);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader,
                               Cursor data) {
        String sortedColumnIndex = MainDatabaseHelper.TaxaColumns.NAME;

        if (mSavedState.containsKey(KEY_SWITCH_LABEL)) {
            switch ((LabelSwitcher) mSavedState.getSerializable(KEY_SWITCH_LABEL)) {
                case FRENCH:
                    sortedColumnIndex = MainDatabaseHelper.TaxaColumns.NAME_FR;
                    break;
                default:
                    sortedColumnIndex = MainDatabaseHelper.TaxaColumns.NAME;
                    break;
            }
        }

        if (mAdapter == null) {
            initializeAdapter(data,
                              sortedColumnIndex);
        }
        else {
            mAdapter.setSortedColumnIndex(sortedColumnIndex);
            mAdapter.swapCursor(data);
        }

        getListView().setFastScrollEnabled(true);
        getListView().setScrollingCacheEnabled(true);

        // the list should now be shown
        if (isResumed()) {
            setListShown(true);
        }
        else {
            setListShownNoAnimation(true);
        }

        // sets the current position to the selected taxon
        if (getInput().getCurrentSelectedTaxonId() != -1) {
            getListView().setSelection(mAdapter.getItemPosition(getInput().getTaxa()
                                                                          .get(getInput().getCurrentSelectedTaxonId())
                                                                          .getTaxonId()));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // data is not available anymore, delete reference
        if (mAdapter != null) {
            mAdapter.swapCursor(null);
        }
    }

    @Override
    public void setListShown(boolean shown) {
        setListShown(shown,
                     true);
    }

    @Override
    public void setListShownNoAnimation(boolean shown) {
        setListShown(shown,
                     false);
    }

    public Bundle getSavedInstanceState() {
        return this.mSavedState;
    }

    public abstract AbstractTaxon createTaxon(long taxonId);

    public abstract AbstractInput getInput();

    public abstract void clearFilters();

    // see http://code.google.com/p/android/issues/detail?id=21742
    private void setListShown(boolean shown,
                              boolean animate) {
        if (mListShown == shown) {
            return;
        }

        mListShown = shown;

        if (shown) {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                                                                               android.R.anim.fade_out));
                mListContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                                                                           android.R.anim.fade_in));
            }

            mProgressContainer.setVisibility(View.GONE);
            mListContainer.setVisibility(View.VISIBLE);
        }
        else {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                                                                               android.R.anim.fade_in));
                mListContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                                                                           android.R.anim.fade_out));
            }

            mProgressContainer.setVisibility(View.VISIBLE);
            mListContainer.setVisibility(View.INVISIBLE);
        }
    }

    private void initializeAdapter(Cursor cursor,
                                   String sortedColumnIndex) {
        if (mAdapter == null) {
            // create the adapter we will use to display the loaded data
            mAdapter = new AlphabetSectionIndexerCursorAdapter(getActivity(),
                                                               R.layout.list_item_taxon,
                                                               R.plurals.taxa_count,
                                                               cursor,
                                                               sortedColumnIndex,
                                                               new String[] {
                                                                       MainDatabaseHelper.TaxaColumns.NAME_FR,
                                                                       MainDatabaseHelper.TaxaUnitiesColumns.COLOR,
                                                                       MainDatabaseHelper.TaxaColumns.PATRIMONIAL,
                                                                       MainDatabaseHelper.TaxaUnitiesColumns.NB_OBS,
                                                                       MainDatabaseHelper.TaxaUnitiesColumns.DATE,
                                                                       MainDatabaseHelper.TaxaColumns.MESSAGE
                                                               },
                                                               new int[] {
                                                                       R.id.textSwitcher,
                                                                       R.id.viewStatusColor,
                                                                       R.id.imageViewHeritage,
                                                                       R.id.textViewTaxonObservers,
                                                                       R.id.textViewTaxonDate,
                                                                       R.id.textViewTaxonMessage
                                                               },
                                                               0) {
                @Override
                public View getView(int position,
                                    View convertView,
                                    ViewGroup parent) {
                    View view = super.getView(position,
                                              convertView,
                                              parent);

                    if (getItemViewType(position) == TYPE_NORMAL) {
                        if ((mSavedState.getParcelable(KEY_SELECTED_TAXON) != null) && ((AbstractTaxon) mSavedState.getParcelable(KEY_SELECTED_TAXON)).getTaxonId() == getItemId(position)) {
                            view.setBackgroundColor(ThemeUtils.getAccentColor(getContext()));

                            Cursor cursor = (Cursor) getItem(position);
                            AbstractTaxon selectedTaxon = mSavedState.getParcelable(KEY_SELECTED_TAXON);

                            // saves the current name of this taxon
                            switch ((LabelSwitcher) mSavedState.getSerializable(KEY_SWITCH_LABEL)) {
                                case FRENCH:
                                    selectedTaxon.setNameEntered(cursor.getString(cursor.getColumnIndex(MainDatabaseHelper.TaxaColumns.NAME_FR)));
                                    break;
                                default:
                                    selectedTaxon.setNameEntered(cursor.getString(cursor.getColumnIndex(MainDatabaseHelper.TaxaColumns.NAME)));
                                    break;
                            }

                            // apply selection to this taxon
                            getInput().getTaxa()
                                      .put(selectedTaxon.getId(),
                                           selectedTaxon);
                            mSavedState.putParcelable(KEY_SELECTED_TAXON,
                                                      selectedTaxon);
                        }
                        else {
                            view.setBackgroundColor(Color.TRANSPARENT);
                        }
                    }

                    return view;
                }
            };

            // sets a custom ViewBinder for this adapter
            mAdapter.setViewBinder(new ViewBinder() {
                @Override
                @SuppressWarnings("deprecation")
                public boolean setViewValue(View view,
                                            Cursor cursor,
                                            int columnIndex) {
                    if (view.getId() == R.id.textSwitcher) {
                        TextSwitcher textSwitcher = (TextSwitcher) view;
                        textSwitcher.setInAnimation(AnimationUtils.loadAnimation(getActivity(),
                                                                                 android.R.anim.fade_in));
                        textSwitcher.setOutAnimation(AnimationUtils.loadAnimation(getActivity(),
                                                                                  android.R.anim.fade_out));

                        String filter = mSavedState.getString(KEY_FILTER);
                        String selectedColor = Integer.toHexString(ThemeUtils.getAccentColor(getContext()))
                                                      .substring(2);

                        String taxonDisplayName;

                        switch ((LabelSwitcher) mSavedState.getSerializable(KEY_SWITCH_LABEL)) {
                            case LATIN:
                                taxonDisplayName = cursor.getString(cursor.getColumnIndex(MainDatabaseHelper.TaxaColumns.NAME));
                                break;
                            default:
                                taxonDisplayName = cursor.getString(cursor.getColumnIndex(MainDatabaseHelper.TaxaColumns.NAME_FR));
                                break;
                        }

                        // selected taxon
                        if ((mSavedState.getParcelable(KEY_SELECTED_TAXON) != null) && ((AbstractTaxon) mSavedState.getParcelable(KEY_SELECTED_TAXON)).getTaxonId() == cursor.getLong(cursor.getColumnIndex(MainDatabaseHelper.TaxaColumns._ID))) {
                            Spanned nameFilterFormat = (filter != null) ? Html.fromHtml(taxonDisplayName.replaceAll(Pattern.compile("(?i)(" + filter + ")")
                                                                                                                           .pattern(),
                                                                                                                    "<b>$1</b>")) : SpannedString.valueOf(taxonDisplayName);
                            textSwitcher.setText(nameFilterFormat);
                        }
                        else {
                            Spanned nameFilterFormat = (filter != null) ? Html.fromHtml(taxonDisplayName.replaceAll(Pattern.compile("(?i)(" + filter + ")")
                                                                                                                           .pattern(),
                                                                                                                    "<font color=\"#" + selectedColor + "\">$1</font>")) : SpannedString.valueOf(taxonDisplayName);
                            textSwitcher.setText(nameFilterFormat);
                        }

                        return true;
                    }
                    else if (view.getId() == R.id.viewStatusColor) {
                        if (mSavedState.getBoolean(KEY_DISPLAY_TAXON_STATUS)) {
                            String colorString = cursor.getString(columnIndex);

                            if (colorString == null) {
                                view.setBackgroundColor(getResources().getColor(R.color.taxon_status_color_new));
                            }
                            else {
                                view.setBackgroundColor(Color.parseColor(colorString.toLowerCase(Locale.getDefault())));
                            }
                        }

                        return true;
                    }
                    else if (view.getId() == R.id.imageViewHeritage) {
                        if (mSavedState.getBoolean(KEY_DISPLAY_TAXON_HERITAGE)) {
                            if (Boolean.parseBoolean(cursor.getString(columnIndex))) {
                                view.setVisibility(View.VISIBLE);
                            }
                            else {
                                view.setVisibility(View.INVISIBLE);
                            }
                        }

                        return true;
                    }
                    else if (view.getId() == R.id.textViewTaxonObservers) {
                        TextView textViewTaxonObservers = (TextView) view;
                        textViewTaxonObservers.setText(Integer.toString(cursor.getInt(columnIndex)));

                        if (mSavedState.getBoolean(KEY_DISPLAY_TAXON_DETAILS) && (mSavedState.getParcelable(KEY_SELECTED_TAXON) != null) && (((AbstractTaxon) mSavedState.getParcelable(KEY_SELECTED_TAXON)).getTaxonId() == cursor.getLong(cursor.getColumnIndex(MainDatabaseHelper.TaxaColumns._ID)))) {
                            textViewTaxonObservers.setVisibility(View.VISIBLE);
                        }
                        else {
                            textViewTaxonObservers.setVisibility(View.GONE);
                        }

                        return true;
                    }
                    else if (view.getId() == R.id.textViewTaxonDate) {
                        TextView textViewTaxonDate = (TextView) view;
                        String dateString = cursor.getString(columnIndex);

                        if (!TextUtils.isEmpty(dateString)) {
                            try {
                                textViewTaxonDate.setText(DateFormat.getLongDateFormat(getActivity())
                                                                    .format((new SimpleDateFormat("yyyy/MM/dd",
                                                                                                  Locale.getDefault())).parse(dateString)));
                            }
                            catch (ParseException pe) {
                                Log.w(TAG,
                                      pe.getMessage(),
                                      pe);
                            }
                        }
                        else {
                            textViewTaxonDate.setText("");
                        }

                        if (mSavedState.getBoolean(KEY_DISPLAY_TAXON_DETAILS) && (mSavedState.getParcelable(KEY_SELECTED_TAXON) != null) && (((AbstractTaxon) mSavedState.getParcelable(KEY_SELECTED_TAXON)).getTaxonId() == cursor.getLong(cursor.getColumnIndex(MainDatabaseHelper.TaxaColumns._ID)))) {
                            textViewTaxonDate.setVisibility(View.VISIBLE);
                        }
                        else {
                            textViewTaxonDate.setVisibility(View.GONE);
                        }

                        return true;
                    }
                    else if (view.getId() == R.id.textViewTaxonMessage) {
                        TextView textViewTaxonMessage = (TextView) view;
                        String message = cursor.getString(columnIndex);

                        if ((message != null) && (!message.equalsIgnoreCase("None"))) {
                            textViewTaxonMessage.setBackgroundResource(R.drawable.ic_action_info);
                            textViewTaxonMessage.setText(message);
                            textViewTaxonMessage.setPadding((int) (32 * getResources().getDisplayMetrics().density + 0.5f),
                                                            0,
                                                            0,
                                                            0);
                        }
                        else {
                            textViewTaxonMessage.setBackgroundDrawable(null);
                            textViewTaxonMessage.setText("");
                        }

                        if (mSavedState.getBoolean(KEY_DISPLAY_TAXON_DETAILS) && (mSavedState.getParcelable(KEY_SELECTED_TAXON) != null) && (((AbstractTaxon) mSavedState.getParcelable(KEY_SELECTED_TAXON)).getTaxonId() == cursor.getLong(cursor.getColumnIndex(MainDatabaseHelper.TaxaColumns._ID)))) {
                            textViewTaxonMessage.setVisibility(View.VISIBLE);
                        }
                        else {
                            textViewTaxonMessage.setVisibility(View.GONE);
                        }

                        return true;
                    }
                    else {
                        return false;
                    }
                }
            });

            // sets a custom FilterQueryProvider for this adapter
            mAdapter.setFilterQueryProvider(new FilterQueryProvider() {
                @Override
                public Cursor runQuery(CharSequence constraint) {
                    // updates KEY_FILTER and restart the loader
                    mSavedState.putString(KEY_FILTER,
                                          (constraint != null) ? constraint.toString() : null);
                    getLoaderManager().restartLoader(0,
                                                     mSavedState,
                                                     AbstractTaxaFragment.this);

                    return mAdapter.getCursor();
                }
            });

            setListAdapter(mAdapter);
        }
    }

    @Deprecated
    protected static enum LabelSwitcher {
        LATIN, FRENCH
    }
}

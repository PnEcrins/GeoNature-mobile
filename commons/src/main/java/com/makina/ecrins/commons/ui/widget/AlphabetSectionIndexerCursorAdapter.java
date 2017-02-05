package com.makina.ecrins.commons.ui.widget;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.makina.ecrins.commons.BuildConfig;
import com.makina.ecrins.commons.R;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * {@code SimpleCursorAdapter} custom implementation that use an {@code AlphabetIndexer} widget to
 * keep track of the section indices.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class AlphabetSectionIndexerCursorAdapter
        extends SimpleCursorAdapter
        implements SectionIndexer,
                   PinnedSectionListView.PinnedSectionListAdapter {

    private static final String TAG = AlphabetSectionIndexerCursorAdapter.class.getName();

    protected static final int TYPE_NORMAL = 0;
    private static final int TYPE_HEADER = 1;

    private static final String ALPHABET = " ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int TYPE_COUNT = 2;

    private final Context mContext;
    private final LayoutInflater mInflater;

    private int mResourceLabelCount;

    private AlphabetIndexer mIndexer;
    private String mSortedColumnIndex;
    private long mSelectedItem;
    private int mSelectedItemPosition;

    private int[] usedSectionNumbers;
    private final SparseIntArray sectionToOffset;
    private final Map<Integer, Integer> sectionToPosition;

    public AlphabetSectionIndexerCursorAdapter(Context context,
                                               int layout,
                                               int resourceLabelCount,
                                               Cursor c,
                                               String sortedColumnIndex,
                                               String[] from,
                                               int[] to,
                                               int flags) {
        super(context,
              layout,
              c,
              from,
              to,
              flags);

        this.mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        this.mResourceLabelCount = resourceLabelCount;
        this.mSortedColumnIndex = sortedColumnIndex;
        this.mSelectedItem = -1;
        this.mSelectedItemPosition = -1;

        //use a TreeMap because we are going to iterate over its keys in sorted order
        sectionToPosition = new TreeMap<>();
        sectionToOffset = new SparseIntArray();

        if (c != null) {
            initializeSectionsIndexer(c);
        }
    }

    @Override
    public int getCount() {
        if (super.getCount() != 0) {
            // sometimes your data set gets invalidated.
            // In this case getCount() should return 0 and not our adjusted count for the headers.
            // The only way to know if data is invalidated is to check if super.getCount() is 0.
            return super.getCount() + usedSectionNumbers.length;
        }

        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (getItemViewType(position) == TYPE_NORMAL) {
            // if the list item is not a header, then we fetch the data set item with the same position
            // off-setted by the number of headers that appear before the item in the list
            return super.getItem(position - sectionToOffset.get(getSectionForPosition(position)) - 1);
        }

        return null;
    }

    @Override
    public long getItemId(int position) {
        if (getItemViewType(position) == TYPE_NORMAL) {
            return super.getItemId(position - sectionToOffset.get(getSectionForPosition(position)) - 1);
        }
        else {
            return -1;
        }
    }

    @Override
    public Cursor swapCursor(Cursor c) {
        Cursor oldCursor = super.swapCursor(c);

        if (c != null) {
            initializeSectionsIndexer(c);
        }

        return oldCursor;
    }

    @Override
    public void changeCursor(Cursor cursor) {
        super.changeCursor(cursor);

        if (cursor != null) {
            initializeSectionsIndexer(cursor);
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        // nothing much to this: headers have positions that the sectionIndexer manages.
        if (position == getPositionForSection(getSectionForPosition(position))) {
            return TYPE_HEADER;
        }

        return TYPE_NORMAL;
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_COUNT;
    }

    // return the header view, if it's in a section header position
    @Override
    public View getView(int position,
                        View convertView,
                        ViewGroup parent) {
        final int type = getItemViewType(position);

        if (type == TYPE_HEADER) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_section_header,
                                                parent,
                                                false);
            }

            ((TextView) convertView.findViewById(android.R.id.text1)).setText((String) getSections()[getSectionForPosition(position)]);

            return convertView;
        }

        return super.getView(position - sectionToOffset.get(getSectionForPosition(position)) - 1,
                             convertView,
                             parent);
    }

    //these two methods just disable the headers
    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) != TYPE_HEADER;

    }

    @Override
    public int getPositionForSection(int section) {
        if (sectionToOffset.get(section,
                                Integer.MIN_VALUE) == Integer.MIN_VALUE) {
            // This is only the case when the FastScroller is scrolling, and so this section doesn't appear in our data set.
            // The implementation of Fastscroller requires that missing sections have the same index as the beginning of the next non-missing section
            // (or the end of the the list if if the rest of the sections are missing).
            int i = 0;
            int maxLength = usedSectionNumbers.length;

            // linear scan over the sections (constant number of these) that appear in the data set to find the first used section that is greater than the given section
            while (i < maxLength && section > usedSectionNumbers[i]) {
                i++;
            }

            // the given section is past all our data
            if (i == maxLength) {
                return getCount();
            }

            if (mIndexer == null) {
                return 0;
            }

            return mIndexer.getPositionForSection(usedSectionNumbers[i]) + sectionToOffset.get(usedSectionNumbers[i]);
        }

        if (mIndexer == null) {
            return 0;
        }

        return mIndexer.getPositionForSection(section) + sectionToOffset.get(section);
    }

    @Override
    public int getSectionForPosition(int position) {
        int i = 0;
        int maxLength = usedSectionNumbers.length;

        // linear scan over the used alphabetical sections' positions to find where the given section fits in
        while (i < maxLength && position >= sectionToPosition.get(usedSectionNumbers[i])) {
            i++;
        }

        return usedSectionNumbers[i - 1];
    }

    @Override
    public Object[] getSections() {
        if (mIndexer == null) {
            return Collections.EMPTY_LIST.toArray();
        }

        return mIndexer.getSections();
    }

    @Override
    public boolean isItemViewTypePinned(int viewType) {
        return viewType == TYPE_HEADER;
    }

    @Override
    public void onSectionViewPinned(@NonNull View view) {
        int itemsCount = getCursor().getCount();
        ((TextView) view.findViewById(android.R.id.text2)).setText(this.mContext.getResources()
                                                                                .getQuantityString(this.mResourceLabelCount,
                                                                                                   itemsCount,
                                                                                                   itemsCount));
    }

    public void setSortedColumnIndex(String pSortedColumnIndex) {
        this.mSortedColumnIndex = pSortedColumnIndex;
    }

    /**
     * Gets the current position for a given item
     *
     * @param itemId item to lookup
     *
     * @return position of this item
     */
    public int getItemPosition(final long itemId) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "getItemPosition: mSelectedItemPosition = " + this.mSelectedItemPosition + ", itemId = " + itemId);
        }

        if ((this.mSelectedItemPosition == -1) || (itemId != this.mSelectedItem)) {
            final int count = getCount();
            int i = 0;

            while ((this.mSelectedItemPosition == -1) && (i < count)) {
                long currentItemId = getItemId(i);

                if (currentItemId == itemId) {
                    this.mSelectedItemPosition = i;
                }

                i++;
            }

            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "getItemPosition : mSelectedItemPosition = " + this.mSelectedItemPosition);
            }

            if (this.mSelectedItemPosition != -1) {
                this.mSelectedItem = itemId;
            }
        }

        return this.mSelectedItemPosition;
    }

    private void initializeSectionsIndexer(@NonNull final Cursor cursor) {

        mIndexer = new AlphabetIndexer(cursor,
                                       cursor.getColumnIndexOrThrow(this.mSortedColumnIndex),
                                       ALPHABET);

        sectionToPosition.clear();
        sectionToOffset.clear();
        mSelectedItemPosition = -1;

        final int count = super.getCount();
        int i;

        // temporarily have a map alphabet section to first index it appears
        // (this map is going to be doing something else later)
        for (i = count - 1; i >= 0; i--) {
            sectionToPosition.put(mIndexer.getSectionForPosition(i),
                                  i);
        }

        i = 0;
        usedSectionNumbers = new int[sectionToPosition.keySet()
                                                      .size()];

        // note that for each section that appears before a position,
        // we must offset our indices by 1,
        // to make room for an alphabetical header in our list
        for (Integer section : sectionToPosition.keySet()) {
            sectionToOffset.put(section,
                                i);
            usedSectionNumbers[i] = section;
            i++;
        }

        // use offset to map the alphabet sections to their actual indices in the list
        for (Integer section : sectionToPosition.keySet()) {
            sectionToPosition.put(section,
                                  sectionToPosition.get(section) + sectionToOffset.get(section));
        }
    }
}

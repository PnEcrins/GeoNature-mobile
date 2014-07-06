package com.makina.ecrins.commons.ui.widget;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.SimpleCursorTreeAdapter;

import java.io.Serializable;

/**
 * Simple groups adapter used for {@link ExpandableListView}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public abstract class AbstractGroupsCursorAdapter<K extends Serializable> extends SimpleCursorTreeAdapter {

    public static final String KEY_SELECTED_GROUP_ID = "selected_group_id";

    private boolean mIsExpandAllGroups;

    public AbstractGroupsCursorAdapter(Context context, int groupLayout, String[] groupFrom, int[] groupTo, int childLayout, String[] childFrom, int[] childTo) {
        super(context, null, groupLayout, groupFrom, groupTo, childLayout, childFrom, childTo);

        mIsExpandAllGroups = false;
    }

    @Override
    protected Cursor getChildrenCursor(Cursor groupCursor) {
        // given the group, we (re)load the loader for all the children within that group

        final K groupId = getGroupId(groupCursor);
        final int groupPosition = groupCursor.getPosition();

        Log.d(getClass().getName(), "getChildrenCursor " + groupId + ":" + groupPosition);

        final Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_SELECTED_GROUP_ID, groupId);

        getLoaderManager().restartLoader(groupPosition, bundle, getLoaderCallbacks());

        return null;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View view = super.getGroupView(groupPosition, isExpanded, convertView, parent);

        if (mIsExpandAllGroups) {
            ((ExpandableListView) parent).expandGroup(groupPosition);
        }

        return view;
    }

    public void setExpendAllGroups(boolean expanded) {
        mIsExpandAllGroups = expanded;
    }

    /**
     * Returns the LoaderManager for this fragment, creating it if needed.
     *
     * @return the LoaderManager instance
     */
    protected abstract LoaderManager getLoaderManager();

    protected abstract LoaderCallbacks<Cursor> getLoaderCallbacks();

    protected abstract K getGroupId(Cursor groupCursor);
}

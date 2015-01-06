package com.makina.ecrins.app.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.makina.ecrins.app.R;
import com.makina.ecrins.app.ui.adapter.StringResourcesArrayAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@code Fragment} representing a list of menu items.
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class MenuListFragment
        extends Fragment
        implements AbsListView.OnItemClickListener {

    private static final String ARG_MENU_ITEMS = "ARG_MENU_ITEMS";

    private OnFragmentInteractionListener mListener;

    /**
     * The Adapter which will be used to populate the ListView with Views.
     */
    private StringResourcesArrayAdapter mAdapter;

    public static MenuListFragment newInstance(final List<Integer> stringResources) {
        final MenuListFragment fragment = new MenuListFragment();
        final Bundle args = new Bundle();
        args.putIntegerArrayList(
                ARG_MENU_ITEMS,
                new ArrayList<>(stringResources)
        );
        fragment.setArguments(args);

        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MenuListFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new StringResourcesArrayAdapter(getActivity());
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_item_list,
                container,
                false
        );

        final ListView listView = (ListView) view.findViewById(android.R.id.list);
        listView.setEmptyView(view.findViewById(android.R.id.empty));
        listView.setAdapter(mAdapter);

        // set OnItemClickListener so we can be notified on item clicks
        listView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onViewCreated(
            View view,
            @Nullable Bundle savedInstanceState) {
        super.onViewCreated(
                view,
                savedInstanceState
        );

        if (getArguments() != null) {
            mAdapter.clear();

            final List<Integer> stringResources = getArguments().getIntegerArrayList(ARG_MENU_ITEMS);

            for (Integer stringResource : stringResources) {
                mAdapter.add(stringResource);
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (OnFragmentInteractionListener) activity;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(
                    activity.toString() + " must implement OnFragmentInteractionListener"
            );
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onItemClick(
            AdapterView<?> parent,
            View view,
            int position,
            long id) {
        if (null != mListener) {
            // notify the active callbacks interface (the activity,
            // if the fragment is attached to one) that an item has been selected.
            mListener.onMenuItemClick(mAdapter.getItem(position));
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {

        public void onMenuItemClick(int stringResource);
    }
}

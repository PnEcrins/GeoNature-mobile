package com.makina.ecrins.app.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.makina.ecrins.app.R;
import com.makina.ecrins.app.ui.fragment.DialogListFragment;
import com.makina.ecrins.app.ui.fragment.MenuListFragment;

import java.util.Arrays;

/**
 * Main {@code Activity} of this application.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class MainActivity
        extends ActionBarActivity
        implements MenuListFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(
                            R.id.container,
                            MenuListFragment.newInstance(Arrays.asList(R.string.menu_item_dialogs))
                    )
                    .commit();
        }
    }

    @Override
    public void onMenuItemClick(int stringResource) {
        switch (stringResource) {
            case R.string.menu_item_dialogs:
                getSupportFragmentManager().beginTransaction()
                        // apply slide in / slide out animation effect between fragments
                        .setCustomAnimations(
                                R.anim.fragment_enter_slide_left,
                                R.anim.fragment_exit_slide_left,
                                R.anim.fragment_pop_enter_slide_right,
                                R.anim.fragment_pop_exit_slide_right
                        )
                        .replace(
                                R.id.container,
                                DialogListFragment.newInstance(),
                                DialogListFragment.class.getName()
                        )
                        .addToBackStack(null)
                        .commit();
                break;
        }
    }
}

package com.makina.ecrins.commons.ui.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Home screen {@code Activity}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 * @see HomeFragment
 */
public abstract class AbstractHomeActivity
        extends AppCompatActivity
        implements HomeFragment.OnHomeFragmentListener {

    private static final String TAG = AbstractHomeActivity.class.getName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            setCloseApplication(false);
        }

        // Display the fragment as the main content.
        getSupportFragmentManager().beginTransaction()
                                   .replace(android.R.id.content,
                                            HomeFragment.newInstance())
                                   .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isCloseApplication()) {
            Log.i(TAG,
                  "onResume: request closing app");

            finish();
        }
    }

    protected abstract boolean isCloseApplication();

    protected abstract void setCloseApplication(boolean closeApplication);
}

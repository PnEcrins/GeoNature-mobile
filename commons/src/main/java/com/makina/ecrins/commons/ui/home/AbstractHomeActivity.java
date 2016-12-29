package com.makina.ecrins.commons.ui.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Home screen {@code Activity}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 * @see HomeFragment
 */
public abstract class AbstractHomeActivity
        extends AppCompatActivity
        implements HomeFragment.OnHomeFragmentListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getSupportFragmentManager().beginTransaction()
                                   .replace(android.R.id.content,
                                            HomeFragment.newInstance())
                                   .commit();
    }
}

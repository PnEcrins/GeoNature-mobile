package com.makina.ecrins.maps.control;

import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;

import com.makina.ecrins.maps.IWebViewFragment;

/**
 * Represents a UI element to add control to the map.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public interface IControl {

    /**
     * The {@link IControl} name.
     * <p>
     * this name is used to expose this {@link IControl} instance in JavaScript.
     * </p>
     *
     * @return the {@link IControl} name.
     *
     * @see {@link WebView#addJavascriptInterface(Object, String)}
     */
    String getName();

    /**
     * Gets the {@link View} used for this {@link IControl} instance.
     *
     * @param forceCreate flag to indicate if the view should be recreated
     *
     * @return the {@link View} used for this {@link IControl} instance.
     */
    View getView(boolean forceCreate);

    /**
     * Optional method to refresh this {@link IControl} instance.
     */
    void refresh();

    /**
     * Report that this {@link IControl} would like to participate in populating the options menu by receiving a call to {@link #onCreateOptionsMenu(Menu, MenuInflater)} and related methods.
     *
     * @return <code>true</code> if this {@link IControl#getView(boolean)} is used through a {@link Menu}
     */
    boolean hasOptionsMenu();

    /**
     * Initialize the contents of the Activity's standard options menu.
     *
     * @param menu     the options menu in which you place your items
     * @param inflater the MenuInflater object that can be used to inflate any views in the menu
     *
     * @see Fragment#onCreateOptionsMenu(android.view.Menu, android.view.MenuInflater)
     */
    void onCreateOptionsMenu(Menu menu,
                             MenuInflater inflater);

    /**
     * Prepare the Screen's standard options menu to be displayed. This is called right before the menu is shown, every time it is shown.
     *
     * @param menu the options menu as last shown or first initialized by {@link #onCreateOptionsMenu(Menu, MenuInflater)}
     *
     * @see Fragment#onPrepareOptionsMenu(android.view.Menu)
     */
    void onPrepareOptionsMenu(Menu menu);

    /**
     * This hook is called whenever an item in options menu is selected.
     *
     * @param item the menu item that was selected.
     *
     * @return returns <code>false</code> to allow normal menu processing to proceed, <code>true</code> to consume it here.
     *
     * @see Fragment#onOptionsItemSelected(android.view.MenuItem)
     */
    boolean onOptionsItemSelected(MenuItem item);

    /**
     * Uses the given {@link IWebViewFragment} to initialize the JavaScript part of this {@link IControl}.
     *
     * @param webViewFragment {@link IWebViewFragment} instance used by this {@link IControl}
     *
     * @see {@link IWebViewFragment#loadUrl(String)}
     */
    void add(IWebViewFragment webViewFragment);

    /**
     * Uses the given {@link IWebViewFragment} to unregister the JavaScript part of this {@link IControl}.
     *
     * @param webViewFragment {@link IWebViewFragment} instance used by this {@link IControl}
     *
     * @see {@link IWebViewFragment#loadUrl(String)}
     */
    void remove(IWebViewFragment webViewFragment);
}

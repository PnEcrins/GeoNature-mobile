package com.makina.ecrins.maps;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.makina.ecrins.maps.content.ITilesLayerDataSource;
import com.makina.ecrins.maps.control.IControl;
import com.makina.ecrins.maps.geojson.Feature;
import com.makina.ecrins.maps.geojson.FeatureCollection;
import com.makina.ecrins.maps.location.Geolocation;
import com.makina.ecrins.maps.location.MockLocationProvider;

import java.util.List;

/**
 * Defines a WebView Fragment embedding a {@link WebView} with Leaflet.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public interface IWebViewFragment {

    /**
     * Gets the current context.
     *
     * @return the context
     */
    public Context getContext();

    /**
     * Whether this fragment's UI is currently visible to the user.
     *
     * @return <code>true</code> if the fragment is currently visible to the user.
     */
    public boolean isMapVisibleToUser();

    /**
     * Retrieve a reference to this activity's ActionBar.
     *
     * @return the Activity's ActionBar, or <code>null</code> if it does not have one
     * @see android.support.v7.app.ActionBarActivity#getSupportActionBar()
     */
    public ActionBar getActionBar();

    /**
     * Gets the bundle used to save this instance state.
     *
     * @return the bundle in which to place the saved state
     */
    public Bundle getSavedInstanceState();

    /**
     * Gets the current {@link MapSettings} used to configure the map.
     *
     * @return the {@link MapSettings} used for this map.
     */
    public MapSettings getMapSettings();

    /**
     * Updates the current {@link MapSettings}.
     *
     * @param mapSettings the {@link MapSettings} to update
     */
    public void setMapSettings(MapSettings mapSettings);

    /**
     * Gets all registered {@link ITilesLayerDataSource} name instances as {@link List}.
     *
     * @return a list of {@link LayerSettings#getName()}
     */
    public List<String> getTilesLayersDataSources();

    /**
     * Gets {@link ITilesLayerDataSource} instance for a given name.
     *
     * @param name the name used by {@link ITilesLayerDataSource} instance to retrieve
     * @return {@link ITilesLayerDataSource} instance for this name
     */
    @Nullable
    public ITilesLayerDataSource getTilesLayersDataSource(String name);

    /**
     * Gets the current selected layer.
     *
     * @return the current layer as {@link LayerSettings}
     */
    public LayerSettings getSelectedLayer();

    /**
     * Updates the selected layer by the given one.
     *
     * @param layerSettings the {@link LayerSettings} to use as selected layer
     */
    public void setSelectedLayer(LayerSettings layerSettings);

    /**
     * Loads the given URL.
     *
     * @param url the URL of the resource to load
     * @see WebView#loadUrl(String)
     */
    public void loadUrl(String url);

    /**
     * Reloads the current view (i.e. reload entirely the map).
     */
    public void reload();

    /**
     * Gets the current position.
     *
     * @return the current position or <code>null</code> if not determined
     */
    public Location getCurrentLocation();

    /**
     * Updates the current position.
     *
     * @param location the current position to update
     */
    public void setCurrentLocation(Location location);

    /**
     * Adds the given {@link IControl} to the map.
     * <p>
     * If the given parent is null, this {@link IControl} will not be attached to a parent view.
     * </p>
     *
     * @param control {@link IControl} instance to add
     * @param parent  the {@link ViewGroup} to use for adding {@link IControl#getView(boolean)}
     */
    public void addControl(IControl control, ViewGroup parent);

    /**
     * Removes the given {@link IControl} from the map.
     *
     * @param control {@link IControl} instance to remove
     */
    public void removeControl(IControl control);

    /**
     * Returns a {@link List} of registered {@link IControl}.
     *
     * @return a {@link List} of {@link IControl#getName()}
     */
    public List<String> getControls();

    /**
     * Returns the {@link IControl} instance registered for a given name.
     *
     * @param name the name used by the registered {@link IControl}
     * @return the {@link IControl} instance for this name
     */
    public IControl getControl(String name);

    /**
     * Returns <code>true</code> if this {@link IControl#getName()} is registered or not.
     *
     * @param name the name of the registered {@link IControl} to find
     * @return <code>true</code> if this {@link IControl#getName()} is registered or not, <code>false</code> otherwise
     */
    public boolean hasControl(String name);

    /**
     * Declare that the options menu has changed, so should be recreated.
     *
     * @see Activity#invalidateOptionsMenu()
     */
    public void invalidateMenu();

    /**
     * Returns a <code>List</code> of {@link Feature}.
     *
     * @return <code>List</code> of {@link Feature}.
     */
    public List<Feature> getFeatures();

    /**
     * Updates the selected {@link Feature} for a given location.
     * <p>
     * Override this method to perform a custom action when a given {@link Feature} was selected.
     * </p>
     *
     * @param geolocation     the current {@link Geolocation}
     * @param selectedFeature the selected {@link Feature} to update (may be <code>null</code>)
     */
    public void setSelectedFeature(Geolocation geolocation, Feature selectedFeature);

    /**
     * Gets all editable {@link Feature}s as {@link FeatureCollection}.
     *
     * @return {@link FeatureCollection}
     */
    public FeatureCollection getEditableFeatures();

    /**
     * Gets the current {@link Feature} currently edited.
     *
     * @return the selected {@link Feature} edited or <code>null</code> if none.
     */
    public Feature getCurrentEditableFeature();

    /**
     * Sets the current {@link Feature} currently edited.
     *
     * @param selectedFeature the selected {@link Feature} edited (may be <code>null</code>).
     */
    public void setCurrentEditableFeature(Feature selectedFeature);

    /**
     * Updates the selected {@link Feature}.
     * <p>
     * Override this method to perform a custom action when a given {@link Feature} was selected.
     * </p>
     *
     * @param selectedFeature the selected {@link Feature} to update
     * @return <code>true</code> if the given {@link Feature} was successfully updated or added, <code>false</code> otherwise
     */
    public boolean addOrUpdateEditableFeature(Feature selectedFeature);

    /**
     * Delete a previously added {@link Feature}.
     * <p>
     * Override this method to perform a custom action when a given {@link Feature} was deleted.
     * </p>
     *
     * @param featureId the {@link Feature#getId()} used to identify the {@link Feature} to delete
     * @return <code>true</code> if the given {@link Feature} was successfully deleted <code>false</code> otherwise
     */
    public boolean deleteEditableFeature(String featureId);

    /**
     * @see LocationManager#requestLocationUpdates(String, long, float, android.location.LocationListener)
     */
    public void requestLocationUpdates(LocationListener listener);

    public MockLocationProvider getMockLocationProvider();

    public String getLocalizedMessage(String messageId);
}

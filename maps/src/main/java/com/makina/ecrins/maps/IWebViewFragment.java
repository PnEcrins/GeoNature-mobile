package com.makina.ecrins.maps;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.makina.ecrins.maps.content.ITilesLayerDataSource;
import com.makina.ecrins.maps.control.IControl;
import com.makina.ecrins.maps.jts.geojson.Feature;
import com.makina.ecrins.maps.jts.geojson.FeatureCollection;
import com.makina.ecrins.maps.location.Geolocation;
import com.makina.ecrins.maps.location.MockLocationProvider;
import com.makina.ecrins.maps.settings.LayerSettings;
import com.makina.ecrins.maps.settings.MapSettings;

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
    Context getContext();

    /**
     * Whether this fragment's UI is currently visible to the user.
     *
     * @return <code>true</code> if the fragment is currently visible to the user.
     */
    boolean isMapVisibleToUser();

    /**
     * Retrieve a reference to this activity's ActionBar.
     *
     * @return the Activity's ActionBar, or <code>null</code> if it does not have one
     *
     * @see android.support.v7.app.AppCompatActivity#getSupportActionBar()
     */
    ActionBar getActionBar();

    /**
     * Gets the bundle used to save this instance state.
     *
     * @return the bundle in which to place the saved state
     */
    Bundle getSavedInstanceState();

    /**
     * Gets the current {@link MapSettings} used to configure the map.
     *
     * @return the {@link MapSettings} used for this map.
     */
    MapSettings getMapSettings();

    /**
     * Updates the current {@link MapSettings}.
     *
     * @param mapSettings the {@link MapSettings} to update
     */
    void setMapSettings(MapSettings mapSettings);

    /**
     * Gets all registered {@link ITilesLayerDataSource} name instances as {@link List}.
     *
     * @return a list of {@link LayerSettings#getName()}
     */
    List<String> getTilesLayersDataSources();

    /**
     * Gets {@link ITilesLayerDataSource} instance for a given name.
     *
     * @param name the name used by {@link ITilesLayerDataSource} instance to retrieve
     *
     * @return {@link ITilesLayerDataSource} instance for this name
     */
    @Nullable
    ITilesLayerDataSource getTilesLayersDataSource(String name);

    /**
     * Gets the current selected layer.
     *
     * @return the current layer as {@link LayerSettings}
     */
    LayerSettings getSelectedLayer();

    /**
     * Updates the selected layer by the given one.
     *
     * @param layerSettings the {@link LayerSettings} to use as selected layer
     */
    void setSelectedLayer(LayerSettings layerSettings);

    /**
     * Loads the given URL.
     *
     * @param url the URL of the resource to load
     *
     * @see WebView#loadUrl(String)
     */
    void loadUrl(String url);

    /**
     * Reloads the current view (i.e. reload entirely the map).
     */
    void reload();

    /**
     * Gets the current position.
     *
     * @return the current position or <code>null</code> if not determined
     */
    Location getCurrentLocation();

    /**
     * Updates the current position.
     *
     * @param location the current position to update
     */
    void setCurrentLocation(Location location);

    /**
     * Adds the given {@link IControl} to the map.
     * <p>
     * If the given parent is null, this {@link IControl} will not be attached to a parent view.
     * </p>
     *
     * @param control {@link IControl} instance to add
     * @param parent  the {@link ViewGroup} to use for adding {@link IControl#getView(boolean)}
     */
    void addControl(IControl control,
                    ViewGroup parent);

    /**
     * Removes the given {@link IControl} from the map.
     *
     * @param control {@link IControl} instance to remove
     */
    void removeControl(IControl control);

    /**
     * Returns a {@link List} of registered {@link IControl}.
     *
     * @return a {@link List} of {@link IControl#getName()}
     */
    List<String> getControls();

    /**
     * Returns the {@link IControl} instance registered for a given name.
     *
     * @param name the name used by the registered {@link IControl}
     *
     * @return the {@link IControl} instance for this name
     */
    IControl getControl(String name);

    /**
     * Returns <code>true</code> if this {@link IControl#getName()} is registered or not.
     *
     * @param name the name of the registered {@link IControl} to find
     *
     * @return <code>true</code> if this {@link IControl#getName()} is registered or not, <code>false</code> otherwise
     */
    boolean hasControl(String name);

    /**
     * Declare that the options menu has changed, so should be recreated.
     *
     * @see Activity#invalidateOptionsMenu()
     */
    void invalidateMenu();

    /**
     * Returns a {@code List} of {@link Feature}.
     *
     * @return a {@code List} of {@link Feature}.
     */
    @NonNull
    List<Feature> getFeatures();

    /**
     * Updates the selected {@link Feature} for a given location.
     * <p>
     * Override this method to perform a custom action when a given {@link Feature} was selected.
     * </p>
     *
     * @param geolocation     the current {@link Geolocation}
     * @param selectedFeature the selected {@link Feature} to update (may be <code>null</code>)
     */
    void setSelectedFeature(Geolocation geolocation,
                            Feature selectedFeature);

    /**
     * Gets all editable {@link Feature}s as {@link FeatureCollection}.
     *
     * @return {@link FeatureCollection}
     */
    FeatureCollection getEditableFeatures();

    /**
     * Gets the current {@link Feature} currently edited.
     *
     * @return the selected {@link Feature} edited or <code>null</code> if none.
     */
    @Nullable
    Feature getCurrentEditableFeature();

    /**
     * Sets the current {@link Feature} currently edited.
     *
     * @param selectedFeature the selected {@link Feature} edited (may be {@code null}).
     */
    void setCurrentEditableFeature(@Nullable final Feature selectedFeature);

    /**
     * Updates the selected {@link Feature}.
     * <p>
     * Override this method to perform a custom action when a given {@link Feature} was selected.
     * </p>
     *
     * @param selectedFeature the selected {@link Feature} to update
     *
     * @return {@code true} if the given {@link Feature} was successfully updated or added, {@code false} otherwise
     */
    boolean addOrUpdateEditableFeature(@NonNull final Feature selectedFeature);

    /**
     * Delete a previously added {@link Feature}.
     * <p>
     * Override this method to perform a custom action when a given {@link Feature} was deleted.
     * </p>
     *
     * @param featureId the {@link Feature#getId()} used to identify the {@link Feature} to delete
     *
     * @return <code>true</code> if the given {@link Feature} was successfully deleted <code>false</code> otherwise
     */
    boolean deleteEditableFeature(String featureId);

    /**
     * @see LocationManager#requestLocationUpdates(String, long, float, android.location.LocationListener)
     */
    void requestLocationUpdates(LocationListener listener);

    MockLocationProvider getMockLocationProvider();

    String getLocalizedMessage(String messageId);
}

package com.mapbox.mapboxandroiddemo.examples.plugins;

// #-code-snippet: location-plugin-options-activity full-java

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerOptions;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.OnLocationLayerClickListener;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;

import java.util.List;

/**
 * Use the LocationLayerOptions class to customize the Location Layer Plugin's
 * device location icon.
 */
public class LocationOptionsActivity extends AppCompatActivity implements
    OnMapReadyCallback, PermissionsListener, OnLocationLayerClickListener, LocationEngineListener {

  private PermissionsManager permissionsManager;
  private MapboxMap mapboxMap;
  private MapView mapView;
  private LocationEngine locationEngine;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_location_plugin_options);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {
    LocationOptionsActivity.this.mapboxMap = mapboxMap;
    enableLocationPlugin();
  }

  @SuppressWarnings({"MissingPermission"})
  private void enableLocationPlugin() {
    // Check if permissions are enabled and if not request
    if (PermissionsManager.areLocationPermissionsGranted(this)) {

      // Set up the location engine
      initializeLocationEngine();

      // Create and customize the plugin's options
      LocationLayerOptions locationLayerOptions = LocationLayerOptions.builder(this)
          .elevation(5)
          .accuracyAlpha(.6f)
          .accuracyColor(Color.RED)
          .foregroundDrawable(R.drawable.android_custom_location_icon)
          .build();

      // Create the plugin
      LocationLayerPlugin locationLayerPlugin = new LocationLayerPlugin(
          mapView, mapboxMap, locationEngine, locationLayerOptions);

      // Set the plugin's camera and render modes
      locationLayerPlugin.setCameraMode(CameraMode.NONE);
      locationLayerPlugin.setRenderMode(RenderMode.NORMAL);

      // Add the location icon click listener
      locationLayerPlugin.addOnLocationClickListener(this);

      getLifecycle().addObserver(locationLayerPlugin);
    } else {
      permissionsManager = new PermissionsManager(this);
      permissionsManager.requestLocationPermissions(this);
    }
  }


  @Override
  public void onLocationLayerClick() {
    Toast.makeText(this, "OnLocationLayerClick", Toast.LENGTH_LONG).show();
  }

  private void initializeLocationEngine() {
    locationEngine = new LocationEngineProvider(this).obtainBestLocationEngineAvailable();
    locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
    locationEngine.setFastestInterval(1000);
    locationEngine.addLocationEngineListener(this);
    locationEngine.activate();
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  @Override
  public void onExplanationNeeded(List<String> permissionsToExplain) {
    Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
  }

  @Override
  public void onPermissionResult(boolean granted) {
    if (granted) {
      enableLocationPlugin();
    } else {
      Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
      finish();
    }
  }

  @SuppressWarnings({"MissingPermission"})
  protected void onStart() {
    super.onStart();
    mapView.onStart();
    if (locationEngine != null) {
      locationEngine.requestLocationUpdates();
      locationEngine.addLocationEngineListener(this);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    mapView.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
    mapView.onPause();
  }

  @Override
  protected void onStop() {
    super.onStop();
    mapView.onStop();
    if (locationEngine != null) {
      locationEngine.removeLocationEngineListener(this);
      locationEngine.removeLocationUpdates();
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mapView.onDestroy();
    if (locationEngine != null) {
      locationEngine.deactivate();
    }
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }

  @Override
  @SuppressWarnings({"MissingPermission"})
  public void onConnected() {
    locationEngine.requestLocationUpdates();
  }

  @Override
  public void onLocationChanged(Location location) {
    mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
        new LatLng(location.getLatitude(), location.getLongitude()), 16));
    locationEngine.removeLocationEngineListener(this);
  }
}
// #-end-code-snippet: location-plugin-options-activity full-java
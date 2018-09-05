package com.mapbox.mapboxandroiddemo.examples.plugins;

// #-code-snippet: location-plugin-options-activity full-java

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerOptions;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.OnCameraTrackingChangedListener;
import com.mapbox.mapboxsdk.plugins.locationlayer.OnLocationLayerClickListener;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.List;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;

/**
 * Use the LocationLayerOptions class to customize the Location Layer Plugin's
 * device location icon.
 */
public class LocationOptionsActivity extends AppCompatActivity implements
  OnMapReadyCallback, OnLocationLayerClickListener, PermissionsListener, OnCameraTrackingChangedListener {

  private static String TAG = "LocationOptionsActivity";
  private PermissionsManager permissionsManager;
  private MapView mapView;
  private MapboxMap mapboxMap;
  private LocationLayerPlugin locationLayerPlugin;
  private boolean isInTrackingMode;

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
    this.mapboxMap = mapboxMap;
    enableLocationPlugin();
  }

  @SuppressWarnings( {"MissingPermission"})
  private void enableLocationPlugin() {
    // Check if permissions are enabled and if not request
    if (PermissionsManager.areLocationPermissionsGranted(this)) {

      // Create and customize the plugin's options
      LocationLayerOptions locationLayerOptions = LocationLayerOptions.builder(this)
        .elevation(5)
        .accuracyAlpha(.6f)
        .accuracyColor(Color.RED)
        .foregroundDrawable(R.drawable.android_custom_location_icon)
        .build();

      Log.d(TAG, "enableLocationPlugin: made it here 1");
      // Create the plugin
      locationLayerPlugin = new LocationLayerPlugin(mapView, mapboxMap, locationLayerOptions);

      // Set the plugin's camera and render modes
      locationLayerPlugin.setCameraMode(CameraMode.TRACKING);
      isInTrackingMode = true;
      locationLayerPlugin.setRenderMode(RenderMode.NORMAL);

      Log.d(TAG, "enableLocationPlugin: made it here 2");

      // Add the location icon click listener
      locationLayerPlugin.addOnLocationClickListener(this);

      // Add the camera tracking listener. Fires if the map camera is manually moved.
      locationLayerPlugin.addOnCameraTrackingChangedListener(this);

      Log.d(TAG, "enableLocationPlugin: made it here 3");

      getLifecycle().addObserver(locationLayerPlugin);

      setUpClickMeSymbolLayer();

      Log.d(TAG, "enableLocationPlugin: made it here 4");

      findViewById(R.id.back_to_camera_tracking_mode).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {

          if (!isInTrackingMode) {
            isInTrackingMode = true;
          } else {
            isInTrackingMode = false;
          }
        }
      });

    } else {
      permissionsManager = new PermissionsManager(this);
      permissionsManager.requestLocationPermissions(this);
    }
  }

  @Override
  @SuppressWarnings( {"MissingPermission"})
  public void onLocationLayerClick() {
    if (locationLayerPlugin.getLastKnownLocation() != null) {
      Toast.makeText(this, String.format(getString(R.string.current_location),
        locationLayerPlugin.getLastKnownLocation().getLatitude(),
        locationLayerPlugin.getLastKnownLocation().getLongitude(), "\ud83d\ude01"), Toast.LENGTH_LONG).show();
    }
  }

  @Override
  public void onCameraTrackingDismissed() {
    Log.d("LocationOptionsActivity", "onCameraTrackingDismissed: ");
    isInTrackingMode = false;
  }

  @Override
  public void onCameraTrackingChanged(int currentMode) {
    Log.d("LocationOptionsActivity", "onCameraTrackingChanged: ");
  }

  @SuppressWarnings( {"MissingPermission"})
  private void setUpClickMeSymbolLayer() {
    FeatureCollection featureCollection = FeatureCollection.fromFeatures(new Feature[] {});
    GeoJsonSource geoJsonSource = new GeoJsonSource("source-id", featureCollection);
    mapboxMap.addSource(geoJsonSource);
    SymbolLayer symbolLayer = new SymbolLayer("layer-id", "source-id");
    symbolLayer.setProperties(
      textSize(17f),
      textColor(Color.BLUE),
      textField(getString(R.string.tap_the_icon)),
      textOffset(new Float[] {0f, -3f}),
      textIgnorePlacement(true),
      textAllowOverlap(true)
    );
    mapboxMap.addLayer(symbolLayer);
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

  @SuppressWarnings( {"MissingPermission"})
  protected void onStart() {
    super.onStart();
    mapView.onStart();
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
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }
}
// #-end-code-snippet: location-plugin-options-activity full-java
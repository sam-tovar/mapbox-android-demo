package com.mapbox.mapboxandroiddemo.examples.dds;

// #-code-snippet: animated-dash-line full-java

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.net.MalformedURLException;
import java.net.URL;

import static com.mapbox.mapboxsdk.style.layers.Property.LINE_CAP_ROUND;
import static com.mapbox.mapboxsdk.style.layers.Property.LINE_JOIN_ROUND;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineDasharray;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

/**
 * Create an effect of animated and moving LineLayer dashes by rapidly adjusting the
 * dash and gap lengths.
 */
public class AnimatedDashLineActivity extends AppCompatActivity implements OnMapReadyCallback {

  private MapView mapView;
  private MapboxMap mapboxMap;
  private Handler handler;
  private String tag = "AnimatedDashLine";
  private RefreshDashAndGapRunnable refreshDashAndGapRunnable;
  private int animationSpeedMillseconds = 50;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_animated_dash_line);

    handler = new Handler();
    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {
    AnimatedDashLineActivity.this.mapboxMap = mapboxMap;
    initBikePathLayer();
    Log.d(tag, "onMapReady: here 1");
    Runnable runnable = new RefreshDashAndGapRunnable();
    Log.d(tag, "onMapReady: runnable made");
    handler.postDelayed(runnable, animationSpeedMillseconds);
    Log.d(tag, "onMapReady: here 2");
  }

  private void initBikePathLayer() {
    try {
      GeoJsonSource geoJsonSource = new GeoJsonSource("animated_line_source", new URL(
        "https://raw.githubusercontent.com/Chicago/osd-bike-routes/master/data/Bikeroutes.geojson"
      ));
      mapboxMap.addSource(geoJsonSource);
      LineLayer animatedDashBikeLineLayer = new LineLayer("animated_line_layer_id", "animated_line_source");
      animatedDashBikeLineLayer.withProperties(
        lineWidth(4.5f),
        lineColor(Color.parseColor("#bf42f4")),
        lineCap(LINE_CAP_ROUND),
        lineJoin(LINE_JOIN_ROUND)
      );
      mapboxMap.addLayer(animatedDashBikeLineLayer);
    } catch (MalformedURLException malformedUrlException) {
      Log.d("AnimatedDashLine", "Check the URL: " + malformedUrlException.getMessage());
    }
  }

  private class RefreshDashAndGapRunnable implements Runnable {

    private float valueOne, valueTwo, valueThree, valueFour, ValueFive;
    private float dashLength = 1;
    private float gapLength = 3;

    // We divide the animation up into 40 totalNumberOfSteps to make careful use of the finite space in
    // LineAtlas
    private float totalNumberOfSteps = 40;

    // A # of totalNumberOfSteps proportional to the dashLength are devoted to manipulating the dash
    private float dashSteps = totalNumberOfSteps * dashLength / (gapLength + dashLength);

    // A # of totalNumberOfSteps proportional to the gapLength are devoted to manipulating the gap
    private float gapSteps = totalNumberOfSteps - dashSteps;

    // The current currentStep #
    private int currentStep = 0;

    private String TAG = "AnimatedDashLine";

    @Override
    public void run() {
      Log.d(TAG, "RefreshDashAndGapRunnable run: ");
      currentStep = currentStep + 1;
      if (currentStep >= totalNumberOfSteps) {
        currentStep = 0;
      }
      if (currentStep < dashSteps) {
        valueOne = currentStep / dashSteps;
        valueTwo = (1 - valueOne) * dashLength;
        valueThree = gapLength;
        valueFour = valueOne * dashLength;
        ValueFive = 0;
      } else {
        valueOne = (currentStep - dashSteps) / (gapSteps);
        valueTwo = 0;
        valueThree = (1 - valueOne) * gapLength;
        valueFour = dashLength;
        ValueFive = valueOne * gapLength;
      }
      Log.d(TAG, "RefreshDashAndGapRunnable run: here");

      Float[] newFloatArray = new Float[] {valueTwo, valueThree, valueFour, ValueFive};

      mapboxMap.getLayer("animated_line_layer_id").setProperties(
        lineDasharray(newFloatArray));
      Log.d(TAG, "RefreshDashAndGapRunnable run: layer done being gotten");
      handler.postDelayed(this, animationSpeedMillseconds);
    }
  }

  // Add the mapView lifecycle to the activity's lifecycle methods
  @Override
  public void onResume() {
    super.onResume();
    mapView.onResume();
  }

  @Override
  protected void onStart() {
    super.onStart();
    mapView.onStart();
  }

  @Override
  protected void onStop() {
    super.onStop();
    mapView.onStop();
  }

  @Override
  public void onPause() {
    super.onPause();
    mapView.onPause();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mapView.onDestroy();
    handler.removeCallbacks(refreshDashAndGapRunnable);
    refreshDashAndGapRunnable = null;
    handler = null;
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}
// #-end-code-snippet: animated-dash-line full-java
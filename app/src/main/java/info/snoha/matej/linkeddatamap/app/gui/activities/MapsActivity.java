package info.snoha.matej.linkeddatamap.app.gui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import info.snoha.matej.linkeddatamap.Log;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.app.gui.nearby.NearbyAdapter;
import info.snoha.matej.linkeddatamap.app.gui.utils.UI;
import info.snoha.matej.linkeddatamap.app.internal.map.LayerManager;
import info.snoha.matej.linkeddatamap.app.internal.map.MapManager;
import info.snoha.matej.linkeddatamap.app.internal.model.MarkerModel;
import info.snoha.matej.linkeddatamap.app.internal.model.Position;
import info.snoha.matej.linkeddatamap.app.internal.utils.AndroidUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    /** MAP **/

    private GoogleMap map;
    private GoogleApiClient apiClient;

	/** Position tracking **/

    private boolean positionTracking;
    private Timer positionTrackingTimer;
	private static final int POSITION_TRACKING_FREQUENCY = 1_000;

	/** Map camera tracking **/

    private CameraPosition cameraPosition;
    private Timer cameraTrackingTimer;
	private static final int CAMERA_TRACKING_FREQUENCY = 3_000;

	/** Nearby tracking **/

	private boolean nearbyTracking;
    private Timer nearbyTrackingTimer;
	private static final int NEARBY_TRACKING_FREQUENCY = 5_000;

    protected void onStart() {
        super.onStart();
        apiClient.connect();
    }

    protected void onStop() {
        super.onStop();
        if (apiClient.isConnected())
            apiClient.disconnect();
    }

    // TODO onPause, onResume - timers wait&notify?

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            //actionBar.setIcon(R.drawable.ic_map_white_24dp);
            //actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getTitle() + " " + AndroidUtils.getVersion(this));
        }

        apiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        LayerManager.with(this);
		MapManager.with(this);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(v -> {

			Location location = getCurrentLocation();
			if (location != null) {

				if (map.getCameraPosition().zoom <= 14) {
					map.animateCamera(CameraUpdateFactory.newLatLngZoom(
							new LatLng(location.getLatitude(), location.getLongitude()), 15));
				} else {
					map.animateCamera(CameraUpdateFactory.newLatLng(
							new LatLng(location.getLatitude(), location.getLongitude())));
				}
			}
		});
        fab.setOnLongClickListener(v -> {

			new MaterialDialog.Builder(MapsActivity.this)
					.title("Turn position tracking " + (positionTracking ? "OFF" : "ON") + "?")
					.neutralText("Cancel")
					.positiveText("OK")
					.onPositive((dialog, which) -> {
						positionTracking = !positionTracking;
						if (positionTracking) {
							positionTrackingTimer = new Timer("Position Tracking Timer");
							positionTrackingTimer.scheduleAtFixedRate(
									new TimerTask() {
										@Override
										public void run() {
											// TODO
											UI.run(() -> fab.callOnClick());
										}
									}, 0, POSITION_TRACKING_FREQUENCY
							);

						} else {
							if (positionTrackingTimer != null) {
								positionTrackingTimer.cancel();
								positionTrackingTimer = null;
							}
						}
					})
					.show();
			return true;
		});

        ((AppCompatButton) findViewById(R.id.button_clear)).setTextColor(Color.BLACK); // < API21
        findViewById(R.id.button_clear).setOnClickListener(v -> {
			MapManager.setLayers(cameraPosition, LayerManager.LAYER_NONE);
			hideNearby();
		});

        ((AppCompatButton) findViewById(R.id.button_layers)).setTextColor(Color.BLACK); // < API21
        findViewById(R.id.button_layers).setOnClickListener(v -> {

			if (LayerManager.getLayerIDs(true).size() == 0) {
				UI.message(MapsActivity.this, "No layers.\n" +
						"Please open Settings --> Layers to load defaults");
				return;
			}

			List<String> enabledLayerNames = LayerManager.getLayerNames(true);
			List<Integer> selectedLayerDialogIndexes = new ArrayList<>();
			for (int layerID : MapManager.getLayers()) {
				selectedLayerDialogIndexes.add(enabledLayerNames.indexOf(
						LayerManager.getLayerName(layerID)
				));
			}

			new MaterialDialog.Builder(MapsActivity.this)
					.title("Choose layers")
					.items(enabledLayerNames)
					.itemsCallbackMultiChoice(selectedLayerDialogIndexes.toArray(new Integer[0]),
                            (dialog, which, text) -> {

								List<String> layerNames = new ArrayList<>(text.length);
								for (CharSequence name : text) {
									layerNames.add(name.toString());
								}
								MapManager.setLayers(cameraPosition, LayerManager.getLayerIDs(layerNames));
								return true;
							})
					.positiveText("OK")
					.neutralText("Cancel")
					.show();

		});

        AppCompatButton nearbyButton = (AppCompatButton) findViewById(R.id.button_nearby);
        nearbyButton.setTextColor(Color.BLACK); // < API21
        nearbyButton.setOnClickListener((View v) -> {

        	nearbyTracking = !nearbyTracking;

        	if (nearbyTracking) {
        		nearbyTrackingTimer = new Timer("Nearby Tracking Timer");
        		nearbyTrackingTimer.scheduleAtFixedRate(new TimerTask() {

        			private boolean firstRun = true;

					@Override
					public void run() {
						showAndRefreshNearby(firstRun);
						firstRun = false;
					}
				}, 0, NEARBY_TRACKING_FREQUENCY);
			} else {
        		if (nearbyTrackingTimer != null) {
					nearbyTrackingTimer.cancel();
					nearbyTrackingTimer = null;
				}
        		hideNearby();
			}
		});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                MapManager.setLayers(cameraPosition, MapManager.getLayers());
                return true;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setMapToolbarEnabled(false);
        try {
            map.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            UI.message(this, "Missing location permission");
        }

        map.setOnMarkerClickListener(marker -> {

			TextView textView = new TextView(MapsActivity.this);
			textView.setAutoLinkMask(Linkify.WEB_URLS);
			textView.setText(marker.getSnippet());

			new MaterialDialog.Builder(MapsActivity.this)
					.title(marker.getTitle())
					.customView(textView, true)
					.neutralText("Cancel")
					.show();

			return true;
		});

        map.setOnCameraChangeListener(cameraPosition -> MapsActivity.this.cameraPosition = cameraPosition);

        // TODO
        LatLng mapCenter = new LatLng(50.0819015, 14.4326654);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(mapCenter, 6));

        MapManager.with(this, map);

        cameraTrackingTimer = new Timer("Camera Tracking Timer");
        cameraTrackingTimer.scheduleAtFixedRate(new TimerTask() {

            CameraPosition lastPosition = cameraPosition;

            @Override
            public void run() {
                if (!ObjectUtils.equals(lastPosition, cameraPosition)) {
                    lastPosition = cameraPosition;
                    MapManager.updateLayers(cameraPosition);
                }
            }
        }, 0, CAMERA_TRACKING_FREQUENCY);
    }

    private Location getCurrentLocation() {
        try {
            Location location = LocationServices.FusedLocationApi.getLastLocation(apiClient);

            if (location == null && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                throw new SecurityException("Missing location permission");
            }
            return location;

        } catch (SecurityException e) {

            if (hasWindowFocus()) {
                UI.message(this, "Missing location permission");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        0);
            }
            return null;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

        LocationRequest request = new LocationRequest();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(POSITION_TRACKING_FREQUENCY);
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, request, new LocationCallback() {

                @Override
                public void onLocationResult(LocationResult result) {
                }
            }, Looper.getMainLooper());
        } catch (SecurityException e) {
            UI.message(this, "Missing location permission");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        UI.message(this, "Google APIs disconnected");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        UI.message(this, "Google APIs failed to connect");
    }

	private void hideNearby() {
		RecyclerView listView = (RecyclerView) findViewById(R.id.nearby);
		UI.run(() -> {
			listView.setVisibility(View.GONE);
			listView.setAdapter(new NearbyAdapter(this, Collections.emptyList()));
		});
	}

	private void showAndRefreshNearby(boolean showMessages) {

		Log.debug("Refreshing nearby");
		if (showMessages) {
			UI.message(MapsActivity.this, "Please wait");
		}

		Location location = getCurrentLocation();
		if (location == null) {
			if (showMessages) {
				UI.message(MapsActivity.this, "Unknown location");
			}
			hideNearby();
			return;
		}

		Position myPosition = new Position(location.getLatitude(), location.getLongitude());
		List<MarkerModel> nearbyMarkers = MapManager.getNearbyMarkers(myPosition, 10);

		Log.debug("Found " + nearbyMarkers.size() + " nearby");

		if (nearbyMarkers.isEmpty()) {
			if (showMessages) {
				UI.message(MapsActivity.this, "Nothing near your location");
			}
			hideNearby();
			return;
		}

		RecyclerView listView = (RecyclerView) findViewById(R.id.nearby);

		UI.run(() -> {
			listView.setVisibility(View.VISIBLE);
			if (listView.getAdapter() == null
					|| !nearbyMarkers.equals(((NearbyAdapter) listView.getAdapter()).getItems())) {

				listView.setLayoutManager(new LinearLayoutManager(this));
				listView.setAdapter(new NearbyAdapter(this, nearbyMarkers, myPosition));
			}
		});
	}
}
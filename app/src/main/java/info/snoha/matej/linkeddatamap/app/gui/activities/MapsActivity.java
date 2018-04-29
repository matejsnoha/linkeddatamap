package info.snoha.matej.linkeddatamap.app.gui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import info.snoha.matej.linkeddatamap.Log;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.app.gui.nearby.NearbyAdapter;
import info.snoha.matej.linkeddatamap.app.gui.utils.UI;
import info.snoha.matej.linkeddatamap.app.internal.layers.Layer;
import info.snoha.matej.linkeddatamap.app.internal.layers.LayerDatabase;
import info.snoha.matej.linkeddatamap.app.internal.map.MapManager;
import info.snoha.matej.linkeddatamap.app.internal.model.MarkerModel;
import info.snoha.matej.linkeddatamap.app.internal.model.Position;
import info.snoha.matej.linkeddatamap.app.internal.utils.AndroidUtils;

import io.fabric.sdk.android.Fabric;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends AppCompatActivity
		implements OnMapReadyCallback {

	/** Activity singleton **/

	private static MapsActivity instance;

	/** MAP **/

	private GoogleMap map;

	/** Location tracking **/

	private static final int POSITION_TRACKING_FREQUENCY = 1_000;
	private static final int LOCATION_PERMISSION_REQUEST_CODE = 11;

	private Location location;
	private boolean locationTracking;
	private Timer locationTrackingTimer;
	private LocationSource.OnLocationChangedListener locationChangeListenerForMap;

	/** Map camera tracking **/

	private static final int CAMERA_TRACKING_FREQUENCY = 3_000;

	private CameraPosition cameraPosition;
	private Timer cameraTrackingTimer;

	/** Nearby tracking **/

	private static final int NEARBY_TRACKING_FREQUENCY = 3_000;
	private static final int NEARBY_COUNT = 20;

	private boolean nearbyTracking;
	private Timer nearbyTrackingTimer;

	public MapsActivity() {
		instance = this;
	}

	public static MapsActivity getInstance() {
		return instance;
	}

	protected void onStart() {
		super.onStart();
		startLocationTracking();
	}

	protected void onStop() {
		super.onStop();
		if (cameraTrackingTimer != null) {
			cameraTrackingTimer.cancel();
		}
		if (nearbyTrackingTimer != null) {
			nearbyTrackingTimer.cancel();
		}
		if (locationTrackingTimer != null) {
			locationTrackingTimer.cancel();
		}
		Log.info("Maps Activity stopped");
	}

	// TODO onPause, onResume - timers wait&notify?

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Fabric.with(this, new Crashlytics());

		Log.info("Maps Activity starting");

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayShowTitleEnabled(true);
			actionBar.setDisplayShowHomeEnabled(true);
			//actionBar.setIcon(R.drawable.ic_map_white_24dp);
			//actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setTitle(getTitle() + " " + AndroidUtils.getVersion(this));
		}

		setContentView(R.layout.activity_maps);
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		LayerDatabase.with(this);
		MapManager.with(this, null, this::showProgress, this::hideProgress); // map initialized later

		FloatingActionButton fab = findViewById(R.id.fab);
		fab.setOnClickListener(v -> {

			Location location = getCurrentLocation();
			if (location != null) {

				LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
				CameraUpdate update = map.getCameraPosition().zoom <= 14
						? CameraUpdateFactory.newLatLngZoom(latLng, 15)
						: CameraUpdateFactory.newLatLng(latLng);

				Log.info("FAB moving camera to " + location.getLatitude() + " " + location.getLongitude());
				map.animateCamera(update);
			}
		});
		fab.setOnLongClickListener(v -> {

			new MaterialDialog.Builder(MapsActivity.this)
					.title("Turn location tracking " + (locationTracking ? "OFF" : "ON") + "?")
					.neutralText("Cancel")
					.positiveText("OK")
					.onPositive((dialog, which) -> {
						locationTracking = !locationTracking;
						if (locationTracking) {
							locationTrackingTimer = new Timer("Location Tracking Timer");
							locationTrackingTimer.scheduleAtFixedRate(
									new TimerTask() {
										@Override
										public void run() {
											// TODO
											UI.run(fab::callOnClick);
										}
									}, 0, POSITION_TRACKING_FREQUENCY
							);

						} else {
							if (locationTrackingTimer != null) {
								locationTrackingTimer.cancel();
								locationTrackingTimer = null;
							}
						}
					})
					.show();
			return true;
		});

		((AppCompatButton) findViewById(R.id.button_clear)).setTextColor(Color.BLACK); // < API21
		findViewById(R.id.button_clear).setOnClickListener(v -> {
			MapManager.setDataLayers(cameraPosition, Collections.emptyList());
			hideNearby();
		});

		((AppCompatButton) findViewById(R.id.button_layers)).setTextColor(Color.BLACK); // < API21
		findViewById(R.id.button_layers).setOnClickListener(v -> {

			if (LayerDatabase.getEnabledLayers().isEmpty()) {
				UI.message(MapsActivity.this, "No layers.\n" +
						"Please specify some in Settings --> Map Layers & Data Layers");
				return;
			}

			List<Layer> enabledLayers = new ArrayList<>(LayerDatabase.getEnabledLayers());

			List<Integer> selectedLayerDialogIndexes = new ArrayList<>();
			for (Layer layer : MapManager.getVisibleLayers()) {
				selectedLayerDialogIndexes.add(enabledLayers.indexOf(layer));
			}

			new MaterialDialog.Builder(MapsActivity.this)
					.title("Choose layers")
					.items(CollectionUtils.collect(enabledLayers, Layer::getTitle))
					.itemsCallbackMultiChoice(selectedLayerDialogIndexes.toArray(new Integer[0]),
							(dialog, which, text) -> {

								List<Layer> layers = new ArrayList<>(which.length);
								for (Integer index : which) {
									if (index >= 0 && index < enabledLayers.size()) {
										layers.add(enabledLayers.get(index));
									}
								}
								MapManager.setDataLayers(cameraPosition, layers);
								return true;
							})
					.positiveText("OK")
					.neutralText("Cancel")
					.show();
		});

		AppCompatButton nearbyButton = findViewById(R.id.button_nearby);
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

		requestLocationPermission();
	}

	public void requestLocationPermission() {
		// check and request location permission if needed
		if (!hasLocationPermission()) {

			UI.message(this, "Missing location permission");
			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
					LOCATION_PERMISSION_REQUEST_CODE);
		}
	}

	public boolean hasLocationPermission() {
		return ActivityCompat.checkSelfPermission(this,
				Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
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
				MapManager.setDataLayers(cameraPosition, MapManager.getVisibleLayers());
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

		// TODO define in data layer and also zoom
		LatLng mapCenter = new LatLng(50.0819015, 14.4326654);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(mapCenter, 6));

		cameraPosition = map.getCameraPosition();
		map.setOnCameraMoveListener(() -> cameraPosition = map.getCameraPosition());

		MapManager.with(this, map, this::showProgress, this::hideProgress);

		cameraTrackingTimer = new Timer("Camera Tracking Timer");
		cameraTrackingTimer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				MapManager.updateMarkersOnMap(cameraPosition);
			}
		}, 0, CAMERA_TRACKING_FREQUENCY);

		Log.info("Map initialized");
	}

	private Location getCurrentLocation() {
		return location;
	}

	private void startLocationTracking() {

		LocationRequest request = LocationRequest.create();
		request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		request.setInterval(POSITION_TRACKING_FREQUENCY);
		request.setFastestInterval(POSITION_TRACKING_FREQUENCY);

		try {
			if (map != null) {
				map.setMyLocationEnabled(true);
				map.setLocationSource(new LocationSource() {
					@Override
					public void activate(OnLocationChangedListener onLocationChangedListener) {
						locationChangeListenerForMap = onLocationChangedListener;
						if (location != null) {
							locationChangeListenerForMap.onLocationChanged(location);
						}
					}

					@Override
					public void deactivate() {
						locationChangeListenerForMap = null;
					}
				});
			}

			LocationServices.getFusedLocationProviderClient(this).getLastLocation()
					.addOnSuccessListener(location -> {

						this.location = location;
						if (locationChangeListenerForMap != null) {
							locationChangeListenerForMap.onLocationChanged(location);
						}
					});

		} catch (SecurityException e) {
			UI.message(this, "Missing location permission");
		}


	}

	private void hideNearby() {
		RecyclerView listView = findViewById(R.id.nearby);
		UI.run(() -> {
			listView.setVisibility(View.GONE);
			listView.setAdapter(new NearbyAdapter(this, Collections.emptyList()));
		});
	}

	private void showAndRefreshNearby(boolean showMessages) {

		Log.debug("Refreshing nearby");

		Location location = getCurrentLocation();
		if (location == null) {
			if (showMessages) {
				UI.message(this, "Unknown location");
			}
			hideNearby();
			return;
		}

		Position myPosition = new Position(location);
		List<MarkerModel> nearbyMarkers = MapManager.getSortedClosestMarkers(myPosition, NEARBY_COUNT);

		Log.debug("Found " + nearbyMarkers.size() + " nearby");

		if (nearbyMarkers.isEmpty()) {
			if (showMessages) {
				UI.message(this, "Nothing near your location");
			}
			hideNearby();
			return;
		}

		RecyclerView listView = findViewById(R.id.nearby);

		UI.run(() -> {
			// TODO set visibility sooner or check for null timer - bug with nearby showing after close
			listView.setVisibility(View.VISIBLE);
			if (listView.getAdapter() == null
					|| !nearbyMarkers.equals(((NearbyAdapter) listView.getAdapter()).getItems())) {

				listView.setLayoutManager(new LinearLayoutManager(this));
				listView.setAdapter(new NearbyAdapter(this, nearbyMarkers, myPosition));
			}
		});
	}

	public void showProgress() {
		findViewById(R.id.progress).setVisibility(View.VISIBLE);
	}

	public void hideProgress() {
		findViewById(R.id.progress).setVisibility(View.GONE);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		if (hasLocationPermission()) {
			try {
				startLocationTracking();
			} catch (SecurityException e) {
				Log.error("Missing location permission, even though it was just granted");
			}
		} else {
			UI.message(this, "You can enable location permission later in app settings");
		}
	}
}
package info.snoha.matej.linkeddatamap.gui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
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
import com.google.android.gms.maps.model.Marker;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import info.snoha.matej.linkeddatamap.internal.map.LayerManager;
import info.snoha.matej.linkeddatamap.internal.map.MapManager;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.gui.utils.UI;
import info.snoha.matej.linkeddatamap.internal.model.MarkerModel;
import info.snoha.matej.linkeddatamap.internal.model.Position;
import info.snoha.matej.linkeddatamap.internal.utils.Utils;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final int POSITION_TRACKING_FREQUENCY = 1000;
    private static final int CAMERA_TRACKING_FREQUENCY = 3000;

    private GoogleMap map;
    private GoogleApiClient apiClient;

    private boolean positionTracking;
    private Timer positionTrackingTimer;

    private CameraPosition cameraPosition;
    private Timer cameraTrackingTimer;

    protected void onStart() {
        super.onStart();
        apiClient.connect();
    }

    protected void onStop() {
        super.onStop();
        if (apiClient.isConnected())
            apiClient.disconnect();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            //actionBar.setIcon(R.drawable.ic_map_white_24dp);
            //actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getTitle() + " " + Utils.getVersion(this));
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

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Location location = getCurrentLocation();
                if (location != null) {

                    if (map.getCameraPosition().zoom <= 12) {
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(location.getLatitude(), location.getLongitude()), 16));
                    } else {
                        map.animateCamera(CameraUpdateFactory.newLatLng(
                                new LatLng(location.getLatitude(), location.getLongitude())));
                    }
                }
            }
        });
        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                new MaterialDialog.Builder(MapsActivity.this)
                        .title("Turn position tracking " + (positionTracking ? "OFF" : "ON") + "?")
                        .neutralText("Cancel")
                        .positiveText("OK")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                positionTracking = !positionTracking;
                                if (positionTracking) {
                                    positionTrackingTimer = new Timer("Position Tracking Timer");
                                    positionTrackingTimer.scheduleAtFixedRate(
                                            new TimerTask() {
                                                @Override
                                                public void run() {
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            fab.callOnClick();
                                                        }
                                                    });
                                                }
                                            }, 0, POSITION_TRACKING_FREQUENCY
                                    );

                                } else {
                                    if (positionTrackingTimer != null) {
                                        positionTrackingTimer.cancel();
                                        positionTrackingTimer = null;
                                    }
                                }
                            }
                        })
                        .show();
                return true;
            }
        });

        ((AppCompatButton) findViewById(R.id.button_clear)).setTextColor(Color.BLACK); // < API21
        findViewById(R.id.button_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapManager.setLayers(cameraPosition, LayerManager.LAYER_NONE);
            }
        });

        ((AppCompatButton) findViewById(R.id.button_layers)).setTextColor(Color.BLACK); // < API21
        findViewById(R.id.button_layers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
                                new MaterialDialog.ListCallbackMultiChoice() {

                            @Override
                            public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {

                                List<String> layerNames = new ArrayList<>(text.length);
                                for (CharSequence name : text) {
                                    layerNames.add(name.toString());
                                }
                                MapManager.setLayers(cameraPosition, LayerManager.getLayerIDs(layerNames));
                                return true;
                            }
                        })
                        .positiveText("OK")
                        .neutralText("Cancel")
                        .show();

            }
        });

        ((AppCompatButton) findViewById(R.id.button_nearby)).setTextColor(Color.BLACK); // < API21
        findViewById(R.id.button_nearby).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //DoubleShot.dump(MapsActivity.this, "LinkedDataMap/doubleshot.nt");

                UI.message(MapsActivity.this, "Please wait");

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Location location = getCurrentLocation();
                        if (location == null) {
                            UI.message(MapsActivity.this, "Unknown location");
                            return;
                        }

                        final List<MarkerModel> nearbyMarkers = MapManager.getNearbyMarkers(
                                new Position(location.getLatitude(), location.getLongitude())
                        );
                        if (nearbyMarkers.isEmpty()) {
                            UI.message(MapsActivity.this, "Nothing near your location");
                            return;
                        }

                        UI.run(new Runnable() {
                            @Override
                            public void run() {

                            new MaterialDialog.Builder(MapsActivity.this)
                                .title("Near your location")
                                .items(CollectionUtils.collect(nearbyMarkers,
                                        new Transformer<MarkerModel, String>() {
                                            @Override
                                            public String transform(MarkerModel input) {
                                                return input.getName() + "\n" + input.getPosition().toShortString();
                                            }
                                        }))
                                .itemsCallback(new MaterialDialog.ListCallback() {

                                    @Override
                                    public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                        MarkerModel marker = nearbyMarkers.get(which);
                                        UI.message(MapsActivity.this, "Launching navigation to " + marker.getName());
                                        String uri = String.format(Locale.US,
                                                "http://maps.google.com/maps?daddr=%f,%f",
                                                marker.getPosition().getLatitude(),
                                                marker.getPosition().getLongitude());
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                                        startActivity(intent);
                                    }
                                })
                                .neutralText("Cancel")
                                .show();
                            }
                        });
                    }
                }, "Nearby places").start();
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

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {

                TextView textView = new TextView(MapsActivity.this);
                textView.setAutoLinkMask(Linkify.WEB_URLS);
                textView.setText(marker.getSnippet());

                new MaterialDialog.Builder(MapsActivity.this)
                        .title(marker.getTitle())
                        .customView(textView, true)
                        .neutralText("Cancel")
                        .show();

                return true;
            }
        });

        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                MapsActivity.this.cameraPosition = cameraPosition;
            }
        });

        // TODO
        LatLng mapCenter = new LatLng(50.0819015, 14.4326654);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(mapCenter, 6));

        LayerManager.with(this);
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
}
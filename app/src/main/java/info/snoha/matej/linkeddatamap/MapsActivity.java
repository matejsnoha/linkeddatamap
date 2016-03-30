package info.snoha.matej.linkeddatamap;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

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

import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends ActionBarActivity
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
            //actionBar.setDisplayShowHomeEnabled(true);
            //actionBar.setIcon(R.drawable.ic_map_white_24dp);
            //actionBar.setDisplayHomeAsUpEnabled(true);
        }

        actionBar.setTitle(getTitle() + " " + Utils.getVersion(this));

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
                } else {
                    Snackbar.make(getSnackView(), "Location not available", Snackbar.LENGTH_LONG).show();
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
                    Snackbar.make(getSnackView(), "No layers.\n" +
                            "Please open Settings --> Layers to load defaults",
                            Snackbar.LENGTH_LONG).show();
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
                Snackbar.make(getSnackView(), "Not implemented yet", Snackbar.LENGTH_LONG).show();
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
            Snackbar.make(getSnackView(), "Missing location permission", Snackbar.LENGTH_LONG).show();
        }

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {

                if (marker.getSnippet().contains("<http")) {

                    new MaterialDialog.Builder(MapsActivity.this)
                            .title(marker.getTitle())
                            .content(marker.getSnippet())
                            .positiveText("WEB")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    startActivity(new Intent(Intent.ACTION_VIEW,
                                            Uri.parse(marker.getSnippet().substring(
                                                    marker.getSnippet().indexOf("<") + 1,
                                                    marker.getSnippet().indexOf(">")
                                            ))));
                                }
                            })
                            .neutralText("Cancel")
                            .show();
                } else {

                    new MaterialDialog.Builder(MapsActivity.this)
                            .title(marker.getTitle())
                            .content(marker.getSnippet())
                            .neutralText("Cancel")
                            .show();
                }
                return true;
            }
        });

        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                MapsActivity.this.cameraPosition = cameraPosition;
            }
        });

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
            return LocationServices.FusedLocationApi.getLastLocation(apiClient);
        } catch (SecurityException e) {
            Snackbar.make(getSnackView(), "Missing location permission", Snackbar.LENGTH_LONG).show();
            return null;
        }
    }

    private View getSnackView() {
        return findViewById(R.id.buttons); //getWindow().getDecorView().getRootView();
    }

    @Override
    public void onConnected(Bundle bundle) {

        //Snackbar.make(getSnackView(), "Google APIs connected", Snackbar.LENGTH_LONG).show();

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
            Snackbar.make(getSnackView(), "Missing location permission", Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Snackbar.make(getSnackView(), "Google APIs disconnected", Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Snackbar.make(getSnackView(), "Google APIs failed to connect", Snackbar.LENGTH_LONG).show();
    }
}
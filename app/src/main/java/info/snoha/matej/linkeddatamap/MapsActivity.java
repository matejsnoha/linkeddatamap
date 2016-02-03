package info.snoha.matej.linkeddatamap;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends ActionBarActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int LAYER_NONE = 0;
    private static final int LAYER_RUIAN_MIN = 1;
    private static final int LAYER_RUIAN = 2;
    private static final int LAYER_DOUBLESHOT = 3;

    private static final int POSITION_TRACKING_FREQUENCY = 1000;

    private GoogleMap map;
    private GoogleApiClient apiClient;
    private boolean positionTracking;
    private Timer positionTrackingTimer;

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
            actionBar.setIcon(R.mipmap.ic_launcher);
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

                    if (map.getCameraPosition().zoom <= 10) {
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(location.getLatitude(), location.getLongitude()), 16));
                    } else {
                        map.animateCamera(CameraUpdateFactory.newLatLng(
                                new LatLng(location.getLatitude(), location.getLongitude())));
                    }
                } else {
                    Snackbar.make(getRootView(), "Location not available", Snackbar.LENGTH_LONG).show();
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

        findViewById(R.id.button_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchLayer(LAYER_NONE);
            }
        });

        findViewById(R.id.button_ruian_min).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchLayer(LAYER_RUIAN_MIN);
            }
        });

        findViewById(R.id.button_ruian).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchLayer(LAYER_RUIAN);
            }
        });

        findViewById(R.id.button_doubleshot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchLayer(LAYER_DOUBLESHOT);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setMapToolbarEnabled(false);
        try {
            map.setMyLocationEnabled(true);
        } catch (SecurityException e) {
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

        LatLng mapCenter = new LatLng(50.0819015, 14.4326654);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(mapCenter, 6));

    }

    private void switchLayer(int layer) {

        final MaterialDialog progressDialog = new MaterialDialog.Builder(this)
                .title("Please wait ...")
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .show();

        map.clear();

        switch (layer) {
            case LAYER_NONE:
                progressDialog.hide();
                break;
            case LAYER_RUIAN_MIN:
                progressDialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final List<MarkerModel> markers = getRuianMarkers(10000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                for (MarkerModel marker : markers) {
                                    map.addMarker(new MarkerOptions()
                                            .position(new LatLng(marker.getLatitude(), marker.getLongitude()))
                                            .title(marker.getName())
                                            .snippet(marker.getText()));
                                }
                                progressDialog.hide();
                            }
                        });
                    }
                }).start();
                break;
            case LAYER_RUIAN:
                progressDialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final List<MarkerModel> markers = getRuianMarkers(null);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                for (MarkerModel marker : markers) {
                                    map.addMarker(new MarkerOptions()
                                            .position(new LatLng(marker.getLatitude(), marker.getLongitude()))
                                            .title(marker.getName())
                                            .snippet(marker.getText()));
                                }
                                progressDialog.hide();
                            }
                        });
                    }
                }).start();
                break;
            case LAYER_DOUBLESHOT:
                progressDialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final List<MarkerModel> markers = getDoubleShotMarkers();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                for (MarkerModel marker : markers) {
                                    map.addMarker(new MarkerOptions()
                                            .position(new LatLng(marker.getLatitude(), marker.getLongitude()))
                                            .title(marker.getName())
                                            .snippet(marker.getText()));
                                }
                                progressDialog.hide();
                            }
                        });
                    }
                }).start();
                break;
        }
    }

    private Location getCurrentLocation() {
        try {
            return LocationServices.FusedLocationApi.getLastLocation(apiClient);
        } catch (SecurityException e) {
            Snackbar.make(getRootView(), "Missing location permission", Snackbar.LENGTH_LONG).show();
            return null;
        }
    }

    private View getRootView() {
        return getWindow().getDecorView().getRootView();
    }

    @Override
    public void onConnected(Bundle bundle) {
        // Snackbar.make(getRootView(), "Google APIs connected", Snackbar.LENGTH_LONG).show();

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
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Snackbar.make(getRootView(), "Google APIs disconnected", Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Snackbar.make(getRootView(), "Google APIs failed to connect", Snackbar.LENGTH_LONG).show();
    }

    static class MarkerModel {

        private double latitude;
        private double longitude;
        private String name;
        private String text;

        public MarkerModel(double latitude, double longitude, String name, String text) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.name = name;
            this.text = text;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    private List<MarkerModel> getDoubleShotMarkers() {

        List<MarkerModel> markers = new ArrayList<>();
        for (DoubleShot.SimplePlace shop : DoubleShot.getPlaces(this)) {
            markers.add(new MarkerModel(shop.latitude, shop.longitude, shop.name, shop.address));
        }
        return markers;
    }

    private List<MarkerModel> getRuianMarkers(Integer limit) {

        List<MarkerModel> markers = new ArrayList<>();
        for (Ruian.SimplePlace place : Ruian.getPlaces(this, limit)) {
            markers.add(new MarkerModel(place.latitude, place.longitude, place.name, place.address));
        }
        return markers;
    }
}

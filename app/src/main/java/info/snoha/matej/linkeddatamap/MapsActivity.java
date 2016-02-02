package info.snoha.matej.linkeddatamap;

import android.location.Location;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

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
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends ActionBarActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap map;
    private GoogleApiClient apiClient;

    private static final int LAYER_NONE = 0;
    private static final int LAYER_RUIAN = 1;
    private static final int LAYER_DOUBLESHOT = 2;

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
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(location.getLatitude(), location.getLongitude()), 16));
                } else {
                    Snackbar.make(getRootView(), "Location not available", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        findViewById(R.id.button_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchLayer(LAYER_NONE);
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

        LatLng mapCenter = new LatLng(50.0819015, 14.4326654);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(mapCenter, 6));

    }

    private void switchLayer(int layer) {

        map.clear();
        switch (layer) {
            case LAYER_NONE:
                break;
            case LAYER_RUIAN:
                for (MarkerModel marker : getRuianMarkers()) {
                    map.addMarker(new MarkerOptions()
                            .position(new LatLng(marker.getLatitude(), marker.getLongitude()))
                            .title(marker.getName())
                            .snippet(marker.getAddress()));
                }
                break;
            case LAYER_DOUBLESHOT:
                for (MarkerModel marker : getDoubleShotMarkers()) {
                    map.addMarker(new MarkerOptions()
                            .position(new LatLng(marker.getLatitude(), marker.getLongitude()))
                            .title(marker.getName())
                            .snippet(marker.getAddress()));
                }
                break;
        }
    }

    private Location getCurrentLocation() {
        try {
            return LocationServices.FusedLocationApi.getLastLocation(apiClient);
        } catch (SecurityException e) {
            return null;
        }
    }

    private View getRootView() {
        return getWindow().getDecorView().getRootView();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Snackbar.make(getRootView(), "Google APIs connected", Snackbar.LENGTH_LONG).show();

        LocationRequest request = new LocationRequest();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(5000);
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, request, new LocationCallback() {

                @Override
                public void onLocationResult(LocationResult result) {
                }
            }, Looper.getMainLooper());
        } catch (SecurityException e) {
            Snackbar.make(getRootView(), "Missing location permission", Snackbar.LENGTH_LONG).show();
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
        private String address;
        private String description;
        private String url;

        public MarkerModel(double latitude, double longitude, String name, String address) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.name = name;
            this.address = address;
        }

        public MarkerModel(double latitude, double longitude, String name, String description, String url, String address) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.name = name;
            this.description = description;
            this.url = url;
            this.address = address;
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

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }

    private List<MarkerModel> getDoubleShotMarkers() {

        List<MarkerModel> markers = new ArrayList<>();
        for (DoubleShot.SimpleShop shop : DoubleShot.getShops(this)) {
            markers.add(new MarkerModel(shop.latitude, shop.longitude, shop.name, shop.address));
        }
        return markers;
    }

    private List<MarkerModel> getRuianMarkers() {

        List<MarkerModel> markers = new ArrayList<>();
//        for (DoubleShot.SimpleShop shop : DoubleShot.getShops(this)) {
//            markers.add(new MarkerModel(shop.latitude, shop.longitude, shop.name, shop.address));
//        }
        return markers;
    }
}

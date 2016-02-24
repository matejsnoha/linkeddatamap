package info.snoha.matej.linkeddatamap;

import android.content.Context;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MapManager {

    public static final int LAYER_NONE = 0;
    public static final int LAYER_RUIAN = 1;
    public static final int LAYER_DOUBLE_SHOT = 2;

    public static final int MARKER_DISTANCE_SCREENS = 1; // number of screens away from center
    public static final int MARKER_MIN_DISTANCE_METERS = 100; // always show markers this far
    public static final int MARKER_MAX_DISPLAY_COUNT = 500; // switch to heatmap if more

    private static Context context;
    private static GoogleMap map;

    private static int layer;

    private static List<MarkerModel> allMarkers;
    private static TileProvider heatmapTileProvider;
    private static boolean heatmapMode;

    public static void with(Context context, GoogleMap map) {
        MapManager.context = context;
        MapManager.map = map;
    }

    public static void setLayer(int layer, final CameraPosition position) {

        final MaterialDialog progressDialog = new MaterialDialog.Builder(context)
                .title("Please wait ...")
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .show();

        map.clear();
        MapManager.layer = layer;
        allMarkers = Collections.emptyList();

        switch (layer) {
            case LAYER_NONE:
                progressDialog.hide();
                break;
            case LAYER_RUIAN:
                progressDialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        setMarkers(MapManager.getRuianMarkers());
                        updateLayer(position, new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.hide();
                            }
                        });
                    }
                }).start();
                break;
            case LAYER_DOUBLE_SHOT:
                progressDialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        setMarkers(MapManager.getDoubleShotMarkers());
                        updateLayer(position, new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.hide();
                            }
                        });
                    }
                }).start();
                break;
        }
    }

    private static void setMarkers(List<MarkerModel> markers) {

        allMarkers = markers;

        heatmapTileProvider = new HeatmapTileProvider.Builder()
                .data(CollectionUtils.collect(allMarkers, new Transformer<MarkerModel, LatLng>() {
                    @Override
                    public LatLng transform(MarkerModel input) {
                        return new LatLng(
                                input.getPosition().getLatitude(),
                                input.getPosition().getLongitude());
                    }
                }))
                .build();
    }

    public static void updateLayer(CameraPosition position) {
        updateLayer(position, null);
    }

    public static void updateLayer(CameraPosition position, Runnable callback) {

        final List<MarkerModel> filteredMarkers = getClosestMarkers(allMarkers, position);

        if (filteredMarkers.size() <= MARKER_MAX_DISPLAY_COUNT) {

            heatmapMode = false;
            Utils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    map.clear();
                    for (MarkerModel marker : filteredMarkers) {
                        map.addMarker(new MarkerOptions()
                                .position(new LatLng(marker.getPosition().getLatitude(),
                                        marker.getPosition().getLongitude()))
                                .title(marker.getName())
                                .snippet(marker.getText()));
                    }
                }
            });

        } else if (!heatmapMode) {

            heatmapMode = true;
            Utils.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    map.clear();
                    map.addTileOverlay(new TileOverlayOptions().tileProvider(heatmapTileProvider));
                }
            });
        } // else do nothing, heatmap persists on camera change

        if (callback != null)
            Utils.runOnUIThread(callback);
    }

    public static List<MarkerModel> getClosestMarkers(List<MarkerModel> allMarkers, CameraPosition position) {

        if (position == null || allMarkers == null)
            return Collections.emptyList();

        Position center = toPosition(position);
        int range = Math.max(MARKER_MIN_DISTANCE_METERS,
                getScreenWidth(center.getLatitude(), position.zoom) * MARKER_DISTANCE_SCREENS);

        List<MarkerModel> filtered = new ArrayList<>();
        for (MarkerModel marker : allMarkers) {
            if (center.distanceTo(marker.getPosition()) <= range)
                filtered.add(marker);
        }

        Log.i("Map layer " + layer, "Showing " + filtered.size() + " markers up to "
                + new DecimalFormat("#.#").format(range / 1000f) + "km away from " + center);

        return filtered;
    }

    public static List<MarkerModel> getDoubleShotMarkers() {

        List<MarkerModel> markers = new ArrayList<>();
        for (DoubleShot.SimplePlace shop : DoubleShot.getPlaces(context)) {
            markers.add(new MarkerModel(new Position(shop.latitude, shop.longitude),
                    shop.name, shop.address));
        }
        return markers;
    }

    public static List<MarkerModel> getRuianMarkers() {

        List<MarkerModel> markers = new ArrayList<>();
        for (Ruian.SimplePlace place : Ruian.getPlaces(context)) {
            markers.add(new MarkerModel(new Position(place.latitude, place.longitude),
                    place.name, place.address));
        }
        return markers;
    }

    private static Position toPosition(CameraPosition cameraPosition) {
        if (cameraPosition == null || cameraPosition.target == null)
            return null;

        return new Position(cameraPosition.target.latitude, cameraPosition.target.longitude);
    }

    private static int getScreenWidth(double latitude, double zoom) {

        final double SCREEN_SIZE = Utils.pixelsToDip(context, Math.max(
                context.getResources().getDisplayMetrics().widthPixels,
                context.getResources().getDisplayMetrics().heightPixels));
        final double EQUATOR_LENGTH = 40075004; // meters

        double pixelSize = (EQUATOR_LENGTH * Math.cos(Math.toRadians(latitude)))
                / Math.pow(2, zoom + 8); // 256 * 2^z
        return (int) (SCREEN_SIZE * pixelSize);
    }
}

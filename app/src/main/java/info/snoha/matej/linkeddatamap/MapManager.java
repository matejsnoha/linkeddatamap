package info.snoha.matej.linkeddatamap;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MapManager {

    public static final int LAYER_NONE = -1;
    public static final int LAYER_RUIAN = 0;
    public static final int LAYER_DOUBLE_SHOT = 1;
    public static final int LAYER_CUSTOM_1 = 2;
    public static final int LAYER_CUSTOM_2 = 3;

    public static final int MARKER_DISTANCE_SCREENS = 1; // number of screens away from center
    public static final int MARKER_MIN_DISTANCE_METERS = 100; // always show markers this far
    public static final int MARKER_MAX_DISPLAY_COUNT = 500; // switch to heatmap if more

    private static Context context;
    private static GoogleMap map;

    private static volatile List<Integer> layers = Collections.emptyList();

    private static volatile List<MarkerModel> allMarkers;

    private static volatile TileProvider heatmapTileProvider;
    private static volatile boolean heatmapMode;

    public static void with(Context context, GoogleMap map) {
        MapManager.context = context;
        MapManager.map = map;
    }

    public static List<Integer> getLayers() {
        return layers;
    }

    public static void setLayers(final CameraPosition position, List<Integer> layers) {
        setLayers(position, layers.toArray(new Integer[0]));
    }

    public static void setLayers(final CameraPosition position, final Integer... layers) {

        final MaterialDialog progressDialog = new MaterialDialog.Builder(context)
                .title("Please wait ...")
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .show();

        map.clear();

        final List<MarkerModel> newMarkers = new ArrayList<>();
        final List<Integer> newLayers = new ArrayList<>();

        if (layers.length == 0 || (layers.length == 1 && layers[0] == LAYER_NONE)) {
            MapManager.layers = newLayers;
            setMarkers(newMarkers);
            progressDialog.hide();
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {

                List<Integer> newLayers = new ArrayList<>();

                for (int layer : layers) {
                    switch (layer) {
                        case LAYER_RUIAN:
                            newLayers.add(layer);
                            newMarkers.addAll(MapManager.getRuianMarkers());
                            break;
                        case LAYER_DOUBLE_SHOT:
                            newLayers.add(layer);
                            newMarkers.addAll(MapManager.getDoubleShotMarkers());
                            break;
                        default:
                            break;
                    }
                }

                MapManager.layers = newLayers;
                setMarkers(newMarkers);
                updateLayers(position, new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.hide();
                    }
                });

            }
        }).start();
    }

    private static void setMarkers(List<MarkerModel> markers) {

        allMarkers = markers;

        if (markers.size() > 0) {
            heatmapMode = false;
            heatmapTileProvider = new HeatmapTileProvider.Builder()
                    .data(CollectionUtils.collect(allMarkers, new Transformer<MarkerModel, LatLng>() {
                        @Override
                        public LatLng transform(MarkerModel input) {
                            return new LatLng(
                                    input.getPosition().getLatitude(),
                                    input.getPosition().getLongitude());
                        }
                    }))
                    .opacity(0.5)
                    .gradient(new Gradient(
                            new int[] {
                                context.getResources().getColor(R.color.primaryDark)},
                            new float[]{
                                0.01f}))
                    .build();
        }
    }

    public static void updateLayers(CameraPosition position) {
        updateLayers(position, null);
    }

    public static void updateLayers(CameraPosition position, Runnable callback) {

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

        Log.i("Map layers " + layers, "Showing " + filtered.size() + " markers up to "
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

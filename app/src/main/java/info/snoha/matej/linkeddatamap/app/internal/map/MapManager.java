package info.snoha.matej.linkeddatamap.app.internal.map;

import android.content.Context;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import org.apache.commons.collections4.CollectionUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import info.snoha.matej.linkeddatamap.app.internal.model.MarkerModel;
import info.snoha.matej.linkeddatamap.app.internal.model.Position;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.app.gui.utils.UI;
import info.snoha.matej.linkeddatamap.app.internal.utils.AndroidUtils;

public class MapManager {

    public static final int MARKER_DISTANCE_SCREENS = 1; // number of screens away from center
    public static final int MARKER_MIN_DISTANCE_METERS = 100; // always show markers this far
    public static final int MARKER_MAX_DISPLAY_COUNT = 500; // switch to heatmap if more

    private static Context context;
    private static GoogleMap map;

    private static volatile List<Integer> layers = Collections.emptyList();

    private static volatile List<MarkerModel> allMarkers;

    private static volatile TileProvider heatmapTileProvider;
    private static volatile boolean heatmapMode;

	public static void with(Context context) {
		MapManager.context = context;
	}

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

    public static void setLayers(final CameraPosition position, final String... layerNames) {
        List<Integer> layerIDs = new ArrayList<>();

    }

    public static void setLayers(final CameraPosition position, final Integer... layerIDs) {

		if (map == null) {
			return;
		}

        final MaterialDialog progressDialog = new MaterialDialog.Builder(context)
                .title("Please wait ...")
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .cancelable(false)
                .show();

		map.clear();

        final List<MarkerModel> newMarkers = new ArrayList<>();
        final List<Integer> newLayers = new ArrayList<>();

        if (layerIDs.length == 0 || (layerIDs.length == 1 && layerIDs[0] == LayerManager.LAYER_NONE)) {
            MapManager.layers = newLayers;
            setMarkers(newMarkers);
            progressDialog.hide();
            return;
        }

        new Thread(() -> {

			List<Integer> newLayers1 = new ArrayList<>();

			for (int layer : layerIDs) {
				newLayers1.add(layer);
				newMarkers.addAll(LayerManager.getMarkers(layer));
			}

			MapManager.layers = newLayers1;
			setMarkers(newMarkers);
			updateLayers(position, () -> progressDialog.hide());

		}).start();
    }

    private static void setMarkers(List<MarkerModel> markers) {

        allMarkers = markers;

        if (markers.size() > 0) {
            heatmapMode = false;
            heatmapTileProvider = new HeatmapTileProvider.Builder()
                    .data(CollectionUtils.collect(allMarkers, input -> new LatLng(
							input.getPosition().getLatitude(),
							input.getPosition().getLongitude())))
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

    public static void updateLayers(final CameraPosition position, Runnable callback) {

		if (map == null) {
			// TODO log
			return;
		}

        final List<MarkerModel> filteredMarkers = getClosestMarkers(allMarkers, position);

        if (filteredMarkers.size() <= MARKER_MAX_DISPLAY_COUNT) {

            heatmapMode = false;
            UI.run(() -> {
				map.clear();
				for (MarkerModel marker : filteredMarkers) {
					map.addMarker(new MarkerOptions()
							.icon(BitmapDescriptorFactory.defaultMarker(getLayerHue(marker.getLayer())))
							.position(new LatLng(marker.getPosition().getLatitude(),
									marker.getPosition().getLongitude()))
							.title(marker.getName())
							.snippet(marker.getText()));
				}
			});

        } else if (!heatmapMode) {

            heatmapMode = true;
            UI.run(() -> {
				map.clear();
				map.addTileOverlay(new TileOverlayOptions().tileProvider(heatmapTileProvider));
			});
        } // else do nothing, heatmap persists on camera change

        if (callback != null)
            UI.run(callback);
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

    public static List<MarkerModel> getNearbyMarkers(final Position position, int count) {

        if (allMarkers == null || position == null)
            return Collections.emptyList();

        List<MarkerModel> topN = new ArrayList<>(allMarkers);
        Collections.sort(topN, (lhs, rhs) -> lhs.getPosition().distanceTo(position).compareTo(
				rhs.getPosition().distanceTo(position)));
        return topN.subList(0, Math.min(count, topN.size()));
    }

    private static Position toPosition(CameraPosition cameraPosition) {
        if (cameraPosition == null || cameraPosition.target == null)
            return null;

        return new Position(cameraPosition.target.latitude, cameraPosition.target.longitude);
    }

    private static int getScreenWidth(double latitude, double zoom) {

        final double SCREEN_SIZE = AndroidUtils.pixelsToDip(context, Math.max(
                context.getResources().getDisplayMetrics().widthPixels,
                context.getResources().getDisplayMetrics().heightPixels));
        final double EQUATOR_LENGTH = 40075004; // meters

        double pixelSize = (EQUATOR_LENGTH * Math.cos(Math.toRadians(latitude)))
                / Math.pow(2, zoom + 8); // 256 * 2^z
        return (int) (SCREEN_SIZE * pixelSize);
    }

    private static float getLayerHue(int layer) {
		switch (layer) {
			case 1:
				return BitmapDescriptorFactory.HUE_RED;
			case 2:
				return BitmapDescriptorFactory.HUE_ORANGE;
			case 3:
				return BitmapDescriptorFactory.HUE_YELLOW;
			case 4:
				return BitmapDescriptorFactory.HUE_GREEN;
			case 5:
				return BitmapDescriptorFactory.HUE_BLUE;
			default:
				return BitmapDescriptorFactory.HUE_VIOLET;
		}
	}
}

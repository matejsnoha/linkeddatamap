package info.snoha.matej.linkeddatamap.app.internal.map;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import info.snoha.matej.linkeddatamap.Log;
import info.snoha.matej.linkeddatamap.app.internal.layers.LayerManager;
import info.snoha.matej.linkeddatamap.app.internal.model.BoundingBox;
import org.apache.commons.collections4.CollectionUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import info.snoha.matej.linkeddatamap.app.internal.model.MarkerModel;
import info.snoha.matej.linkeddatamap.app.internal.model.Position;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.app.gui.utils.UI;
import info.snoha.matej.linkeddatamap.app.internal.utils.AndroidUtils;

public class MapManager {

    public static final int MARKER_DISTANCE_SCREENS = 1; // number of screens away from center
    public static final int MARKER_MIN_DISTANCE_METERS = 100; // always show markers this far
    public static final int MARKER_MAX_DISPLAY_COUNT = 300; // switch to heatmap if more
	public static final int MARKER_LOAD_TIMEOUT = 60; // seconds

    private static Context context;
    private static GoogleMap map;

    private static volatile List<Integer> visibleLayers = Collections.emptyList();
	private static volatile List<MarkerModel> visibleMarkers;

    private static volatile TileProvider heatmapTileProvider;
    private static volatile boolean heatmapMode;

	public static void with(Context context) {
		MapManager.context = context;
	}

    public static void with(Context context, GoogleMap map) {
        MapManager.context = context;
        MapManager.map = map;
    }

    public static List<Integer> getVisibleLayers() {
        return visibleLayers;
    }

    public static void setDataLayers(final CameraPosition position, List<Integer> layers) {
        setDataLayers(position, layers.toArray(new Integer[0]));
    }

    public static void setDataLayers(final CameraPosition cameraPosition, final Integer... layerIDs) {

		if (map == null || cameraPosition == null) {
			Log.warn("Map not initialized yet");
			return;
		}

        final MaterialDialog progressDialog = new MaterialDialog.Builder(context)
                .title("Please wait ...")
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .cancelable(false)
                .show();

		map.clear();

		visibleLayers = Arrays.asList(layerIDs);

        if (layerIDs.length == 0 || (layerIDs.length == 1 && layerIDs[0] == LayerManager.LAYER_NONE)) {

            setVisibleMarkers(Collections.emptyList());
            progressDialog.hide();
            return;

        }

        new Thread(() -> updateMarkersOnMap(cameraPosition, progressDialog::hide)).start();
    }

    private static void setVisibleMarkers(List<MarkerModel> markers) {

        visibleMarkers = markers;

        if (markers.size() > 0) {
            heatmapMode = false;
            heatmapTileProvider = new HeatmapTileProvider.Builder()
                    .data(CollectionUtils.collect(visibleMarkers, marker -> new LatLng(
							marker.getPosition().getLatitude(),
							marker.getPosition().getLongitude())))
                    .opacity(0.5)
                    .gradient(new Gradient(
                            new int[] {
                                context.getResources().getColor(R.color.primaryDark)},
                            new float[]{
                                0.01f}))
                    .build();
        }
    }

    public static void updateMarkersOnMap(CameraPosition cameraPosition) {
        updateMarkersOnMap(cameraPosition, null);
    }

    public static synchronized void updateMarkersOnMap(final CameraPosition cameraPosition, Runnable callback) {

		if (map == null || cameraPosition == null) {
			Log.warn("Map not initialized yet");
			return;
		}

		// fetch markers in camera range
		Position center = new Position(cameraPosition);
		int range = Math.max(MARKER_MIN_DISTANCE_METERS,
				getScreenWidth(center.getLatitude(), cameraPosition.zoom) * MARKER_DISTANCE_SCREENS);

		BoundingBox geoLimits = BoundingBox.from(center, range);

		CountDownLatch doneSignal = new CountDownLatch(visibleLayers.size());
		List<MarkerModel> newVisibleMarkers = new ArrayList<>();

		for (int layerId : visibleLayers) {

			LayerManager.getMarkers(layerId, geoLimits, new LayerManager.Callback() {

				@Override
				public void onSuccess(List<MarkerModel> markers) {
					synchronized (newVisibleMarkers) {
						newVisibleMarkers.addAll(markers);
					}
					doneSignal.countDown();
				}

				@Override
				public void onFailure(String reason) {
					// logged on underlying levels
					doneSignal.countDown();
				}
			});
		}

		try {
			doneSignal.await(MARKER_LOAD_TIMEOUT, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Log.warn("Interrupted wait for marker load", e);
		}

		synchronized (newVisibleMarkers) { // in case of timeout above

			Log.info("[Layers " + visibleLayers + "] Showing " + newVisibleMarkers.size() + " markers up to "
					+ new DecimalFormat("#.#").format(range / 1000f) + "km away from " + center);

			setVisibleMarkers(newVisibleMarkers);

			if (newVisibleMarkers.size() <= MARKER_MAX_DISPLAY_COUNT) {

				heatmapMode = false;
				UI.run(() -> {
					map.clear();
					for (MarkerModel marker : newVisibleMarkers) {
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

		}

        if (callback != null) {
			UI.run(callback);
		}
    }

    public static List<MarkerModel> getSortedClosestMarkers(final Position position, int count) {

		// TODO look further than currently visible on map
        if (visibleMarkers == null || position == null)
            return Collections.emptyList();

        List<MarkerModel> markersByDistance = new ArrayList<>(visibleMarkers);
        Collections.sort(markersByDistance, (lhs, rhs) -> lhs.getPosition().distanceTo(position).compareTo(
				rhs.getPosition().distanceTo(position)));
        return markersByDistance.subList(0, Math.min(count, markersByDistance.size()));
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

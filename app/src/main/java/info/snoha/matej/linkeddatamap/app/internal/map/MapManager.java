package info.snoha.matej.linkeddatamap.app.internal.map;

import android.content.Context;
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
import info.snoha.matej.linkeddatamap.app.gui.utils.UI;
import info.snoha.matej.linkeddatamap.app.internal.layers.Layer;
import info.snoha.matej.linkeddatamap.app.internal.layers.LayerDatabase;
import info.snoha.matej.linkeddatamap.app.internal.model.BoundingBox;
import info.snoha.matej.linkeddatamap.app.internal.model.MarkerModel;
import info.snoha.matej.linkeddatamap.app.internal.model.Position;
import info.snoha.matej.linkeddatamap.app.internal.utils.AndroidUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static info.snoha.matej.linkeddatamap.Utils.formatDistance;
import static info.snoha.matej.linkeddatamap.Utils.formatDuration;

public class MapManager {

    public static final float MARKER_PRELOAD_SCREENS = 1.5f; // number of screens away from center
	public static final float MARKER_SHOW_SCREENS = 0.75f; // number of screens away from center
    public static final int MARKER_MIN_DISTANCE_METERS = 100; // always show markers this far
    public static final int MARKER_MAX_DISPLAY_COUNT = 128; // switch to heatmap if more
	public static final int MARKER_LOAD_TIMEOUT = 180; // seconds

    private static Context context;
    private static GoogleMap map;
    private static Runnable showProgress;
	private static Runnable hideProgress;

    private static List<Layer> visibleLayers = Collections.emptyList();
	private static List<MarkerModel> preloadedMarkers;

    private static boolean heatmapMode;

    private static CameraPosition lastUpdatedPosition;
	private static CameraPosition currentPosition;

    public static void with(Context context, GoogleMap map, Runnable showProgress, Runnable hideProgress) {
        MapManager.context = context;
        MapManager.map = map;
        MapManager.showProgress = showProgress;
        MapManager.hideProgress = hideProgress;
    }

    public static List<Layer> getVisibleLayers() {
        return Collections.unmodifiableList(visibleLayers);
    }

    public static void setDataLayers(final CameraPosition cameraPosition, List<Layer> layers) {

		if (map == null || cameraPosition == null) {
			Log.warn("Map not initialized yet");
			return;
		}

		UI.run(map::clear);

		if (layers == null) {
			layers = Collections.emptyList();
		}
		visibleLayers = layers;

        if (layers.isEmpty()) {
            setPreloadedMarkers(Collections.emptyList());
            return;
        }

        new Thread(() -> updateMarkersOnMap(cameraPosition, true)).start();
    }

    private static void setPreloadedMarkers(List<MarkerModel> markers) {

        preloadedMarkers = markers;
    }

    public static void updateMarkersOnMap(CameraPosition position) {
        updateMarkersOnMap(position, null, false);
    }

	public static void updateMarkersOnMap(CameraPosition position, boolean layersChanged) {
		updateMarkersOnMap(position, null, layersChanged);
	}

    public static synchronized void updateMarkersOnMap(CameraPosition position,
													   Runnable callback, boolean layersChanged) {

		if (map == null || position == null) {

			// not initialized or no change
			Log.debug("Map not initialized yet");
			if (callback != null) {
				UI.run(callback);
			}
			return;

		} else if (!shouldUpdateMarkers(position) && !layersChanged) {

			// not updating here, just marking as current so we don't have to recheck it again
			currentPosition = position;
			drawMarkersInCameraRange(position, preloadedMarkers); // heatmap <-> markers if needed
			if (callback != null) {
				UI.run(callback);
			}
			return;
		}

		// first position or a move to a large enough distance
		lastUpdatedPosition = position;
		currentPosition = position;
		Log.debug("Updating markers on map");

		long startTime = System.currentTimeMillis();
		UI.run(showProgress);

		// fetch markers in camera range
		Position center = new Position(position);
		int range = Math.max(MARKER_MIN_DISTANCE_METERS, (int) (getVisibleRange(position) * MARKER_PRELOAD_SCREENS));

		BoundingBox geoLimits = BoundingBox.from(center, range);

		CountDownLatch doneSignal = new CountDownLatch(visibleLayers.size());
		List<MarkerModel> newPreloadedMarkers = new ArrayList<>();

		for (Layer layer : visibleLayers) {

			LayerDatabase.getMarkers(layer, geoLimits, new LayerDatabase.Callback() {

				@Override
				public void onSuccess(List<MarkerModel> markers) {
					synchronized (newPreloadedMarkers) {
						newPreloadedMarkers.addAll(markers);
					}
					doneSignal.countDown();
				}

				@Override
				public void onFailure(String reason) {
					// logged on underlying levels
					doneSignal.countDown();
					UI.message(context, reason);
				}
			});
		}

		try {
			doneSignal.await(MARKER_LOAD_TIMEOUT, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Log.warn("Interrupted wait for marker parse", e);
		}

		synchronized (newPreloadedMarkers) { // in case of timeout above

			Log.info("[Layers " + visibleLayers + "] Preloaded " + newPreloadedMarkers.size() + " markers up to "
					+ formatDistance(range) + " away from " + center);

			setPreloadedMarkers(newPreloadedMarkers);

			drawMarkersInCameraRange(position, newPreloadedMarkers, true);

			UI.messageShort(context, "Loaded " + newPreloadedMarkers.size() + " markers in "
					+ formatDuration(System.currentTimeMillis() - startTime));
			UI.run(hideProgress);
		}

        if (callback != null) {
			UI.run(callback);
		}
    }

	private static void drawMarkersInCameraRange(CameraPosition position, List<MarkerModel> markers) {
		drawMarkersInCameraRange(position, markers, false);
	}

    private static void drawMarkersInCameraRange(CameraPosition position, List<MarkerModel> markers,
												 boolean layersChangedOrUpdated) {

    	if (markers == null || markers.isEmpty()) {
    		UI.run(map::clear);
    		return;
		}

		Position center = new Position(position);
		int range = Math.max(MARKER_MIN_DISTANCE_METERS, (int) (getVisibleRange(position) * MARKER_SHOW_SCREENS));

		Collection<MarkerModel> markersToRender = CollectionUtils.select(markers,
				marker -> center.distanceTo(marker.getPosition()) <= range); // TODO check lat/long instead of circle

		if (markersToRender.size() <= MARKER_MAX_DISPLAY_COUNT) {

			heatmapMode = false;
			UI.run(() -> {
				map.clear();
				for (MarkerModel marker : markersToRender) {
					map.addMarker(new MarkerOptions()
							.icon(BitmapDescriptorFactory.defaultMarker(marker.getLayer().getColorHue()))
							.position(new LatLng(marker.getPosition().getLatitude(),
									marker.getPosition().getLongitude()))
							.title(marker.getName())
							.snippet(marker.getText()));
				}
			});

		} else if (!heatmapMode || layersChangedOrUpdated) { // all preloaded markers are shown in heatmap mode

			heatmapMode = true;

			List<TileProvider> heatmapTileProviders = new ArrayList<>();

			for (Layer layer : visibleLayers) {

				Collection<MarkerModel> markersInLayer = CollectionUtils.select(markers,
						marker -> marker.getLayer() == layer);

				if (!markersInLayer.isEmpty()) {

					heatmapTileProviders.add(new HeatmapTileProvider.Builder()
							.data(CollectionUtils.collect(markersInLayer,
									marker -> new LatLng(
											marker.getPosition().getLatitude(),
											marker.getPosition().getLongitude())))
							.opacity(0.5)
							.gradient(new Gradient(
									new int[]{
											layer.getColorAndroid()},
									new float[]{
											0.01f}))
							.build());
				}
			}

			UI.run(() -> {
				map.clear();
				for (TileProvider heatmapTileProvider : heatmapTileProviders) {
					map.addTileOverlay(new TileOverlayOptions().tileProvider(heatmapTileProvider));
				}
			});

		} else {

			// do nothing, heatmap persists on camera change until layersChangedOrUpdated == true
			// Log.debug("Not redrawing markers");
		}
	}

    public static List<MarkerModel> getSortedClosestMarkers(final Position position, int count) {

		// TODO look further than currently visible on map
        if (preloadedMarkers == null || position == null)
            return Collections.emptyList();

        List<MarkerModel> markersByDistance = new ArrayList<>(preloadedMarkers);
        Collections.sort(markersByDistance, (lhs, rhs) -> lhs.getPosition().distanceTo(position).compareTo(
				rhs.getPosition().distanceTo(position)));
        return markersByDistance.subList(0, Math.min(count, markersByDistance.size()));
    }

    private static int getVisibleRange(CameraPosition position) {

        final double SCREEN_SIZE = AndroidUtils.pixelsToDip(context, Math.max(
                context.getResources().getDisplayMetrics().widthPixels,
                context.getResources().getDisplayMetrics().heightPixels));
        final double EQUATOR_LENGTH = 40075004; // meters

        double pixelSize = (EQUATOR_LENGTH * Math.cos(Math.toRadians(position.target.latitude)))
                / Math.pow(2, position.zoom + 8); // 256 * 2^z
        return (int) (SCREEN_SIZE * pixelSize);
    }

    private static boolean shouldUpdateMarkers(CameraPosition position) {
		if (position == null
				|| ObjectUtils.equals(lastUpdatedPosition, position)
				|| ObjectUtils.equals(currentPosition, position)) {

			return false;
		} else if (lastUpdatedPosition == null) {
			return true;
		}

		double preloadedRange = getVisibleRange(lastUpdatedPosition) * MARKER_PRELOAD_SCREENS;
		double visibleRange = getVisibleRange(position);

		double distance = new Position(position).distanceTo(new Position(lastUpdatedPosition));

		boolean shouldUpdate = (distance > preloadedRange / 2) || (visibleRange > preloadedRange);

		Log.debug("Moved " + formatDistance(distance)
				+ ", visible " + formatDistance(visibleRange)
				+ ", preloaded " + formatDistance(preloadedRange)
				+ ", -> " + (shouldUpdate ? "" : "not ") + "updating");

		return shouldUpdate; // TODO
	}
}

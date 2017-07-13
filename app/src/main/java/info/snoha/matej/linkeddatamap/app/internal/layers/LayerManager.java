package info.snoha.matej.linkeddatamap.app.internal.layers;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import info.snoha.matej.linkeddatamap.Log;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.app.internal.model.MarkerModel;
import info.snoha.matej.linkeddatamap.app.internal.model.Position;
import info.snoha.matej.linkeddatamap.app.internal.net.SparqlClient;
import info.snoha.matej.linkeddatamap.app.internal.utils.AndroidUtils;

import static info.snoha.matej.linkeddatamap.app.internal.utils.AndroidUtils.getBooleanPreferenceValue;
import static info.snoha.matej.linkeddatamap.app.internal.utils.AndroidUtils.getStringPreferenceValue;
import static info.snoha.matej.linkeddatamap.app.internal.utils.AndroidUtils.setBooleanPreferenceValue;
import static info.snoha.matej.linkeddatamap.app.internal.utils.AndroidUtils.setStringPreferenceValue;

public class LayerManager {

    public static final int LAYER_COUNT = 6;

    public static final int LAYER_NONE = 0;

    private static Context context;

    public static void with(Context context) {
        LayerManager.context = context;

        // first run init
        if (!getBooleanPreferenceValue(context, "initialized")) {

			setBooleanPreferenceValue(context, "pref_maplayer_1_enabled", true);
			setStringPreferenceValue(context, "pref_maplayer_1_name", "DoubleShot");
			setStringPreferenceValue(context, "pref_maplayer_1_definition",
					AndroidUtils.readRawResource(context, R.raw.maplayer_doubleshot));

        	setBooleanPreferenceValue(context, "pref_datalayer_1_enabled", true);
			setStringPreferenceValue(context, "pref_datalayer_1_name", "DoubleShot");
			setStringPreferenceValue(context, "pref_datalayer_1_definition",
					AndroidUtils.readRawResource(context, R.raw.datalayer_doubleshot));

        	setBooleanPreferenceValue(context, "initialized", true);
		}
    }

    public static List<String> getDataLayerNames(boolean onlyEnabled) {
        List<String> names = new ArrayList<>(LAYER_COUNT);
        for (int i = 1; i <= LAYER_COUNT; i++) {
            if (!onlyEnabled || getBooleanPreferenceValue(context, "pref_datalayer_" + i + "_enabled")) {
                names.add(getStringPreferenceValue(context, "pref_datalayer_" + i + "_name"));
            }
        }
        return names;
    }

    public static List<Integer> getDataLayerIDs(boolean onlyEnabled) {
        List<Integer> ids = new ArrayList<>(LAYER_COUNT);
        for (int i = 1; i <= LAYER_COUNT; i++) {
            if (!onlyEnabled || getBooleanPreferenceValue(context, "pref_datalayer_" + i + "_enabled")) {
                ids.add(i);
            }
        }
        return ids;
    }

    public static List<Integer> getDataLayerIDs(List<String> layerNames) {
        List<String> allLayerNames = getDataLayerNames(false);
        List<Integer> ids = new ArrayList<>(layerNames.size());
        for (String name : layerNames) {
            ids.add(allLayerNames.indexOf(name) + 1);
        }
        return ids;
    }

    public static int getLayerID(String layerName) {
        return getDataLayerNames(false).indexOf(layerName) + 1;
    }

    public static String getLayerName(int layerID) {
        return getDataLayerNames(false).get(layerID - 1);
    }

    public static List<MarkerModel> getMarkers(int layerID) {
        switch (layerID) {
            case LAYER_NONE:
                return Collections.emptyList();
            default:
                return getLayerMarkers(layerID);
        }
    }

    private static List<MarkerModel> getLayerMarkers(int layerID) {

        final List<MarkerModel> markers = new ArrayList<>();

        // TODO data and map layer pairing in Settings
		// TODO move to managers?
		String dataLayerDefinition = getStringPreferenceValue(
				context, "pref_datalayer_" + layerID + "_definition");
        String mapLayerDefinition = getStringPreferenceValue(
        		context, "pref_maplayer_" + layerID + "_definition");

		DataLayer dataLayer = DataLayerManager.load(dataLayerDefinition);
		Log.debug("Data Layer " + layerID + ":");
		Log.debug(dataLayer);

        MapLayer mapLayer = MapLayerManager.load(mapLayerDefinition);
        Log.debug("Map Layer " + layerID + ":");
		Log.debug(mapLayer);

        SparqlClient.getLayer(context, dataLayer, mapLayer, false, (SparqlClient.ListResultCallback) content -> {

			for (List<String> row : content) {
				try {
					Position pos = new Position(row.get(0), row.get(1));
					String name = row.get(2);
					String description = "";
					// TODO description
//					for (int i = 3; i < row.size(); i++) {
//						description += description.isEmpty() ? row.get(i) : "\n\n" + row.get(i);
//					}
					markers.add(new MarkerModel(layerID, pos, name, description));
				} catch (Exception e) {
				}
			}
		});

        return markers;
    }
}

package info.snoha.matej.linkeddatamap.app.internal.layers;

import android.content.Context;
import info.snoha.matej.linkeddatamap.Log;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.app.gui.utils.UI;
import info.snoha.matej.linkeddatamap.app.internal.model.BoundingBox;
import info.snoha.matej.linkeddatamap.app.internal.model.MarkerModel;
import info.snoha.matej.linkeddatamap.app.internal.model.Position;
import info.snoha.matej.linkeddatamap.app.internal.sparql.CsvSparqlClient;
import info.snoha.matej.linkeddatamap.app.internal.sparql.LayerQueryBuilder;
import info.snoha.matej.linkeddatamap.app.internal.utils.AndroidUtils;

import java.util.ArrayList;
import java.util.List;

import static info.snoha.matej.linkeddatamap.app.internal.utils.AndroidUtils.getBooleanPreferenceValue;
import static info.snoha.matej.linkeddatamap.app.internal.utils.AndroidUtils.getStringPreferenceValue;
import static info.snoha.matej.linkeddatamap.app.internal.utils.AndroidUtils.setBooleanPreferenceValue;
import static info.snoha.matej.linkeddatamap.app.internal.utils.AndroidUtils.setStringPreferenceValue;

public class LayerManager {

    public static final int LAYER_COUNT = 6;
    public static final int LAYER_NONE = 0;

	public interface Callback {

		void onSuccess(List<MarkerModel> markers);

		void onFailure(String reason);
	}

    private static Context context;

    public static void with(Context context) {
        LayerManager.context = context;

        // first run init
        //if (!getBooleanPreferenceValue(context, "initialized")) {

			setBooleanPreferenceValue(context, "pref_maplayer_1_enabled", true);
			setStringPreferenceValue(context, "pref_maplayer_1_name", "DoubleShot");
			setStringPreferenceValue(context, "pref_maplayer_1_definition",
					AndroidUtils.readRawResource(context, R.raw.maplayer_doubleshot));

        	setBooleanPreferenceValue(context, "pref_datalayer_1_enabled", true);
			setStringPreferenceValue(context, "pref_datalayer_1_name", "DoubleShot");
			setStringPreferenceValue(context, "pref_datalayer_1_definition",
					AndroidUtils.readRawResource(context, R.raw.datalayer_doubleshot));

			setBooleanPreferenceValue(context, "pref_maplayer_2_enabled", true);
			setStringPreferenceValue(context, "pref_maplayer_2_name", "RUIAN (old)");
			setStringPreferenceValue(context, "pref_maplayer_2_definition",
					AndroidUtils.readRawResource(context, R.raw.maplayer_ruian_old));

			setBooleanPreferenceValue(context, "pref_datalayer_2_enabled", true);
			setStringPreferenceValue(context, "pref_datalayer_2_name", "RUIAN (old)");
			setStringPreferenceValue(context, "pref_datalayer_2_definition",
					AndroidUtils.readRawResource(context, R.raw.datalayer_ruian_old));

			setBooleanPreferenceValue(context, "pref_maplayer_3_enabled", true);
			setStringPreferenceValue(context, "pref_maplayer_3_name", "RUIAN");
			setStringPreferenceValue(context, "pref_maplayer_3_definition",
					AndroidUtils.readRawResource(context, R.raw.maplayer_ruian));

			setBooleanPreferenceValue(context, "pref_datalayer_3_enabled", true);
			setStringPreferenceValue(context, "pref_datalayer_3_name", "RUIAN");
			setStringPreferenceValue(context, "pref_datalayer_3_definition",
					AndroidUtils.readRawResource(context, R.raw.datalayer_ruian));

        	setBooleanPreferenceValue(context, "initialized", true);
		//}
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

    public static void getMarkers(int layerID, BoundingBox geoLimits, Callback callback) {
        if (layerID == LAYER_NONE) {
			callback.onFailure("No layer selected");
			return;
		}

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

		String endpointUrl = mapLayer.getSparqlEndpoint();
		String query = LayerQueryBuilder.query(context, dataLayer, mapLayer, geoLimits);

		CsvSparqlClient.execute(endpointUrl, query, new CsvSparqlClient.Callback() {

			@Override
			public void onSuccess(List<String> columns, List<List<String>> results) {
				List<MarkerModel> markers = new ArrayList<>();
				for (List<String> row : results) {
					try {
						Position pos = new Position(row.get(0), row.get(1));
						String name = row.get(2);
						String description = row.size() >= 4 ? row.get(3) : "";
						markers.add(new MarkerModel(layerID, pos, name, description));
					} catch (Exception e) {
						Log.debug("Could not parse row: " + row, e);
					}
				}
				callback.onSuccess(markers);
			}

			@Override
			public void onFailure(String reason) {
				UI.message(context, "Could not load layer:\n" + reason);
				callback.onFailure(reason);
			}
		});
    }
}

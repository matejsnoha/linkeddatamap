package info.snoha.matej.linkeddatamap.app.internal.layers;

import android.content.Context;
import com.google.gson.Gson;
import info.snoha.matej.linkeddatamap.Log;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.app.gui.utils.UI;
import info.snoha.matej.linkeddatamap.app.internal.model.BoundingBox;
import info.snoha.matej.linkeddatamap.app.internal.model.MarkerModel;
import info.snoha.matej.linkeddatamap.app.internal.model.Position;
import info.snoha.matej.linkeddatamap.sparql.CsvSparqlClient;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static info.snoha.matej.linkeddatamap.app.internal.utils.AndroidUtils.getBooleanPreferenceValue;
import static info.snoha.matej.linkeddatamap.app.internal.utils.AndroidUtils.getStringPreferenceValue;
import static info.snoha.matej.linkeddatamap.app.internal.utils.AndroidUtils.readRawResource;
import static info.snoha.matej.linkeddatamap.app.internal.utils.AndroidUtils.setBooleanPreferenceValue;
import static info.snoha.matej.linkeddatamap.app.internal.utils.AndroidUtils.setStringPreferenceValue;

public class LayerDatabase {

	private static final String PREF_KEY_LAYERS = "layers";

	private static Context context;

	private static final Map<String, Layer> layers = new LinkedHashMap<>();

	public interface Callback {

		void onSuccess(List<MarkerModel> markers);

		void onFailure(String reason);
	}

    public static void with(Context context) {
        LayerDatabase.context = context;
		load();
    }

	public static void load() {
		try {
			initIfEmpty();
			String serializedLayers = getStringPreferenceValue(context, PREF_KEY_LAYERS);
			List<Layer> storedLayers = new Gson().fromJson(serializedLayers, List.class);
			layers.clear();
			if (storedLayers != null) {
				for (Layer layer : storedLayers) {
					layers.put(layer.getUri(), layer);
				}
			}
		} catch (Exception e) {
			Log.warn("Could not load layers", e);
		}
	}

	public static void save() {
		try {
			String serializedLayers = new Gson().toJson(layers);
			setStringPreferenceValue(context, PREF_KEY_LAYERS, serializedLayers);
		} catch (Exception e) {
			Log.warn("Could not save layers", e);
		}
	}

	public static Collection<Layer> getLayers() {
		return Collections.unmodifiableCollection(layers.values());
	}

	public static Collection<Layer> getEnabledLayers() {
		return CollectionUtils.select(layers.values(), Layer::isEnabled);
	}

	public static Collection<String> getLayerNames() {
		return CollectionUtils.collect(layers.values(), Layer::getTitle);
	}

	public static Collection<String> getLayerUris() {
		return CollectionUtils.collect(layers.values(), Layer::getUri);
	}

	public static Layer getLayer(String uri) {
		return layers.get(uri);
	}

	public static Layer getLayerByName(String name) { // TODO what about duplicate names?
		return IterableUtils.find(layers.values(), (layer) -> layer.getTitle().equals(name));
	}

	public static Layer addLayer(String layerDefinition) {
		Layer layer = new LayerManager().load(layerDefinition);
		if (layer != null) {
			layers.put(layer.getUri(), layer);
			save();
		} else {
			Log.warn("Layer not added to database");
		}
		return layer;
	}

    public static void getMarkers(Layer layer, BoundingBox geoLimits, Callback callback) {
        if (layer == null) {
			callback.onFailure("No layer selected");
			return;
		}

		String endpointUrl = layer.getSparqlEndpoint(); // TODO federated queries
		String query = LayerQueryBuilder.query(context, layer, geoLimits);

		if (endpointUrl == null || query == null) {
			callback.onFailure("Invalid endpoint or built query");
			return;
		}

		CsvSparqlClient.execute(endpointUrl, query, new CsvSparqlClient.CSVCallback() {

			@Override
			public void onSuccess(List<String> columns, List<List<String>> results) {
				List<MarkerModel> markers = new ArrayList<>();
				for (List<String> row : results) {
					try {
						Position pos = new Position(row.get(0), row.get(1));
						String name = row.get(2);
						String description = row.size() >= 4 ? row.get(3) : "";
						markers.add(new MarkerModel(layer, pos, name, description));
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

    private static void initIfEmpty() {

		//if (!getBooleanPreferenceValue(context, "initialized")) {

			Log.info("Loading example layers");

			addLayer(readRawResource(context, R.raw.layer_doubleshot));
			addLayer(readRawResource(context, R.raw.layer_ruian));

			setBooleanPreferenceValue(context, "initialized", true);
		//}
	}
}

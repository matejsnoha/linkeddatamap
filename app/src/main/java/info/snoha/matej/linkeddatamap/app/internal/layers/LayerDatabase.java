package info.snoha.matej.linkeddatamap.app.internal.layers;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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

    public static synchronized void with(Context context) {
        LayerDatabase.context = context;
		load();
    }

	public static synchronized void load() {
		try {
			String serializedLayers = getStringPreferenceValue(context, PREF_KEY_LAYERS);
			List<Layer> storedLayers = new Gson().fromJson(serializedLayers, new TypeToken<List<Layer>>(){}.getType());
			layers.clear();
			if (storedLayers != null) {
				for (Layer layer : storedLayers) {
					layers.put(layer.getUri(), layer);
				}
			}
		} catch (Exception e) {
			Log.warn("Could not load layers", e);
		}
		initIfEmpty();
	}

	public static synchronized void save() {
		try {
			String serializedLayers = new Gson().toJson(new ArrayList<>(layers.values()));
			setStringPreferenceValue(context, PREF_KEY_LAYERS, serializedLayers);
		} catch (Exception e) {
			Log.warn("Could not save layers", e);
		}
	}

	public static synchronized Collection<Layer> getLayers() {
		return Collections.unmodifiableCollection(layers.values());
	}

	public static synchronized Collection<Layer> getEnabledLayers() {
		return CollectionUtils.select(layers.values(), Layer::isEnabled);
	}

	public static synchronized Collection<String> getLayerNames() {
		return CollectionUtils.collect(layers.values(), Layer::getTitle);
	}

	public static synchronized Collection<String> getLayerUris() {
		return CollectionUtils.collect(layers.values(), Layer::getUri);
	}

	public static synchronized Layer getLayer(String uri) {
		return layers.get(uri);
	}

	public static synchronized Layer getLayerByName(String name) { // TODO what about duplicate names?
		return IterableUtils.find(layers.values(), (layer) -> layer.getTitle().equals(name));
	}

	public static synchronized Layer addLayer(String layerDefinition) {
		Layer layer = new LayerParser().parse(layerDefinition);
		if (layer != null && layer.isValid()) {
			layers.put(layer.getUri(), layer);
			save();
		} else {
			Log.warn("Layer not added to database");
		}
		return layer;
	}

	public static synchronized void removeLayer(Layer layer) {
		if (layer != null) {
			layers.remove(layer.getUri());
			save();
		} else {
			Log.warn("Layer not removed from database");
		}
	}

    private static void initIfEmpty() {

		if (!getBooleanPreferenceValue(context, "initialized") || layers.isEmpty()) {

			Log.info("Loading example layers");

			addLayer(readRawResource(context, R.raw.layer_doubleshot));
			addLayer(readRawResource(context, R.raw.layer_ruian));

			setBooleanPreferenceValue(context, "initialized", true);
		}
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
}

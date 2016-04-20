package info.snoha.matej.linkeddatamap.internal.map;

import android.app.Activity;
import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import info.snoha.matej.linkeddatamap.internal.model.MarkerModel;
import info.snoha.matej.linkeddatamap.internal.model.Position;
import info.snoha.matej.linkeddatamap.internal.net.SparqlClient;
import info.snoha.matej.linkeddatamap.internal.utils.Utils;

public class LayerManager {

    public static final int LAYER_COUNT = 5;

    public static final int LAYER_NONE = 0;

    private static Context context;

    public static void with(Context context) {
        LayerManager.context = context;
    }

    public static List<String> getLayerNames(boolean onlyEnabled) {
        List<String> names = new ArrayList<>(LAYER_COUNT);
        for (int i = 1; i <= LAYER_COUNT; i++) {
            if (!onlyEnabled || Utils.getBooleanPreferenceValue(context, "pref_layer_" + i + "_enabled")) {
                names.add(Utils.getStringPreferenceValue(context, "pref_layer_" + i + "_name"));
            }
        }
        return names;
    }

    public static List<Integer> getLayerIDs(boolean onlyEnabled) {
        List<Integer> ids = new ArrayList<>(LAYER_COUNT);
        for (int i = 1; i <= LAYER_COUNT; i++) {
            if (!onlyEnabled || Utils.getBooleanPreferenceValue(context, "pref_layer_" + i + "_enabled")) {
                ids.add(i);
            }
        }
        return ids;
    }

    public static List<Integer> getLayerIDs(List<String> layerNames) {
        List<String> allLayerNames = getLayerNames(false);
        List<Integer> ids = new ArrayList<>(layerNames.size());
        for (String name : layerNames) {
            ids.add(allLayerNames.indexOf(name) + 1);
        }
        return ids;
    }

    public static int getLayerID(String layerName) {
        return getLayerNames(false).indexOf(layerName) + 1;
    }

    public static String getLayerName(int layerID) {
        return getLayerNames(false).get(layerID - 1);
    }

    public static List<MarkerModel> getMarkers(int layerID) {
        switch (layerID) {
            case LAYER_NONE:
                return Collections.emptyList();
            default:
                return getCustomLayerMarkers(layerID);
        }
    }

    public static List<MarkerModel> getCustomLayerMarkers(int layerID) {

        final List<MarkerModel> markers = new ArrayList<>();

        SparqlClient.getLayer((Activity) context, layerID, false, new SparqlClient.ListResultCallback() {
            @Override
            public void run(List<List<String>> content) {

                for (List<String> row : content) {
                    try {
                        Position pos = new Position(row.get(0), row.get(1));
                        String name = row.get(2);
                        String descr = "";
                        for (int i = 3; i < row.size(); i++) {
                            descr += descr.isEmpty() ? row.get(i) : "\n\n" + row.get(i);
                        }
                        markers.add(new MarkerModel(pos, name, descr));
                    } catch (Exception e) {
                    }
                }
            }
        });

        return markers;
    }
}

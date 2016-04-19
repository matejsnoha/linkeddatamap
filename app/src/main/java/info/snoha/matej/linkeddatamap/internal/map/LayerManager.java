package info.snoha.matej.linkeddatamap.internal.map;

import android.app.Activity;
import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import info.snoha.matej.linkeddatamap.internal.model.MarkerModel;
import info.snoha.matej.linkeddatamap.internal.model.Position;
import info.snoha.matej.linkeddatamap.internal.net.SparqlClient;
import info.snoha.matej.linkeddatamap.internal.utils.Utils;
import info.snoha.matej.linkeddatamap.internal.model.DoubleShot;
import info.snoha.matej.linkeddatamap.internal.model.Ruian;

public class LayerManager {

    public static final int LAYER_COUNT = 5;

    public static final int LAYER_NONE = 0;

    public static final int LAYER_RUIAN_OFFLINE = 1;
    public static final int LAYER_DOUBLE_SHOT_OFFLINE = 2;

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
            case LAYER_RUIAN_OFFLINE:
                return getRuianMarkers();
            case LAYER_DOUBLE_SHOT_OFFLINE:
                return getDoubleShotMarkers();
            default:
                return getCustomLayerMarkers(layerID);
        }
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
        Map<String, String> map = Ruian.getPlaceToObjectMapping(context);
        for (Ruian.SimplePlace place : Ruian.getPlaces(context)) {
            markers.add(new MarkerModel(new Position(place.latitude, place.longitude),
                    place.name, place.address
                    + "\n\nPlace: <" + map.get(place.url) + ">"));
        }
        return markers;
    }

    public static List<MarkerModel> getCustomLayerMarkers(int layerID) {

        final List<MarkerModel> markers = new ArrayList<>();

        SparqlClient.getLayer((Activity) context, layerID, false, new SparqlClient.ListResultCallback() {
            @Override
            public void run(List<List<String>> content) {

                for (List<String> line : content) {
                    try {
                        Position pos = new Position(line.get(0), line.get(1));
                        String name = line.get(2);
                        String descr = "";
                        for (int i = 3; i < line.size(); i++) {
                            descr += descr.isEmpty() ? line.get(i) : ", " + line.get(i);
                        }
                        markers.add(new MarkerModel(pos, name, descr.replace("\"", "")));
                    } catch (Exception e) {
                    }
                }
            }
        });

        return markers;
    }
}

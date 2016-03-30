package info.snoha.matej.linkeddatamap;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class LayerManager {

    public static final int LAYER_COUNT = 5;

    public static final int LAYER_NONE = -1;

    public static final int LAYER_RUIAN_OFFLINE = 0;
    public static final int LAYER_DOUBLE_SHOT_OFFLINE = 1;

    private static Context context;

    public static void with(Context context) {
        LayerManager.context = context;
    }

    public static List<String> getLayerNames(boolean onlyEnabled) {
        List<String> names = new ArrayList<>(LAYER_COUNT);
        for (int i = 1; i <= LAYER_COUNT; i++) {
            names.add(Utils.getPreferenceValue(context, "pref_layer_" + i + "_name"));
        }
        return names;
    }

    public static int getLayerID(String name) {
        return getLayerNames(false).indexOf(name);
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
                return Collections.emptyList();
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
}

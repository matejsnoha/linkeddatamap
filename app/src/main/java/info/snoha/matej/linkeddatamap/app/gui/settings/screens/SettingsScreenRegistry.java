package info.snoha.matej.linkeddatamap.app.gui.settings.screens;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

public class SettingsScreenRegistry {

    private static final Map<String, AbstractSettingsScreen> screens = new HashMap<>();

    public static void init(Context context) {
        add(GeneralSettingsScreen.class.getSimpleName(), new GeneralSettingsScreen(context));
        add(MapSettingsScreen.class.getSimpleName(), new MapSettingsScreen(context));
        add(MapLayersSettingsScreen.class.getSimpleName(), new MapLayersSettingsScreen(context));
        add(DataLayersSettingsScreen.class.getSimpleName(), new DataLayersSettingsScreen(context));
    }

    public static void add(String name, AbstractSettingsScreen screen) {
        screens.put(name, screen);
    }

    public static AbstractSettingsScreen get(String name) {
        return screens.get(name);
    }

    public static boolean contains(String name) {
        return screens.containsKey(name);
    }
}

package info.snoha.matej.linkeddatamap.app.gui.settings.screens;

import android.content.Context;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.app.gui.settings.items.LocationPermissionSettingsItem;

public class MapSettingsScreen extends AbstractSettingsScreen {

    public MapSettingsScreen(Context context) {
        super(context);
        addSettingsItems(
                new LocationPermissionSettingsItem(context)
        );
    }

    @Override
    public int getTitleResource() {
        return R.string.map_settings;
    }
}

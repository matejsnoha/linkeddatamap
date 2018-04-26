package info.snoha.matej.linkeddatamap.app.gui.settings.screens;

import android.content.Context;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.app.gui.settings.items.SubscreenSettingsItem;

public class GeneralSettingsScreen extends AbstractSettingsScreen {

    public GeneralSettingsScreen(Context context) {
        super(context);
        addSettingsItems(
                new SubscreenSettingsItem(context, R.raw.map, R.string.map_settings, null,
                        MapSettingsScreen.class.getSimpleName()),
                new SubscreenSettingsItem(context, R.raw.layers, R.string.layers, null,
                        LayersSettingsScreen.class.getSimpleName())
        );
    }

    @Override
    public int getTitleResource() {
        return R.string.settings;
    }
}

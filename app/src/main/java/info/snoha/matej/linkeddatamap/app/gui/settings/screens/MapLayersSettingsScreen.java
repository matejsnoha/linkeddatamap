package info.snoha.matej.linkeddatamap.app.gui.settings.screens;

import android.content.Context;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.app.gui.settings.items.MapLayerSubscreenSettingsItem;
import info.snoha.matej.linkeddatamap.app.internal.layers.LayerManager;

public class MapLayersSettingsScreen extends AbstractSettingsScreen {

    public MapLayersSettingsScreen(Context context) {
        super(context);
        for (int i = 1; i < LayerManager.LAYER_COUNT; i++) {
            SettingsScreenRegistry.add(MapLayerDetailSettingsScreen.class.getSimpleName() + i,
                    new MapLayerDetailSettingsScreen(context, i));
            addSettingsItems(new MapLayerSubscreenSettingsItem(context, i));
        }
    }

    @Override
    public int getTitleResource() {
        return R.string.map_layers;
    }
}

package info.snoha.matej.linkeddatamap.app.gui.settings.screens;

import android.content.Context;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.app.gui.settings.items.DataLayerSubscreenSettingsItem;
import info.snoha.matej.linkeddatamap.app.internal.layers.LayerManager;

public class DataLayersSettingsScreen extends AbstractSettingsScreen {

    public DataLayersSettingsScreen(Context context) {
        super(context);
        for (int i = 1; i < LayerManager.LAYER_COUNT; i++) {
            SettingsScreenRegistry.add(DataLayerDetailSettingsScreen.class.getSimpleName() + i,
                    new DataLayerDetailSettingsScreen(context, i));
            addSettingsItems(new DataLayerSubscreenSettingsItem(context, i));
        }
    }

    @Override
    public int getTitleResource() {
        return R.string.data_layers;
    }
}

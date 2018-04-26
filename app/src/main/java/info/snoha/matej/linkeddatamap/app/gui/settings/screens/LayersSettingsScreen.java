package info.snoha.matej.linkeddatamap.app.gui.settings.screens;

import android.content.Context;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.app.gui.settings.items.DataLayerSubscreenSettingsItem;
import info.snoha.matej.linkeddatamap.app.internal.layers.Layer;
import info.snoha.matej.linkeddatamap.app.internal.layers.LayerDatabase;

public class LayersSettingsScreen extends AbstractSettingsScreen {

    public LayersSettingsScreen(Context context) {
        super(context);
        for (Layer layer : LayerDatabase.getLayers()) {
            SettingsScreenRegistry.add(LayerDetailSettingsScreen.class.getSimpleName() + " " + layer.getUri(),
                    new LayerDetailSettingsScreen(context, layer));
            addSettingsItems(new DataLayerSubscreenSettingsItem(context, layer));
        }
    }

    @Override
    public int getTitleResource() {
        return R.string.data_layers;
    }
}

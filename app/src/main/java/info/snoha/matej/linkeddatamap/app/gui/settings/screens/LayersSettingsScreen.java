package info.snoha.matej.linkeddatamap.app.gui.settings.screens;

import android.content.Context;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.app.gui.settings.items.LayerSubscreenSettingsItem;
import info.snoha.matej.linkeddatamap.app.gui.settings.items.SubscreenSettingsItem;
import info.snoha.matej.linkeddatamap.app.internal.layers.Layer;
import info.snoha.matej.linkeddatamap.app.internal.layers.LayerDatabase;

public class LayersSettingsScreen extends AbstractSettingsScreen {

    public LayersSettingsScreen(Context context) {
        super(context);
        refreshSettingsItems();
    }

    @Override
    public int getTitleResource() {
        return R.string.layers;
    }

    @Override
    public void refresh() {
        refreshSettingsItems();
        super.refresh();
    }

    private void refreshSettingsItems() {
        removeAllSettingsItems();
        for (Layer layer : LayerDatabase.getLayers()) {
            SettingsScreenRegistry.add(
                    LayerSettingsScreen.getScreenName(layer),
                    new LayerSettingsScreen(getContext(), layer));
            addSettingsItems(new LayerSubscreenSettingsItem(getContext(), layer));
        }
        addSettingsItems(new SubscreenSettingsItem(getContext(), R.raw.layers, R.string.add_layer, null,
                AddLayerSettingsScreen.class.getSimpleName()));
    }
}

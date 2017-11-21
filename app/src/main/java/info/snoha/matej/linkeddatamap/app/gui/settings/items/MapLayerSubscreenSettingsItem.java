package info.snoha.matej.linkeddatamap.app.gui.settings.items;

import android.content.Context;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.app.gui.settings.screens.MapLayerDetailSettingsScreen;
import info.snoha.matej.linkeddatamap.app.internal.layers.LayerManager;

public class MapLayerSubscreenSettingsItem extends SubscreenSettingsItem {

    private final int layer;

    public MapLayerSubscreenSettingsItem(Context context, int layer) {
        super(context, R.raw.layers, "Map layer " + layer, LayerManager.getMapLayerName(layer),
                MapLayerDetailSettingsScreen.class.getSimpleName() + layer);
        this.layer = layer;
    }

    @Override
    public void refreshSummary() {
        summary = LayerManager.getMapLayerName(layer);
        super.refreshSummary();
    }
}

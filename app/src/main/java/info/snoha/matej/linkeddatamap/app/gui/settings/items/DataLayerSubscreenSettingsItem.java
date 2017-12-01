package info.snoha.matej.linkeddatamap.app.gui.settings.items;

import android.content.Context;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.app.gui.settings.screens.DataLayerDetailSettingsScreen;
import info.snoha.matej.linkeddatamap.app.internal.layers.LocalLayerManager;
import info.snoha.matej.linkeddatamap.app.internal.map.MapManager;

public class DataLayerSubscreenSettingsItem extends SubscreenSettingsItem {

    private final int layer;

    public DataLayerSubscreenSettingsItem(Context context, int layer) {
        super(context, R.raw.layers, "Data layer " + layer, LocalLayerManager.getDataLayerName(layer),
                DataLayerDetailSettingsScreen.class.getSimpleName() + layer);
        this.layer = layer;
    }

    @Override
    public int getIconColor() {
        return MapManager.getLayerColor(layer);
    }

    @Override
    public void refreshSummary() {
        summary = LocalLayerManager.getDataLayerName(layer);
        super.refreshSummary();
    }
}

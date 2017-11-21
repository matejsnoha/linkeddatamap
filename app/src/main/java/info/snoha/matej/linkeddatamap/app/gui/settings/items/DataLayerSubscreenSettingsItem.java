package info.snoha.matej.linkeddatamap.app.gui.settings.items;

import android.content.Context;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.app.gui.settings.screens.DataLayerDetailSettingsScreen;
import info.snoha.matej.linkeddatamap.app.gui.settings.screens.MapLayerDetailSettingsScreen;
import info.snoha.matej.linkeddatamap.app.gui.utils.UI;
import info.snoha.matej.linkeddatamap.app.internal.layers.LayerManager;
import info.snoha.matej.linkeddatamap.app.internal.map.MapManager;
import info.snoha.matej.linkeddatamap.app.internal.utils.AndroidUtils;

public class DataLayerSubscreenSettingsItem extends SubscreenSettingsItem {

    private final int layer;

    public DataLayerSubscreenSettingsItem(Context context, int layer) {
        super(context, R.raw.layers, "Data layer " + layer, LayerManager.getDataLayerName(layer),
                DataLayerDetailSettingsScreen.class.getSimpleName() + layer);
        this.layer = layer;
    }

    @Override
    public int getIconColor() {
        return MapManager.getLayerColor(layer);
    }

    @Override
    public void refreshSummary() {
        summary = LayerManager.getDataLayerName(layer);
        super.refreshSummary();
    }
}

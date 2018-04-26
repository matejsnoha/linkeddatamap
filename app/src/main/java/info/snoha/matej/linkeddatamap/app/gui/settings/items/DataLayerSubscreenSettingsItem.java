package info.snoha.matej.linkeddatamap.app.gui.settings.items;

import android.content.Context;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.app.gui.settings.screens.LayerDetailSettingsScreen;
import info.snoha.matej.linkeddatamap.app.internal.layers.Layer;

public class DataLayerSubscreenSettingsItem extends SubscreenSettingsItem {

    private final Layer layer;

    public DataLayerSubscreenSettingsItem(Context context, Layer layer) {
        super(context, R.raw.layers, "Data layer " + layer, layer.getTitle(),
                LayerDetailSettingsScreen.class.getSimpleName() + layer);
        this.layer = layer;
    }

    @Override
    public int getIconColor() {
        return layer.getColorAndroid();
    }

    @Override
    public void refreshSummary() {
        summary = layer.getDescription();
        super.refreshSummary();
    }
}

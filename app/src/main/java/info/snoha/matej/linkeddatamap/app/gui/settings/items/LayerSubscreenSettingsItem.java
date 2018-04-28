package info.snoha.matej.linkeddatamap.app.gui.settings.items;

import android.content.Context;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.app.gui.settings.screens.LayerSettingsScreen;
import info.snoha.matej.linkeddatamap.app.internal.layers.Layer;

public class LayerSubscreenSettingsItem extends SubscreenSettingsItem {

    private final Layer layer;

    public LayerSubscreenSettingsItem(Context context, Layer layer) {
        super(context, R.raw.layers, layer.getTitle(), layer.getDescription(),
                LayerSettingsScreen.getName(layer));
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

    @Override
    public void refreshIcon() {
        icon = layer.isEnabled() ? R.raw.layers : R.raw.layers_off;
        super.refreshIcon();
    }
}

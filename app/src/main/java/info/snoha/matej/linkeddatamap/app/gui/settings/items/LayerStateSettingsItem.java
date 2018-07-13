package info.snoha.matej.linkeddatamap.app.gui.settings.items;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import info.snoha.matej.linkeddatamap.Log;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.app.internal.layers.Layer;
import info.snoha.matej.linkeddatamap.app.internal.layers.LayerDatabase;
import info.snoha.matej.linkeddatamap.app.internal.map.MapManager;

public class LayerStateSettingsItem extends AbstractSettingsItem {

    private Layer layer;

    public LayerStateSettingsItem(Context context, Layer layer) {
        super(context);
        this.layer = layer;
    }

    @Override
    public int getIcon() {
        return layer.isEnabled() ? R.raw.layers : R.raw.layers_off;
    }

    @Override
    public int getIconColor() {
        try {
            return Color.parseColor(layer.getColor());
        } catch (Exception e) {
            Log.warn("Unknown color " + layer.getColor());
            return Color.DKGRAY;
        }
    }

    @Override
    public int getTitleResource() {
        return R.string.state;
    }

    @Override
    public String getSummary() {
        return getContext().getResources().getString(layer.isEnabled()
                ? R.string.enabled
                : R.string.disabled);
    }

    @Override
    public void onClick(View view) {
        layer.enabled(!layer.isEnabled());
        LayerDatabase.save();
        if (!layer.isEnabled()) {
            MapManager.hideLayer(layer);
        }
        refreshIcon();
        refreshSummary();
    }
}

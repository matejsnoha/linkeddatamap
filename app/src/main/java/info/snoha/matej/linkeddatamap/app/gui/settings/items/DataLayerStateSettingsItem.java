package info.snoha.matej.linkeddatamap.app.gui.settings.items;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import info.snoha.matej.linkeddatamap.Log;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.app.internal.layers.Layer;
import info.snoha.matej.linkeddatamap.app.internal.layers.LayerDatabase;

public class DataLayerStateSettingsItem extends AbstractSettingsItem {

    private Layer layer;

    public DataLayerStateSettingsItem(Context context, Layer layer) {
        super(context);
        this.layer = layer;
    }

    @Override
    public int getIcon() {
        return R.raw.layers;
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
        refreshSummary();
    }
}

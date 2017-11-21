package info.snoha.matej.linkeddatamap.app.gui.settings.items;

import android.content.Context;
import android.view.View;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.app.internal.layers.LayerManager;
import info.snoha.matej.linkeddatamap.app.internal.map.MapManager;

public class DataLayerStateSettingsItem extends AbstractSettingsItem {

    private int layer;

    public DataLayerStateSettingsItem(Context context, int layer) {
        super(context);
        this.layer = layer;
    }

    @Override
    public int getIcon() {
        return R.raw.layers;
    }

    @Override
    public int getIconColor() {
        return MapManager.getLayerColor(layer);
    }

    @Override
    public int getTitleResource() {
        return R.string.state;
    }

    @Override
    public String getSummary() {
        return getContext().getResources().getString(LayerManager.isDataLayerEnabled(layer)
                ? R.string.enabled
                : R.string.disabled);
    }

    @Override
    public void onClick(View view) {
        LayerManager.setDataLayerEnabled(layer, !LayerManager.isDataLayerEnabled(layer));
        refreshSummary();
    }
}

package info.snoha.matej.linkeddatamap.app.gui.settings.items;

import android.content.Context;
import android.view.View;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.app.internal.layers.LocalLayerManager;

public class MapLayerStateSettingsItem extends AbstractSettingsItem {

    private int layer;

    public MapLayerStateSettingsItem(Context context, int layer) {
        super(context);
        this.layer = layer;
    }

    @Override
    public int getIcon() {
        return R.raw.layers;
    }

    @Override
    public int getTitleResource() {
        return R.string.state;
    }

    @Override
    public String getSummary() {
        return getContext().getResources().getString(LocalLayerManager.isMapLayerEnabled(layer)
                ? R.string.enabled
                : R.string.disabled);
    }

    @Override
    public void onClick(View view) {
        LocalLayerManager.setMapLayerEnabled(layer, !LocalLayerManager.isMapLayerEnabled(layer));
        refreshSummary();
    }
}

package info.snoha.matej.linkeddatamap.app.gui.settings.screens;

import android.content.Context;
import info.snoha.matej.linkeddatamap.app.gui.settings.items.MapLayerDefinitionSettingsItem;
import info.snoha.matej.linkeddatamap.app.gui.settings.items.MapLayerNameSettingsItem;
import info.snoha.matej.linkeddatamap.app.gui.settings.items.MapLayerStateSettingsItem;
import info.snoha.matej.linkeddatamap.app.internal.layers.LayerManager;

public class MapLayerDetailSettingsScreen extends AbstractSettingsScreen {

    private int layer;

    public MapLayerDetailSettingsScreen(Context context, int layer) {
        super(context);
        this.layer = layer;
        addSettingsItems(
                // new MapLayerStateSettingsItem(context, layer),
                new MapLayerNameSettingsItem(context, layer),
                new MapLayerDefinitionSettingsItem(context, layer)
        );
    }

    @Override
    public String getTitleString() {
        return LayerManager.getLayerName(layer);
    }
}

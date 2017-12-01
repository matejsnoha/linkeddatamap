package info.snoha.matej.linkeddatamap.app.gui.settings.screens;

import android.content.Context;
import info.snoha.matej.linkeddatamap.app.gui.settings.items.DataLayerCloudSettingsItem;
import info.snoha.matej.linkeddatamap.app.gui.settings.items.DataLayerDefinitionSettingsItem;
import info.snoha.matej.linkeddatamap.app.gui.settings.items.DataLayerStateSettingsItem;
import info.snoha.matej.linkeddatamap.app.gui.settings.items.DataLayerNameSettingsItem;
import info.snoha.matej.linkeddatamap.app.internal.layers.LocalLayerManager;

public class DataLayerDetailSettingsScreen extends AbstractSettingsScreen {

    private int layer;

    public DataLayerDetailSettingsScreen(Context context, int layer) {
        super(context);
        this.layer = layer;
        addSettingsItems(
                new DataLayerStateSettingsItem(context, layer),
                new DataLayerNameSettingsItem(context, layer),
                new DataLayerDefinitionSettingsItem(context, layer),
                new DataLayerCloudSettingsItem(context, layer)
        );
    }

    @Override
    public String getTitleString() {
        return LocalLayerManager.getDataLayerName(layer);
    }
}

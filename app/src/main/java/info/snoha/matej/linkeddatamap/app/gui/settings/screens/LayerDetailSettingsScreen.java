package info.snoha.matej.linkeddatamap.app.gui.settings.screens;

import android.content.Context;
import info.snoha.matej.linkeddatamap.app.gui.settings.items.DataLayerCloudSettingsItem;
import info.snoha.matej.linkeddatamap.app.gui.settings.items.DataLayerDefinitionSettingsItem;
import info.snoha.matej.linkeddatamap.app.gui.settings.items.DataLayerStateSettingsItem;
import info.snoha.matej.linkeddatamap.app.gui.settings.items.DataLayerNameSettingsItem;
import info.snoha.matej.linkeddatamap.app.internal.layers.Layer;

public class LayerDetailSettingsScreen extends AbstractSettingsScreen {

    private Layer layer;

    public LayerDetailSettingsScreen(Context context, Layer layer) {
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
        return layer.getTitle();
    }
}

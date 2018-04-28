package info.snoha.matej.linkeddatamap.app.gui.settings.screens;

import android.content.Context;
import info.snoha.matej.linkeddatamap.app.gui.settings.items.LayerCloudSettingsItem;
import info.snoha.matej.linkeddatamap.app.gui.settings.items.LayerDeleteSettingsItem;
import info.snoha.matej.linkeddatamap.app.gui.settings.items.LayerDescriptionSettingsItem;
import info.snoha.matej.linkeddatamap.app.gui.settings.items.LayerStateSettingsItem;
import info.snoha.matej.linkeddatamap.app.gui.settings.items.LayerNameSettingsItem;
import info.snoha.matej.linkeddatamap.app.internal.layers.Layer;

public class LayerSettingsScreen extends AbstractSettingsScreen {

    private Layer layer;

    public LayerSettingsScreen(Context context, Layer layer) {
        super(context);
        this.layer = layer;
        addSettingsItems(
                new LayerStateSettingsItem(context, layer),
                new LayerNameSettingsItem(context, layer),
                new LayerDescriptionSettingsItem(context, layer),
                new LayerCloudSettingsItem(context, layer),
				new LayerDeleteSettingsItem(context, layer)
        );
    }

    @Override
    public String getTitleString() {
        return layer.getTitle();
    }

    public static String getName(Layer layer) {
        if (layer != null) {
            return LayerSettingsScreen.class.getSimpleName() + " " + layer.getUri();
        } else {
            return null;
        }
    }
}

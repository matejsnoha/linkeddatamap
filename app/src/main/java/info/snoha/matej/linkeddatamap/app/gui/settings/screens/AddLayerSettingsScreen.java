package info.snoha.matej.linkeddatamap.app.gui.settings.screens;

import android.content.Context;
import info.snoha.matej.linkeddatamap.FrameworkConfiguration;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.app.gui.settings.items.AddLayerCloudSettingsItem;
import info.snoha.matej.linkeddatamap.app.gui.settings.items.AddLayerLinkSettingsItem;
import info.snoha.matej.linkeddatamap.app.gui.settings.items.AddLayerTextSettingsItem;

public class AddLayerSettingsScreen extends AbstractSettingsScreen {

    public AddLayerSettingsScreen(Context context) {
        super(context);
        if (FrameworkConfiguration.ADD_LAYER_CLOUD_ENABLED) {
            addSettingsItems(new AddLayerCloudSettingsItem(context));
        }
        if (FrameworkConfiguration.ADD_LAYER_LINK_ENABLED) {
            addSettingsItems(new AddLayerLinkSettingsItem(context));
        }
        if (FrameworkConfiguration.ADD_LAYER_TEXT_ENABLED) {
            addSettingsItems(new AddLayerTextSettingsItem(context));
        }
    }

    @Override
    public int getTitleResource() {
        return R.string.add_layer;
    }
}

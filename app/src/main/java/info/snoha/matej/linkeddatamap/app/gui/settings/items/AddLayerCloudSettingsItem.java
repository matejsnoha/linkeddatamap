package info.snoha.matej.linkeddatamap.app.gui.settings.items;

import android.content.Context;
import android.view.View;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.app.gui.utils.LayerLoaderUI;

public class AddLayerCloudSettingsItem extends AbstractSettingsItem {

    public AddLayerCloudSettingsItem(Context context) {
        super(context);
    }

    @Override
    public int getIcon() {
        return R.raw.cloud_download;
    }

    @Override
    public int getTitleResource() {
        return R.string.add_from_cloud;
    }

    @Override
    public String getSummary() {
        return null;
    }

    @Override
    public void onClick(View view) {
        LayerLoaderUI.loadFromCloud(getContext());
    }
}

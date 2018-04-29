package info.snoha.matej.linkeddatamap.app.gui.settings.items;

import android.content.Context;
import android.view.View;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.app.gui.utils.UI;

public class AddLayerLinkSettingsItem extends AbstractSettingsItem {

    public AddLayerLinkSettingsItem(Context context) {
        super(context);
    }

    @Override
    public int getIcon() {
        return R.raw.download;
    }

    @Override
    public int getTitleResource() {
        return R.string.add_from_link;
    }

    @Override
    public String getSummary() {
        return null;
    }

    @Override
    public void onClick(View view) {
        UI.message(getContext(), "Not implemented yet");
    }
}

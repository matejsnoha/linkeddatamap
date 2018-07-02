package info.snoha.matej.linkeddatamap.app.gui.settings.items;

import android.content.Context;
import android.view.View;
import com.afollestad.materialdialogs.MaterialDialog;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.app.gui.activities.SettingsActivity;
import info.snoha.matej.linkeddatamap.app.gui.settings.screens.LayerSettingsScreen;
import info.snoha.matej.linkeddatamap.app.gui.settings.screens.LayersSettingsScreen;
import info.snoha.matej.linkeddatamap.app.gui.settings.screens.SettingsScreenRegistry;
import info.snoha.matej.linkeddatamap.app.internal.layers.Layer;
import info.snoha.matej.linkeddatamap.app.internal.layers.LayerDatabase;
import info.snoha.matej.linkeddatamap.app.internal.map.MapManager;

public class LayerDeleteSettingsItem extends AbstractSettingsItem {

    private Layer layer;

    public LayerDeleteSettingsItem(Context context, Layer layer) {
        super(context);
        this.layer = layer;
    }

    @Override
    public int getIcon() {
        return R.raw.delete;
    }

    @Override
    public int getIconColor() {
        return layer.getColorAndroid();
    }

    @Override
    public int getTitleResource() {
        return R.string.delete_layer;
    }

    @Override
    public String getSummary() {
        return null;
    }

    @Override
    public void onClick(View view) {

        new MaterialDialog.Builder(getContext())
                .title(getResources().getString(R.string.delete_layer) + "?")
                .positiveText(R.string.ok)
                .neutralText(R.string.cancel)
                .onPositive((dialog, which) -> {
                    LayerDatabase.removeLayer(layer);
                    MapManager.removeLayer(layer);
                    if (getContext() instanceof SettingsActivity) {
                        SettingsScreenRegistry.remove(LayerSettingsScreen.getScreenName(layer));
                        SettingsScreenRegistry.get(LayersSettingsScreen.class.getSimpleName()).refresh();
                        ((SettingsActivity) getContext()).onBackPressed();
                    }
                })
                .show();
    }
}

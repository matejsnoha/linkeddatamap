package info.snoha.matej.linkeddatamap.app.gui.settings.items;

import android.content.Context;
import android.view.View;
import com.afollestad.materialdialogs.MaterialDialog;
import info.snoha.matej.linkeddatamap.Log;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.api.Layers;
import info.snoha.matej.linkeddatamap.app.gui.settings.screens.MapLayerDetailSettingsScreen;
import info.snoha.matej.linkeddatamap.app.gui.settings.screens.SettingsScreenRegistry;
import info.snoha.matej.linkeddatamap.app.gui.utils.UI;
import info.snoha.matej.linkeddatamap.app.internal.layers.LocalLayerManager;

import java.util.List;

public class MapLayerCloudSettingsItem extends AbstractSettingsItem {

    private int layer;

    public MapLayerCloudSettingsItem(Context context, int layer) {
        super(context);
        this.layer = layer;
    }

    @Override
    public int getIcon() {
        return R.raw.cloud_download;
    }

    @Override
    public int getTitleResource() {
        return R.string.load_from_cloud;
    }

    @Override
    public String getSummary() {
        return null;
    }

    @Override
    public void onClick(View view) {

        new Thread(() -> {
            try {
                Layers.LayerListResponse response = Layers.getMapLayers();
                if (response == null || !response.isSuccess()) {
                    UI.message(getContext(), "Could not load layers");
                    return;
                }
                List<String> layers = response.layers;
                if (layers == null || layers.isEmpty()) {
                    UI.message(getContext(), "Could not load layers: empty response");
                    return;
                }
                UI.run(() -> {
                    new MaterialDialog.Builder(getContext())
                            .title(R.string.load_from_cloud)
                            .items(layers)
                            .itemsCallbackSingleChoice(-1, (dialog, itemView, which, text) -> {
                                String url = text.toString();
                                loadLayer(url);
                                return true;
                            })
                            .positiveText(R.string.ok)
                            .neutralText(R.string.cancel)
                            .show();
                });
            } catch (Exception e) {
                Log.warn("Could not load layers", e);
                UI.message(getContext(), "Could not load layers: " + e);
            }
        }, "Map layer list").start();
    }

    private void loadLayer(String url) {
        new Thread(() -> {
            try {
                Layers.LayerResponse response = Layers.getLayer(url);
                if (response == null || !response.isSuccess()) {
                    UI.message(getContext(), "Could not load layer");
                    return;
                }
                if (response.layer == null || response.layer.isEmpty()) {
                    UI.message(getContext(), "Could not load layer: empty response");
                    return;
                }
                LocalLayerManager.setMapLayerDefinition(layer, response.layer);
                UI.message(getContext(), "Layer successfully loaded");
                UI.run(() ->
                        SettingsScreenRegistry.get(MapLayerDetailSettingsScreen.class.getSimpleName() + layer)
                                .refreshSummary()
                );
            } catch (Exception e) {
                Log.warn("Could not load layer " + url, e);
                UI.message(getContext(), "Could not load layer: " + e);
            }
        }, "Map layer get").start();
    }
}
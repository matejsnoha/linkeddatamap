package info.snoha.matej.linkeddatamap.app.gui.utils;

import android.content.Context;
import com.afollestad.materialdialogs.MaterialDialog;
import info.snoha.matej.linkeddatamap.Log;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.api.Layers;
import info.snoha.matej.linkeddatamap.app.gui.activities.SettingsActivity;
import info.snoha.matej.linkeddatamap.app.gui.settings.screens.LayerSettingsScreen;
import info.snoha.matej.linkeddatamap.app.gui.settings.screens.LayersSettingsScreen;
import info.snoha.matej.linkeddatamap.app.gui.settings.screens.SettingsScreenRegistry;
import info.snoha.matej.linkeddatamap.app.internal.layers.Layer;
import info.snoha.matej.linkeddatamap.app.internal.layers.LayerDatabase;

import java.util.List;

public class LayerLoaderUI {
	
	public static void loadFromCloud(Context context) {

		new Thread(() -> {
			try {
				Layers.LayerListResponse response = Layers.getLayers();
				if (response == null || !response.isSuccess()) {
					UI.message(context, "Could not load layers");
					return;
				}
				List<String> layers = response.layers;
				if (layers == null || layers.isEmpty()) {
					UI.message(context, "Could not load layers: no layers available");
					return;
				}
				UI.run(() -> {
					new MaterialDialog.Builder(context)
							.title(R.string.add_from_cloud)
							.items(layers)
							.itemsCallbackSingleChoice(-1, (dialog, itemView, which, text) -> {
								String url = text.toString();
								loadFromCloudLink(context, url);
								return true;
							})
							.positiveText(R.string.ok)
							.neutralText(R.string.cancel)
							.show();
				});
			} catch (Exception e) {
				Log.warn("Could not load layers", e);
				UI.message(context, "Could not load layers: " + e);
			}
		}, "List layers from cloud").start();
	}

	private static void loadFromCloudLink(Context context, String cloudUrl) {
		new Thread(() -> {
			try {
				Layers.LayerResponse response = Layers.getLayer(cloudUrl);
				if (response == null || !response.isSuccess()) {
					UI.message(context, "Could not load layer");
					return;
				}
				if (response.layer == null || response.layer.isEmpty()) {
					UI.message(context, "Could not load layer: empty response");
					return;
				}
				Layer layer = LayerDatabase.addLayer(response.layer);
				if (layer != null) {
					UI.message(context, "Layer " + layer.getTitle() + " successfully loaded");
					UI.run(() -> {
						if (context instanceof SettingsActivity) {
							SettingsScreenRegistry.get(LayersSettingsScreen.class.getSimpleName()).refresh();
							((SettingsActivity) context).onBackPressed();
						}
						SettingsScreenRegistry.get(LayerSettingsScreen.getScreenName(layer)).refresh();
					});
				} else {
					UI.message(context, "Could not load layer");
				}
			} catch (Exception e) {
				Log.warn("Could not load layer " + cloudUrl, e);
				UI.message(context, "Could not load layer: " + e);
			}
		}, "Add layer from cloud").start();
	}
}

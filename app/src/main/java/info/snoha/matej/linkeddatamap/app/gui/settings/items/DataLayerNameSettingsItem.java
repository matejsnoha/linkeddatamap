package info.snoha.matej.linkeddatamap.app.gui.settings.items;

import android.content.Context;
import android.text.InputType;
import android.view.View;
import com.afollestad.materialdialogs.MaterialDialog;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.app.internal.layers.LayerManager;
import info.snoha.matej.linkeddatamap.app.internal.map.MapManager;

public class DataLayerNameSettingsItem extends AbstractSettingsItem {

    private int layer;

    public DataLayerNameSettingsItem(Context context, int layer) {
        super(context);
        this.layer = layer;
    }

    @Override
    public int getIcon() {
        return R.raw.layers;
    }

    @Override
    public int getIconColor() {
        return MapManager.getLayerColor(layer);
    }

    @Override
    public int getTitleResource() {
        return R.string.layer_name;
    }

    @Override
    public String getSummary() {
        return LayerManager.getDataLayerName(layer);
    }

    @Override
    public void onClick(View view) {
        new MaterialDialog.Builder(getContext())
                .title(R.string.layer_name)
                .input(null, LayerManager.getDataLayerName(layer), (dialog, input) -> {
                    String name = input.toString().trim();
                    if (!name.isEmpty()) {
                        LayerManager.setDataLayerName(layer, name);
                    }
                    refreshSummary();
                })
                .positiveText(R.string.ok)
                .neutralText(R.string.cancel)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .show();
    }
}

package info.snoha.matej.linkeddatamap.app.gui.settings.items;

import android.content.Context;
import android.text.InputType;
import android.view.View;
import com.afollestad.materialdialogs.MaterialDialog;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.app.internal.layers.Layer;
import info.snoha.matej.linkeddatamap.app.internal.layers.LayerDatabase;

public class LayerNameSettingsItem extends AbstractSettingsItem {

    private Layer layer;

    public LayerNameSettingsItem(Context context, Layer layer) {
        super(context);
        this.layer = layer;
    }

    @Override
    public int getIcon() {
        return R.raw.square_edit_outline;
    }

    @Override
    public int getIconColor() {
        return layer.getColorAndroid();
    }

    @Override
    public int getTitleResource() {
        return R.string.layer_name;
    }

    @Override
    public String getSummary() {
        return layer.getTitle();
    }

    @Override
    public void onClick(View view) {
        new MaterialDialog.Builder(getContext())
                .title(R.string.layer_name)
                .input(null, layer.getTitle(), (dialog, input) -> {
                    String inputStr = input.toString().trim();
                    if (!inputStr.isEmpty()) {
                        layer.title(inputStr);
                        LayerDatabase.save();
                    }
                    refreshSummary();
                })
                .positiveText(R.string.ok)
                .neutralText(R.string.cancel)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .show();
    }
}

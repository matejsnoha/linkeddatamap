package info.snoha.matej.linkeddatamap.app.gui.settings.items;

import android.content.Context;
import android.text.InputType;
import android.view.View;
import com.afollestad.materialdialogs.MaterialDialog;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.app.internal.layers.Layer;
import info.snoha.matej.linkeddatamap.app.internal.layers.LayerDatabase;

public class LayerDescriptionSettingsItem extends AbstractSettingsItem {

    private Layer layer;

    public LayerDescriptionSettingsItem(Context context, Layer layer) {
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
        return R.string.layer_description;
    }

    @Override
    public String getSummary() {
        return layer.getDescription();
    }

    @Override
    public void onClick(View view) {
        new MaterialDialog.Builder(getContext())
                .title(R.string.layer_description)
                .input(null, layer.getDescription(), (dialog, input) -> {
                    String inputStr = input.toString().trim();
                    if (!inputStr.isEmpty()) {
                        layer.description(inputStr);
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

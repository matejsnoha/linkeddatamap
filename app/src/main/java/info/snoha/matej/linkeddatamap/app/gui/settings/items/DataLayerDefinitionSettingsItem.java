package info.snoha.matej.linkeddatamap.app.gui.settings.items;

import android.content.Context;
import android.graphics.Typeface;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import com.afollestad.materialdialogs.MaterialDialog;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.app.internal.layers.LocalLayerManager;
import info.snoha.matej.linkeddatamap.app.internal.map.MapManager;

public class DataLayerDefinitionSettingsItem extends AbstractSettingsItem {

    private int layer;

    public DataLayerDefinitionSettingsItem(Context context, int layer) {
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
        return R.string.definition;
    }

    @Override
    public String getSummary() {
        return !LocalLayerManager.getDataLayerDefinition(layer).isEmpty() ? "OK" : null;
    }

    @Override
    public void onClick(View view) {
        MaterialDialog md = new MaterialDialog.Builder(getContext())
                .title(R.string.definition)
                .input(null, LocalLayerManager.getDataLayerDefinition(layer), (dialog, input) -> {
                    String name = input.toString().trim();
                    if (!name.isEmpty()) {
                        LocalLayerManager.setDataLayerDefinition(layer, name);
                    }
                    refreshSummary();
                })
                .positiveText(R.string.ok)
                .neutralText(R.string.cancel)
                .inputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE)
                .build();

        EditText multilineInput = md.getInputEditText();
        multilineInput.setSingleLine(false);
        multilineInput.setTypeface(Typeface.MONOSPACE);
        multilineInput.setTextSize(10);
        multilineInput.setVerticalScrollBarEnabled(true);
        md.show();
    }
}

package info.snoha.matej.linkeddatamap.app.gui.settings.items;

import android.content.Context;
import android.graphics.Typeface;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import com.afollestad.materialdialogs.MaterialDialog;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.app.internal.layers.LayerManager;

public class MapLayerDefinitionSettingsItem extends AbstractSettingsItem {

    private int layer;

    public MapLayerDefinitionSettingsItem(Context context, int layer) {
        super(context);
        this.layer = layer;
    }

    @Override
    public int getIcon() {
        return R.raw.layers;
    }

    @Override
    public int getTitleResource() {
        return R.string.definition;
    }

    @Override
    public String getSummary() {
        return !LayerManager.getMapLayerDefinition(layer).isEmpty() ? "OK" : null;
    }

    @Override
    public void onClick(View view) {
        MaterialDialog md = new MaterialDialog.Builder(getContext())
                .title(R.string.definition)
                .input(null, LayerManager.getMapLayerDefinition(layer), (dialog, input) -> {
                    String name = input.toString().trim();
                    if (!name.isEmpty()) {
                        LayerManager.setMapLayerDefinition(layer, name);
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

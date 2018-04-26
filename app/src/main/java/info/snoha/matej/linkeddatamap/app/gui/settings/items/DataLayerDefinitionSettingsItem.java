package info.snoha.matej.linkeddatamap.app.gui.settings.items;

import android.content.Context;
import android.view.View;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.app.internal.layers.Layer;

public class DataLayerDefinitionSettingsItem extends AbstractSettingsItem {

    private Layer layer;

    public DataLayerDefinitionSettingsItem(Context context, Layer layer) {
        super(context);
        this.layer = layer;
    }

    @Override
    public int getIcon() {
        return R.raw.layers;
    }

    @Override
    public int getIconColor() {
        return layer.getColorAndroid();
    }

    @Override
    public int getTitleResource() {
        return R.string.definition;
    }

    @Override
    public String getSummary() {
        return layer.getDescription();
    }

    @Override
    public void onClick(View view) {
//        MaterialDialog md = new MaterialDialog.Builder(getContext())
//                .title(R.string.definition)
//                .input(null, LayerDatabase.getDataLayerDefinition(layer), (dialog, input) -> {
//                    String name = input.toString().trim();
//                    if (!name.isEmpty()) {
//                        LayerDatabase.setDataLayerDefinition(layer, name);
//                    }
//                    refreshSummary();
//                })
//                .positiveText(R.string.ok)
//                .neutralText(R.string.cancel)
//                .inputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE)
//                .build();
//
//        EditText multilineInput = md.getInputEditText();
//        multilineInput.setSingleLine(false);
//        multilineInput.setTypeface(Typeface.MONOSPACE);
//        multilineInput.setTextSize(10);
//        multilineInput.setVerticalScrollBarEnabled(true);
//        md.show();
    }
}

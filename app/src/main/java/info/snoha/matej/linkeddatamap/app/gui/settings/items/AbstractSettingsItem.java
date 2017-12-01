package info.snoha.matej.linkeddatamap.app.gui.settings.items;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.widget.LinearLayout;
import info.snoha.matej.linkeddatamap.Log;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.app.gui.utils.UI;
import info.snoha.matej.linkeddatamap.app.gui.views.SVGImageView;
import info.snoha.matej.linkeddatamap.app.internal.utils.AndroidUtils;

public abstract class AbstractSettingsItem extends LinearLayout
        implements View.OnClickListener, View.OnLongClickListener {

    public AbstractSettingsItem(Context context) {
        super(context);

        // delay, subclasses may need to initialize icons, texts in their constructor
        UI.runOnNextLayout(this, () -> {
            try {
                setOnClickListener(this);
                setOnLongClickListener(this);

                inflate(getContext(), R.layout.settings_item, this);

                LinearLayout iconLayout = getIconLayoutView();
                SVGImageView icon = new SVGImageView(getContext()); // cannot inflate in xml, superclass lib is jar in gradle
                icon.setImageResource(getIcon());
                AndroidUtils.changeViewColor(icon, getIconColor());
                iconLayout.addView(icon);

                getNameView().setText(getTitle());
                refreshSummary();
            } catch (Exception e) {
                Log.warn("Settings item could not be initialized", e);
            }
        });
    }

    public int getTitleResource() {
        return 0;
    }

    public String getTitleString() {
        return "";
    }

    public String getTitle() {
        try {
            if (getTitleResource() != 0) {
                return getContext().getResources().getString(getTitleResource());
            } else {
                return getTitleString();
            }
        } catch (Exception e) {
            Log.warn("Could not get settings item title ", e);
            return "...";
        }
    }

    public abstract int getIcon();

    public abstract String getSummary();

    public int getIconColor() {
        return getResources().getColor(R.color.settingsItem);
    }

    @Override
    public abstract void onClick(View view);

    @Override
    public boolean onLongClick(View view) {
        return false;
    }

    public LinearLayout getIconLayoutView() {
        return findViewById(R.id.settings_item_icon_layout);
    }

    public SVGImageView getIconView() {
        return (SVGImageView) getIconLayoutView().getChildAt(0);
    }

    public AppCompatTextView getNameView() {
        return findViewById(R.id.settings_item_name);
    }

    public AppCompatTextView getSummaryView() {
        return findViewById(R.id.settings_item_summary);
    }

    public void refreshSummary() {
        try {
            if (getSummaryView() == null) {
                return;
            }
            String summary = getSummary();
            if (summary != null && !summary.isEmpty()) {
                getSummaryView().setVisibility(VISIBLE);
                getSummaryView().setText(summary);
            } else {
                getSummaryView().setVisibility(GONE);
            }
        } catch (Exception e) {
            Log.warn("Settings item summary could not be refreshed", e);
            getSummaryView().setVisibility(GONE);
        }
    }
}

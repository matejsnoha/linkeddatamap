package info.snoha.matej.linkeddatamap.app.gui.settings.screens;

import android.content.Context;
import android.widget.LinearLayout;
import info.snoha.matej.linkeddatamap.Log;
import info.snoha.matej.linkeddatamap.app.gui.settings.items.AbstractSettingsItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractSettingsScreen extends LinearLayout {

    protected final List<AbstractSettingsItem> settingsItems = new ArrayList<>();

    public AbstractSettingsScreen(Context context) {
        super(context);
        setOrientation(LinearLayout.VERTICAL);
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
            Log.warn("Could not get settings screen title ", e);
            return "...";
        }
    }

    protected void removeAllSettingsItems() {
        settingsItems.clear();
        removeAllViews();
    }

    protected void addSettingsItems(AbstractSettingsItem... items) {
        Collections.addAll(settingsItems, items);
        for (AbstractSettingsItem item : items) {
            addView(item, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        }
    }

    public List<AbstractSettingsItem> getSettingsItems() {
        return settingsItems;
    }

    public void refresh() {
        for (AbstractSettingsItem item : getSettingsItems()) {
            item.refresh();
        }
    }
}

package info.snoha.matej.linkeddatamap.app.gui.settings.items;

import android.content.Context;
import android.view.View;
import info.snoha.matej.linkeddatamap.app.gui.activities.SettingsActivity;

public class SubscreenSettingsItem extends AbstractSettingsItem {

    protected int icon;
    protected int titleResource;
    protected String titleString;
    protected String summary;
    protected String screenName;

    public SubscreenSettingsItem(Context context, int icon, int title, String summary, String screenName) {
        super(context);
        this.icon = icon;
        this.titleResource = title;
        this.titleString = "";
        this.summary = summary;
        this.screenName = screenName;
    }

    public SubscreenSettingsItem(Context context, int icon, String title, String summary, String screenName) {
        super(context);
        this.icon = icon;
        this.titleResource = 0;
        this.titleString = title;
        this.summary = summary;
        this.screenName = screenName;
    }

    @Override
    public int getIcon() {
        return icon;
    }

    @Override
    public int getTitleResource() {
        return titleResource;
    }

    @Override
    public String getTitleString() {
        return titleString;
    }

    @Override
    public String getSummary() {
        return summary;
    }

    @Override
    public void onClick(View view) {
        if (getContext() instanceof SettingsActivity) {
            ((SettingsActivity) getContext()).openSubscreen(screenName);
        }
    }
}

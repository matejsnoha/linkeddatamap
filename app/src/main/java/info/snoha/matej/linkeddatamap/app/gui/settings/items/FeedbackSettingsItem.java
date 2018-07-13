package info.snoha.matej.linkeddatamap.app.gui.settings.items;

import android.content.Context;
import android.view.View;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.FrameworkConfiguration;
import info.snoha.matej.linkeddatamap.app.internal.utils.AndroidUtils;

public class FeedbackSettingsItem extends AbstractSettingsItem {

    public FeedbackSettingsItem(Context context) {
        super(context);
    }

    @Override
    public int getIcon() {
        return R.raw.square_edit_outline;
    }

    @Override
    public int getTitleResource() {
        return R.string.feedback;
    }

    @Override
    public String getSummary() {
        return "Please provide us with feedback about your experience using this mobile application.";
    }

    @Override
    public void onClick(View view) {
		AndroidUtils.openUriInBrowser(getContext(), FrameworkConfiguration.FEEDBACK_URI);
    }
}

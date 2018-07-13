package info.snoha.matej.linkeddatamap.app.gui.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextSwitcher;
import android.widget.TextView;
import info.snoha.matej.linkeddatamap.Log;
import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.app.gui.fragments.SettingsFragment;
import info.snoha.matej.linkeddatamap.app.gui.settings.screens.GeneralSettingsScreen;
import info.snoha.matej.linkeddatamap.app.gui.settings.screens.SettingsScreenRegistry;
import info.snoha.matej.linkeddatamap.app.internal.utils.AndroidUtils;
import org.apache.commons.lang3.ObjectUtils;

public class SettingsActivity extends AppCompatActivity {

    private TextSwitcher titleSwitcher;

    private CharSequence title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.info("Settings Activity UI starting");

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        // init screens
        SettingsScreenRegistry.init(this);

        // show fragment
        SettingsFragment settingsFragment;
        if (savedInstanceState == null) {
            String screenSimpleClassName = GeneralSettingsScreen.class.getSimpleName();
            settingsFragment = SettingsFragment.getInstance(screenSimpleClassName);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.content, settingsFragment, screenSimpleClassName)
                    .commit();
        } else {
            settingsFragment = (SettingsFragment) getSupportFragmentManager()
                    .findFragmentByTag(SettingsActivity.class.getSimpleName());
        }

        // Cross-fading title setup.
        title = getTitle();

        titleSwitcher = new TextSwitcher(this);
        titleSwitcher.setFactory(() -> {
            TextView tv = new AppCompatTextView(this);
            tv.setTextAppearance(tv.getContext(), R.style.TextAppearance_AppCompat_Widget_ActionBar_Title);
            return tv;
        });
        titleSwitcher.setCurrentText(title);

        ActionBar ab = getSupportActionBar();
        ab.setCustomView(titleSwitcher);
        ab.setDisplayShowCustomEnabled(true);
        ab.setDisplayShowTitleEnabled(false);

        // Add to hierarchy before accessing layout params.
        int margin = AndroidUtils.dipToPixels(this, 16);
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) titleSwitcher.getLayoutParams();
        lp.leftMargin = margin;
        lp.rightMargin = margin;

        titleSwitcher.setInAnimation(this, R.anim.abc_fade_in);
        titleSwitcher.setOutAnimation(this, R.anim.abc_fade_out);
    }

    public void openSubscreen(String screenName) {
        SettingsFragment fragment = SettingsFragment.getInstance(screenName);
        getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.content, fragment, screenName)
                .addToBackStack(screenName)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onTitleChanged(CharSequence newTitle, int color) {
        super.onTitleChanged(newTitle, color);
        if (!ObjectUtils.equals(title, newTitle)) {
            title = newTitle;
            titleSwitcher.setText(newTitle);
        }
    }
}
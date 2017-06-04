package info.snoha.matej.linkeddatamap.app.gui.activities;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

import info.snoha.matej.linkeddatamap.R;
import info.snoha.matej.linkeddatamap.app.gui.utils.UI;
import info.snoha.matej.linkeddatamap.app.internal.net.SparqlClient;
import info.snoha.matej.linkeddatamap.app.internal.utils.AndroidUtils;
import info.snoha.matej.linkeddatamap.app.internal.map.LayerManager;


public class SettingsActivity extends AppCompatPreferenceActivity {

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener =
            new Preference.OnPreferenceChangeListener() {

        final int SUMMARY_LENGTH = 100;

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {

                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {

                if (stringValue.length() > SUMMARY_LENGTH) {
                    preference.setSummary(stringValue.substring(0, SUMMARY_LENGTH) + "…");
                } else {
                    preference.setSummary(stringValue);
                }
            }
            return true;
        }
    };

    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {

        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                AndroidUtils.getStringPreferenceValue(preference.getContext(), preference.getKey()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || LayerPreferenceFragment.class.getName().equals(fragmentName)
                || DummyPreferenceFragment.class.getName().equals(fragmentName);
    }

    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference("cache_mode"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    public static class LayerPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_layers);
            setHasOptionsMenu(true);

            for (int i = 1; i <= LayerManager.LAYER_COUNT; i++) {

                final Preference namePreference = findPreference("pref_layer_" + i + "_name");
                bindPreferenceSummaryToValue(namePreference);

                final Preference endpointPreference = findPreference("pref_layer_" + i + "_endpoint");
                bindPreferenceSummaryToValue(endpointPreference);

                final Preference queryPreference = findPreference("pref_layer_" + i + "_query");
                bindPreferenceSummaryToValue(queryPreference);

                final int fi = i;
                findPreference("pref_layer_" + i + "_test").setOnPreferenceClickListener(

                        preference -> {

                            new Thread(() -> SparqlClient.getLayer(getActivity(), fi, true,
                                    (SparqlClient.StringResultCallback) result -> UI.run(() ->
                                            new MaterialDialog.Builder(getActivity())
                                                    .title(AndroidUtils.getStringPreferenceValue(namePreference))
                                                    .content(result)
                                                    .positiveText("Back")
                                                    .show())
                            )).start();
                            return true;
                        }
                );
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    public static class DummyPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}

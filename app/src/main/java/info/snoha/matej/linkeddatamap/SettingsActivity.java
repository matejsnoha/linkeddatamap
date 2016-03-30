package info.snoha.matej.linkeddatamap;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.util.Base64;
import android.view.MenuItem;
import android.webkit.WebView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener =
            new Preference.OnPreferenceChangeListener() {

        final int SUMMARY_LENGTH = 100;

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                if (stringValue.length() > SUMMARY_LENGTH) {
                    preference.setSummary(stringValue.substring(0, SUMMARY_LENGTH) + "…");
                } else {
                    preference.setSummary(stringValue);
                }
            }
            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                Utils.getPreferenceValue(preference.getContext(), preference.getKey()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
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

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || LayerPreferenceFragment.class.getName().equals(fragmentName)
                || DummyPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
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

                findPreference("pref_layer_" + i + "_test").setOnPreferenceClickListener(

                        new Preference.OnPreferenceClickListener() {
                            @Override
                            public boolean onPreferenceClick(Preference preference) {

                                try {

                                    final String url = Utils.getPreferenceValue(endpointPreference).trim();

                                    if (url.isEmpty() || url.equals("http://")) {
                                        Snackbar.make(LayerPreferenceFragment.this.getView(),
                                                "Invalid endpoint URL", Snackbar.LENGTH_LONG).show();
                                        return true;
                                    }

                                    String queryPreferenceValue = Utils.getPreferenceValue(queryPreference).trim();
                                    if (queryPreferenceValue.isEmpty()) {
                                        Snackbar.make(LayerPreferenceFragment.this.getView(),
                                                "Invalid query", Snackbar.LENGTH_LONG).show();
                                        return true;
                                    }

                                    final String postBody = queryPreferenceValue
                                            + (queryPreferenceValue.toLowerCase(Locale.US).contains("limit") ?
                                                "" : "\nLIMIT 100");

                                    Snackbar.make(getView(), "Wait please", Snackbar.LENGTH_LONG).show();

                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {

                                            try {

                                                OkHttpClient client = new OkHttpClient();

                                                RequestBody body = RequestBody.create(
                                                        MediaType.parse("application/sparql-query; charset=utf-8"),
                                                        postBody);
                                                Request request = new Request.Builder()
                                                        .url(url)
                                                        .addHeader("Accept", "text/csv; charset=utf-8")
                                                        .post(body)
                                                        .build();
                                                final Response response = client.newCall(request).execute();
                                                final String content = response.body().string().replace("\n", "\n\n");

                                                getActivity().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {

                                                        new MaterialDialog.Builder(getActivity())
                                                                .title(Utils.getPreferenceValue(namePreference))
                                                                .content(content)
                                                                .positiveText("Back")
                                                                .show();
                                                    }
                                                });

                                            } catch (final Exception e) {

                                                getActivity().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {

                                                        Snackbar.make(getView(), e.toString() + ": " +
                                                                e.getMessage(), Snackbar.LENGTH_LONG).show();
                                                    }
                                                });
                                            }
                                        }
                                    }).start();
                                    return true;

                                } catch (Exception e) {
                                    Snackbar.make(getView(), e.toString() + ": " +
                                            e.getMessage(), Snackbar.LENGTH_LONG).show();
                                    return true;
                                }
                            }
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

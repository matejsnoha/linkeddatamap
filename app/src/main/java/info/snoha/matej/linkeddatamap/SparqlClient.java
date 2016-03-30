package info.snoha.matej.linkeddatamap;

import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SparqlClient {

    public interface ResultCallback {
    }

    public interface StringResultCallback extends ResultCallback {
        void run(String result);
    }

    public interface ListResultCallback extends ResultCallback {
        void run(List<List<String>> result);
    }

    public static void getLayer(final Activity activity, int layerID, boolean addLimit, final ResultCallback callback) {

        final String url = Utils.getStringPreferenceValue(activity,
                "pref_layer_" + layerID + "_endpoint").trim();

        if (url.isEmpty() || url.equals("http://")) {
            Snackbar.make(getSnackView(activity),
                    "Invalid endpoint URL", Snackbar.LENGTH_LONG).show();
            return;
        }

        String queryPreferenceValue = Utils.getStringPreferenceValue(activity,
                "pref_layer_" + layerID + "_query").trim();
        if (queryPreferenceValue.isEmpty()) {
            Snackbar.make(getSnackView(activity),
                    "Invalid query", Snackbar.LENGTH_LONG).show();
            return;
        }

        final String postBody = queryPreferenceValue
                + (!queryPreferenceValue.toLowerCase(Locale.US).contains("limit") ?
                (addLimit ? "\nLIMIT 100" : "\nLIMIT 100000") : "");

        Snackbar.make(getSnackView(activity), "Wait please", Snackbar.LENGTH_LONG).show();

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

            if (callback instanceof StringResultCallback) {

                final String content = response.body().string().replace("\n", "\n\n");
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((StringResultCallback) callback).run(content);
                    }
                });

            } else if (callback instanceof ListResultCallback) {
                final String content = response.body().string();
                final List<List<String>> result = new ArrayList<>();
                for (String line : content.split("\n")) {
                    result.add(Arrays.asList(line.split("\\,")));
                }

                Log.i("SPARQL", result.size() - 1 + " results");

                ((ListResultCallback) callback).run(result);
            }

        } catch (final Exception e) {

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Snackbar.make(getSnackView(activity), e.toString() + ": " +
                            e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            });
        }
    }

    private static View getSnackView(Activity activity) {
        return activity.getWindow().getDecorView().getRootView();
    }
}

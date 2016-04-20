package info.snoha.matej.linkeddatamap.internal.net;

import android.content.Context;
import android.util.Log;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import info.snoha.matej.linkeddatamap.gui.utils.UI;
import info.snoha.matej.linkeddatamap.internal.utils.Utils;
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

    public static void getLayer(Context context, int layerID, boolean addLimit, final ResultCallback callback) {

        final String url = Utils.getStringPreferenceValue(context,
                "pref_layer_" + layerID + "_endpoint").trim();

        if (url.isEmpty() || url.equals("http://")) {
            UI.message(context, "Invalid endpoint URL");
            return;
        }

        String queryPreferenceValue = Utils.getStringPreferenceValue(context,
                "pref_layer_" + layerID + "_query").trim();
        if (queryPreferenceValue.isEmpty()) {
            UI.message(context, "Invalid query");
            return;
        }

        final String postBody = queryPreferenceValue
                + (!queryPreferenceValue.toLowerCase(Locale.US).contains("limit") ?
                (addLimit ? "\nLIMIT 100" : "\nLIMIT 100000") : "");

        UI.message(context, "Please wait");

        try {

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build();

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
                UI.run(new Runnable() {
                    @Override
                    public void run() {
                        ((StringResultCallback) callback).run(content);
                    }
                });

            } else if (callback instanceof ListResultCallback) {

                final List<List<String>> result = new ArrayList<>();
                Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(response.body().charStream());
                for (CSVRecord record : records) {
                    result.add(IteratorUtils.toList(record.iterator()));
                }

                Log.i("SPARQL", result.size() - 1 + " results");

                ((ListResultCallback) callback).run(result);
            }

        } catch (Exception e) {

            UI.message(context, e.toString() + ": " +  e.getMessage());
        }
    }
}

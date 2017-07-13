package info.snoha.matej.linkeddatamap.app.internal.net;

import android.content.Context;
import info.snoha.matej.linkeddatamap.Log;
import info.snoha.matej.linkeddatamap.app.gui.utils.UI;
import info.snoha.matej.linkeddatamap.app.internal.layers.DataLayer;
import info.snoha.matej.linkeddatamap.app.internal.layers.LayerQueryBuilder;
import info.snoha.matej.linkeddatamap.app.internal.layers.MapLayer;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SparqlClient {

    private static final int CONNECT_TIMEOUT = 5_000;
    private static final int DATA_TIMEOUT = 120_000;

    public interface ResultCallback {
    }

    public interface StringResultCallback extends ResultCallback {
        void run(String result);
    }

    public interface ListResultCallback extends ResultCallback {
        void run(List<List<String>> result);
    }

    public static void getLayer(Context context, DataLayer dataLayer, MapLayer mapLayer,
                                boolean addLimit, final ResultCallback callback) {

        try {

            final String url = dataLayer.getSparqlEndpoint(); // TODO what about different urls in layers?

            if (url.isEmpty() || url.equals("http://")) {
                UI.message(context, "Invalid endpoint URL");
                return;
            }

            String query = LayerQueryBuilder.query(context, dataLayer, mapLayer);
            Log.debug("SPARQL Query:");
            Log.debug(query);

            if (query == null || query.isEmpty()) {
                UI.message(context, "Invalid query");
                return;
            }

            final String postBody = query
                    + (!query.toLowerCase(Locale.US).contains("limit") ?
                    (addLimit ? "\nLIMIT 100" : "\nLIMIT 100000") : "");

            UI.message(context, "Please wait");


            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                    .readTimeout(DATA_TIMEOUT, TimeUnit.MILLISECONDS)
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
                UI.run(() -> ((StringResultCallback) callback).run(content)); // TODO why on UI?

            } else if (callback instanceof ListResultCallback) {

                final List<List<String>> result = new ArrayList<>();
                Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(response.body().charStream());
                for (CSVRecord record : records) {
                    result.add(IteratorUtils.toList(record.iterator()));
                }

                Log.info("SPARQL" + (result.size() - 1) + " results");

                ((ListResultCallback) callback).run(result);
            }

        } catch (Exception e) {

            UI.message(context, e.toString() + ": " +  e.getMessage());
        }
    }
}

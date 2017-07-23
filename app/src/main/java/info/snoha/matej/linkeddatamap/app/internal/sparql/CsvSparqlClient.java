package info.snoha.matej.linkeddatamap.app.internal.sparql;

import info.snoha.matej.linkeddatamap.Log;
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

public class CsvSparqlClient {

	private static final int CONNECT_TIMEOUT = 5_000;
	private static final int DATA_TIMEOUT = 120_000;
	private static final int MAX_RESULTS = 100_000;

	public interface Callback {

		void onSuccess(List<String> columns, List<List<String>> results);

		void onFailure(String reason);
	}

	public static void execute(String endpointUrl, String query, final Callback callback) {

		try {

			if (endpointUrl.isEmpty() || endpointUrl.equals("http://")) {
				callback.onFailure("Invalid endpoint URL");
				return;
			}

			if (query == null || query.isEmpty()) {
				callback.onFailure("Invalid query");
				return;
			}

			Log.debug("Sparql query to " + endpointUrl + " :\n" + query);

			final String postBody = query
					+ (!query.toLowerCase(Locale.US).contains("limit") ? "\nLIMIT " + MAX_RESULTS : "");

			OkHttpClient client = new OkHttpClient.Builder()
					.connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
					.readTimeout(DATA_TIMEOUT, TimeUnit.MILLISECONDS)
					.build();

			RequestBody body = RequestBody.create(
					MediaType.parse("application/sparql-query; charset=utf-8"),
					postBody);
			Request request = new Request.Builder()
					.url(endpointUrl)
					.addHeader("Accept", "text/csv; charset=utf-8")
					.post(body)
					.build();
			final Response response = client.newCall(request).execute();

			List<String> columns = null;
			List<List<String>> results = new ArrayList<>();
			Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(response.body().charStream());
			for (CSVRecord record : records) {
				if (columns == null) {
					columns = IteratorUtils.toList(record.iterator());
					continue;
				}
				results.add(IteratorUtils.toList(record.iterator()));
			}

			Log.info("Sparql query success, " + (results.size() - 1) + " results");
			callback.onSuccess(columns, results);

		} catch (Exception e) {

			Log.warn("Sparql query failure", e);
			callback.onFailure(e.getMessage());
		}
	}

}
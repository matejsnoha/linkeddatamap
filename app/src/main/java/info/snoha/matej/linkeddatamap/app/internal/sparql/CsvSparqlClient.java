package info.snoha.matej.linkeddatamap.app.internal.sparql;

import info.snoha.matej.linkeddatamap.Log;
import okhttp3.HttpUrl;
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

import static info.snoha.matej.linkeddatamap.Utils.formatDuration;

public class CsvSparqlClient {

	private static final int CONNECT_TIMEOUT = 5_000;
	private static final int DATA_TIMEOUT = 180_000; // TODO configurable in settings
	private static final int MAX_RESULTS = 100_000; // TODO report limit reached -> load more when zoomed in
	private static final boolean SPARQL_PROTOCOL_GET = true;

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

			OkHttpClient client = new OkHttpClient.Builder()
					.connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
					.readTimeout(DATA_TIMEOUT, TimeUnit.MILLISECONDS)
					.build();

			final String queryWithLimit = query
					+ (!query.toLowerCase(Locale.US).contains("limit") ? "\nLIMIT " + MAX_RESULTS : "");

			Log.debug("Sparql query to " + endpointUrl + " :\n" + queryWithLimit);

			Request request;

			if (SPARQL_PROTOCOL_GET) {

				request = new Request.Builder()
						.url(HttpUrl.parse(endpointUrl).newBuilder()
								.addQueryParameter("format", "text/csv")
								.addQueryParameter("timeout", String.valueOf(DATA_TIMEOUT))
								.addQueryParameter("query", queryWithLimit)
								.build())
						.addHeader("Accept", "text/csv")
						.addHeader("Accept-Charset", "utf-8")
						.get()
						.build();

			} else {

				request = new Request.Builder()
						.url(endpointUrl)
						.addHeader("Accept", "text/csv")
						.addHeader("Accept-Charset", "utf-8")
						.post(RequestBody.create(
								MediaType.parse("application/sparql-query; charset=utf-8"), queryWithLimit))
						.build();

			}

			long startTime = System.currentTimeMillis();
			final Response response = client.newCall(request).execute();
			long duration = System.currentTimeMillis() - startTime;

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

			if (columns == null) {
				throw new Exception("Could not parse SPARQL result");
			}

			Log.info("Sparql query success in " + formatDuration(duration) + ", " + results.size() + " results");
			callback.onSuccess(columns, results);

		} catch (Exception e) {

			Log.warn("Sparql query failure", e);
			callback.onFailure(e.getMessage());
		}
	}
}

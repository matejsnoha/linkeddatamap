package info.snoha.matej.linkeddatamap.sparql;

import info.snoha.matej.linkeddatamap.FrameworkConfiguration;
import info.snoha.matej.linkeddatamap.Log;
import okhttp3.FormBody;
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
	private static final int DATA_TIMEOUT = 180_000;
	private static final int MAX_RESULTS = FrameworkConfiguration.SPARQL_MAX_RESULTS; // TODO report limit reached
	private static final SparqlProtocolOperationType OPERATION_TYPE = SparqlProtocolOperationType.POST_ENCODED;

	private enum SparqlProtocolOperationType {
		GET,
		POST_DIRECT,
		POST_ENCODED
	}

	public interface Callback {

		void onFailure(String reason);
	}

	public interface CSVCallback extends Callback {

		void onSuccess(List<String> columns, List<List<String>> results);

	}

	public interface RawCallback extends Callback {

		void onSuccess(String result);

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

			switch (OPERATION_TYPE) {
				case GET:
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
					break;
				case POST_DIRECT:
					request = new Request.Builder()
							.url(endpointUrl)
							.addHeader("Accept", "text/csv")
							.addHeader("Accept-Charset", "utf-8")
							.post(RequestBody.create(
									MediaType.parse("application/sparql-query; charset=utf-8"), queryWithLimit))
							.build();
					break;
				case POST_ENCODED:
					request = new Request.Builder()
							.url(endpointUrl)
							.addHeader("Accept", "text/csv")
							.addHeader("Accept-Charset", "utf-8")
							.post(new FormBody.Builder()
									.add("query", queryWithLimit)
									.build())
							.build();
					break;
				default:
					throw new IllegalArgumentException("Unsupported SPARQL Query operation type");
			}

			long startTime = System.currentTimeMillis();
			final Response response = client.newCall(request).execute();
			long duration = System.currentTimeMillis() - startTime;

			if (callback instanceof CSVCallback) {

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

				Log.info("Sparql query success in " + formatDuration(duration) + ", "
						+ results.size() + " results");

				((CSVCallback) callback).onSuccess(columns, results);

			} else if (callback instanceof RawCallback) {

				String result = response.body().string();
				Log.info("Sparql query success in " + formatDuration(duration) + ", "
						+ result.length() + " chars raw result");

				((RawCallback) callback).onSuccess(result);

			}

		} catch (Exception e) {

			Log.warn("Sparql query failure", e);
			callback.onFailure(e.getMessage());
		}
	}
}

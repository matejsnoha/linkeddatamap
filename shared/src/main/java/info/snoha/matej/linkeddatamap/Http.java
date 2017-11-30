package info.snoha.matej.linkeddatamap;

import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.Reader;
import java.util.concurrent.TimeUnit;

public class Http {

	private static final int CONNECT_TIMEOUT = 5_000;
	private static final int DATA_TIMEOUT = 120_000;

	private static final OkHttpClient client = new OkHttpClient.Builder()
			.connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
			.readTimeout(DATA_TIMEOUT, TimeUnit.MILLISECONDS)
			.writeTimeout(DATA_TIMEOUT, TimeUnit.MILLISECONDS)
			.build();

	public static boolean isUrl(String str) {
		return str != null && str.startsWith("http");
	}

	public static OkHttpClient getClient() {
		return client;
	}

	public static Reader httpGetReader(String url) {
		try {
			return client.newCall(
					new Request.Builder().url(url).get().build())
					.execute().body().charStream();
		} catch (Exception e) {
			Log.warn("Could not get url " + url, e);
			return null;
		}
	}
}

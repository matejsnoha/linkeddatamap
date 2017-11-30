package info.snoha.matej.linkeddatamap.cloud;

import info.snoha.matej.linkeddatamap.Log;

import java.net.InetAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class CloudUtils {

	private static final DateTimeFormatter dateTimeFormatter =
			DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss O", Locale.US);

	public static String formatDate(Object date) {
		if (date instanceof OffsetDateTime) {
			return ((OffsetDateTime) date).format(dateTimeFormatter);
		} else {
			return date != null ? date.toString() : null;
		}
	}
	
	public static String urlDecode(String encoded) {
		try {
			if (encoded == null) {
				return null;
			}
			return URLDecoder.decode(encoded, StandardCharsets.UTF_8.name());
		} catch (Exception e) {
			Log.warn("Could not decode " + encoded, e);
			return null;
		}
	}

	public static String getHostname() {

		String hostname = null;
		try {
			hostname = System.getenv("HOSTNAME");
		} catch (Exception e) {
			Log.warn("Couldn't get hostname by env var");
		}
		if (hostname == null) {
			try {
				hostname = InetAddress.getLocalHost().getHostName();
			} catch (Exception e) {
				Log.warn("Couldn't get hostname from IP");
			}
		}
		return hostname;
	}
}

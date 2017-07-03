package info.snoha.matej.linkeddatamap.rdf;

import java.util.UUID;

public class Uris {

	public static String prefix(String prefix, String what) {
		return "<" + prefix + what + ">";
	}

	public static String newResource(String prefix) {
		return prefix + UUID.randomUUID().toString();
	}

	public static String schema(String what) {
		return prefix("http://schema.org/", what);
	}

	public static String rdf(String what) {
		return prefix("http://www.w3.org/1999/02/22-rdf-syntax-ns#", what);
	}

	public static String ms(String what) {
		return prefix("http://matej.snoha.info/dp/", what);
	}

	public static String msRes(String what) {
		return prefix("http://matej.snoha.info/dp/resource/", what);
	}
}

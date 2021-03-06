package info.snoha.matej.linkeddatamap.rdf;

import java.util.UUID;

public class Uris {

	public static String prefix(String prefix, String what) {
		return "<" + prefix + what + ">";
	}

	public static boolean isUri(String what) {
		return what != null && what.startsWith("<") && what.endsWith(">");
	}

	public static String newResource(String prefix) {
		return prefix + UUID.randomUUID().toString();
	}

	public static String schema(String what) {
		return prefix(Prefixes.SCHEMA_ORG, what);
	}

	public static String rdf(String what) {
		return prefix(Prefixes.RDF, what);
	}

	public static String ms(String what) {
		return prefix(Prefixes.LDM, what);
	}

	public static String msRes(String what) {
		return prefix(Prefixes.LDMRES, what);
	}
}

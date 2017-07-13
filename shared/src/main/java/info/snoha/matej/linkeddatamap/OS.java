package info.snoha.matej.linkeddatamap;

import java.util.Locale;

public class OS {

	public static boolean isAndroid() {
		return (System.getProperty("java.vendor") + " / " + System.getProperty("java.runtime.name"))
				.toLowerCase(Locale.US).contains("android");
	}
}

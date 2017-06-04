package info.snoha.matej.linkeddatamap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class Utils {

	public static InputStream getInputStream(String filename) {
		try {
			return new FileInputStream(filename);
		} catch (Exception e) {
			Log.warn("Could not open file " + getAbsolutePath(filename) + " for reading");
			return null;
		}
	}

	public static OutputStream getOutputStream(String filename) {
		try {
			return new FileOutputStream(filename);
		} catch (Exception e) {
			Log.warn("Could not open file " + getAbsolutePath(filename) + " for writing");
			return null;
		}
	}

	public static String getAbsolutePath(String filename) {
		try {
			return new File(filename).getAbsolutePath();
		} catch (Exception e) {
			return filename;
		}
	}
}

package info.snoha.matej.linkeddatamap.app.internal.utils;

import android.content.Context;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import info.snoha.matej.linkeddatamap.Log;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.InputStream;

public class AndroidUtils {

    public static int dipToPixels(Context context, float dip){
        int dpi = context.getResources().getDisplayMetrics().densityDpi;
        return (int) (dip * (dpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static int pixelsToDip(Context context, float px){
        int dpi = context.getResources().getDisplayMetrics().densityDpi;
        return (int) (px / (dpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static String getVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            return null;
        }
    }

    public static Boolean getBooleanPreferenceValue(Context context, String key) {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getBoolean(key, false);
    }

    public static String getStringPreferenceValue(Context context, String key) {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(key, "");
    }

    public static void setBooleanPreferenceValue(Context context, String key, Boolean value) {
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(key, value)
                .commit();
    }

    public static void setStringPreferenceValue(Context context, String key, String value) {
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()
                .putString(key, value)
                .commit();
    }

    public static File getFile(String path) {
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File root = Environment.getExternalStorageDirectory();
                File dir = new File(root.getAbsolutePath() + File.separator
                        + FilenameUtils.getPathNoEndSeparator(path));
                dir.mkdirs();
                return new File(dir, FilenameUtils.getName(path));
            } else {
                return null;
            }
        } catch (Exception e) {
            Log.error("Could not open file " + path, e);
            return null;
        }
    }

    public static String readRawResource(Context context, int resource) {
        try {
            return IOUtils.toString(context.getResources().openRawResource(resource), "UTF-8");
        } catch (Exception e) {
            Log.error("Could not open raw resource " + resource, e);
            return null;
        }
    }

    public static InputStream getRawResource(Context context, int resource) {
        try {
            return context.getResources().openRawResource(resource);
        } catch (Exception e) {
            Log.error("Could not open raw resource " + resource, e);
            return null;
        }
    }
}

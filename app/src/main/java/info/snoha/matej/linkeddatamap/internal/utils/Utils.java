package info.snoha.matej.linkeddatamap.internal.utils;

import android.content.Context;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileOutputStream;

public class Utils {

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

    public static String getStringPreferenceValue(Preference preference) {
        return PreferenceManager
                .getDefaultSharedPreferences(preference.getContext())
                .getString(preference.getKey(), "");
    }

    public static FileOutputStream getFileOutputStream(String path) {
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File root = Environment.getExternalStorageDirectory();
                File dir = new File(root.getAbsolutePath() + FilenameUtils.getPathNoEndSeparator(path));
                dir.mkdirs();
                File file = new File(dir, FilenameUtils.getName(path));
                return new FileOutputStream(file);
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}

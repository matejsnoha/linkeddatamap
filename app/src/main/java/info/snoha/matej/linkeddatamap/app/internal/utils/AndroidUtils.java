package info.snoha.matej.linkeddatamap.app.internal.utils;

import android.content.Context;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

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

    public static String getStringPreferenceValue(Preference preference) {
        return PreferenceManager
                .getDefaultSharedPreferences(preference.getContext())
                .getString(preference.getKey(), "");
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
            e.printStackTrace();
            return null;
        }
    }
}

package info.snoha.matej.linkeddatamap;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

public class Utils {

    public static void runOnUIThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

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
}

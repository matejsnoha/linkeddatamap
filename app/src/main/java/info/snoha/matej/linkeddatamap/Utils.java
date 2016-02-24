package info.snoha.matej.linkeddatamap;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
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
}

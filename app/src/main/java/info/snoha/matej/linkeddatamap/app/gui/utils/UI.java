package info.snoha.matej.linkeddatamap.app.gui.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import info.snoha.matej.linkeddatamap.Log;

public class UI {

    public static boolean isUiThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    public static void run(Runnable runnable) {
        if (isUiThread()) {
            tryCatch(runnable).run();
        } else {
            new Handler(Looper.getMainLooper()).post(tryCatch(runnable));
        }
    }

    public static void message(final Context context, final String text) {
        run(() -> Toast.makeText(context, text, Toast.LENGTH_LONG).show());
    }

    public static void messageShort(final Context context, final String text) {
        run(() -> Toast.makeText(context, text, Toast.LENGTH_SHORT).show());
    }

    private static Runnable tryCatch(Runnable runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (Exception e) {
                Log.error("UI thread caught exception", e);
            }
        };
    }
}

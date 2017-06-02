package info.snoha.matej.linkeddatamap.gui.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class UI {

    public static boolean isUiThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    public static void run(Runnable runnable) {
        if (isUiThread()) {
            runnable.run();
        } else {
            new Handler(Looper.getMainLooper()).post(runnable);
        }
    }

    public static void message(final Context context, final String text) {
        run(() -> Toast.makeText(context, text, Toast.LENGTH_LONG).show());
    }
}

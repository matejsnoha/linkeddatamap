package info.snoha.matej.linkeddatamap.app.gui.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;
import info.snoha.matej.linkeddatamap.Log;
import info.snoha.matej.linkeddatamap.app.internal.utils.AndroidUtils;

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

    public static void runOnNextLayout(final View view, final Runnable runnable) {
        runOnLayout(view, tryCatch(runnable), true);
    }

    public static void runOnEachLayout(final View view, final Runnable runnable) {
        runOnLayout(view, tryCatch(runnable), false);
    }

    private static void runOnLayout(final View view, final Runnable runnable, final boolean runOnce) {
        try {
            view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onGlobalLayout() {
                    try {
                        runnable.run();
                        if (runOnce) {
                            if (AndroidUtils.isApiAtLeast(Build.VERSION_CODES.JELLY_BEAN)) {
                                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            } else {
                                view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                            }
                        }
                    } catch (Exception e) {
                        Log.error("UI thread error", e);
                    }
                }
            });
        } catch (Exception e) {
            Log.error("UI thread error", e);
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

package com.commit451.gitlab.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;

/**
 * Oh the woes of a weird widget
 */
public class WidgetUtil {

    /**
     * Update any widget
     * @param context context
     * @param clazz class of the widget provider
     * @param widgetId the widget id
     * @see <a href="http://stackoverflow.com/a/7738687/895797">http://stackoverflow.com/a/7738687/895797</a>
     */
    public static void triggerWidgetUpdate(Context context, Class clazz, int widgetId) {
        Intent intent = new Intent(context, clazz);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
        // since it seems the onUpdate() is only fired on that:
        int[] ids = {widgetId};
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }
}

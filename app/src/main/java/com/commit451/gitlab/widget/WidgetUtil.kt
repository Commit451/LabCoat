package com.commit451.gitlab.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent

/**
 * Oh the woes of a weird widget
 */
object WidgetUtil {

    /**
     * Update any widget
     * @param context context
     * *
     * @param clazz class of the widget provider
     * *
     * @param widgetId the widget id
     * *
     * @see [http://stackoverflow.com/a/7738687/895797](http://stackoverflow.com/a/7738687/895797)
     */
    fun triggerWidgetUpdate(context: Context, clazz: Class<*>, widgetId: Int) {
        val intent = Intent(context, clazz)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
        // since it seems the onUpdate() is only fired on that:
        val ids = intArrayOf(widgetId)
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        context.sendBroadcast(intent)
    }
}

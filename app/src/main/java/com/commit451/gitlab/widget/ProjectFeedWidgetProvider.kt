package com.commit451.gitlab.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.commit451.gitlab.R
import com.commit451.gitlab.navigation.DeepLinker

class ProjectFeedWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_FOLLOW_LINK = "com.commit451.gitlab.ACTION_FOLLOW_LINK"
        const val EXTRA_LINK = "com.commit451.gitlab.EXTRA_LINK"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_FOLLOW_LINK) {
            val uri = intent.getStringExtra(EXTRA_LINK)
            val launchIntent = DeepLinker.generateDeeplinkIntentFromUri(context, Uri.parse(uri))
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        }
        super.onReceive(context, intent)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (widgetId in appWidgetIds) {

            val account = ProjectFeedWidgetPrefs.getAccount(context, widgetId)
            val feedUrl = ProjectFeedWidgetPrefs.getFeedUrl(context, widgetId)
            if (account != null && feedUrl != null) {
                val intent = FeedWidgetService.newIntent(context, widgetId, account, feedUrl)


                intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
                val rv = RemoteViews(context.packageName, R.layout.widget_layout_entry)
                rv.setRemoteAdapter(R.id.list_view, intent)

                rv.setEmptyView(R.id.list_view, R.id.empty_view)

                val toastIntent = Intent(context, ProjectFeedWidgetProvider::class.java)
                toastIntent.action = ACTION_FOLLOW_LINK
                toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
                val toastPendingIntent = PendingIntent.getBroadcast(context, 0, toastIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT)
                rv.setPendingIntentTemplate(R.id.list_view, toastPendingIntent)

                appWidgetManager.updateAppWidget(widgetId, rv)
            }
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }
}

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
import timber.log.Timber

class UserFeedWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_FOLLOW_LINK = "com.commit451.gitlab.ACTION_FOLLOW_LINK"
        const val EXTRA_LINK = "com.commit451.gitlab.EXTRA_LINK"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("onReceive")
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
            Timber.d("onUpdate with id: $widgetId")

            // Here we setup the intent which points to the StackViewService which will
            // provide the views for this collection.
            val account = UserFeedWidgetPrefs.getAccount(context, widgetId)

            if (account == null) {
                //TODO alert the user to this misfortune?
                Timber.e("Error getting account or feed url")
            } else {
                val feedUrl = account.serverUrl + "${account.username}.atom"
                Timber.d("Feed url: $feedUrl")
                val intent = FeedWidgetService.newIntent(context, widgetId, account, feedUrl)
                // When intents are compared, the extras are ignored, so we need to embed the extras
                // into the data so that the extras will not be ignored.
                intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
                val rv = RemoteViews(context.packageName, R.layout.widget_layout_entry)
                rv.setRemoteAdapter(R.id.list_view, intent)

                rv.setEmptyView(R.id.list_view, R.id.empty_view)

                // Here we setup the a pending intent template. Individuals items of a collection
                // cannot setup their own pending intents, instead, the collection as a whole can
                // setup a pending intent template, and the individual items can set a fillInIntent
                // to create unique before on an item to item basis.
                val actionIntent = Intent(context, UserFeedWidgetProvider::class.java)
                actionIntent.action = ACTION_FOLLOW_LINK
                actionIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
                val actionPendingIntent = PendingIntent.getBroadcast(context, 0, actionIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT)
                rv.setPendingIntentTemplate(R.id.list_view, actionPendingIntent)

                appWidgetManager.updateAppWidget(widgetId, rv)
            }
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }
}

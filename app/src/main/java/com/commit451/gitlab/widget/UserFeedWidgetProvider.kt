/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.commit451.gitlab.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.commit451.gitlab.R
import com.commit451.gitlab.extension.feedUrl
import com.commit451.gitlab.navigation.DeepLinker
import timber.log.Timber

class UserFeedWidgetProvider : AppWidgetProvider() {

    companion object {
        val ACTION_FOLLOW_LINK = "com.commit451.gitlab.ACTION_FOLLOW_LINK"
        val EXTRA_LINK = "com.commit451.gitlab.EXTRA_LINK"
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
            val feedUrl = account?.user?.feedUrl
            if (account == null || feedUrl == null) {
                //TODO alert the user to this misfortune?
                Timber.e("Error getting account or feed url")
            } else {
                Timber.d("Updating widget with url $feedUrl")
                val intent = ProjectFeedWidgetService.newIntent(context, widgetId, account, feedUrl)
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
                actionIntent.action = UserFeedWidgetProvider.ACTION_FOLLOW_LINK
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
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
import com.commit451.gitlab.navigation.DeepLinker

class ProjectFeedWidgetProvider : AppWidgetProvider() {

    companion object {
        val ACTION_FOLLOW_LINK = "com.commit451.gitlab.ACTION_FOLLOW_LINK"
        val EXTRA_LINK = "com.commit451.gitlab.EXTRA_LINK"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val mgr = AppWidgetManager.getInstance(context)
        if (intent.action == ACTION_FOLLOW_LINK) {
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID)
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
            val intent = ProjectFeedWidgetService.newIntent(context, widgetId, account!!, feedUrl!!)

            intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
            val rv = RemoteViews(context.packageName, R.layout.widget_layout_entry)
            rv.setRemoteAdapter(R.id.list_view, intent)

            rv.setEmptyView(R.id.list_view, R.id.empty_view)

            val toastIntent = Intent(context, ProjectFeedWidgetProvider::class.java)
            toastIntent.action = ProjectFeedWidgetProvider.ACTION_FOLLOW_LINK
            toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
            val toastPendingIntent = PendingIntent.getBroadcast(context, 0, toastIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT)
            rv.setPendingIntentTemplate(R.id.list_view, toastPendingIntent)

            appWidgetManager.updateAppWidget(widgetId, rv)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }
}
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

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.widget.RemoteViewsService

import com.commit451.gitlab.model.Account

import org.parceler.Parcels

/**
 * Service that basically just defers everything to a Factory. Yay!
 */
class ProjectFeedWidgetService : RemoteViewsService() {

    companion object {

        val EXTRA_ACCOUNT = "account"
        val EXTRA_FEED_URL = "feed_url"

        fun newIntent(context: Context, widgetId: Int, account: Account, feedUrl: String): Intent {
            val intent = Intent(context, ProjectFeedWidgetService::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            intent.putExtra(EXTRA_ACCOUNT, Parcels.wrap(account))
            intent.putExtra(EXTRA_FEED_URL, feedUrl)
            return intent
        }
    }

    override fun onGetViewFactory(intent: Intent): RemoteViewsService.RemoteViewsFactory {
        val account = Parcels.unwrap<Account>(intent.getParcelableExtra<Parcelable>(EXTRA_ACCOUNT))
        val feedUrl = intent.getStringExtra(EXTRA_FEED_URL)
        return FeedRemoteViewsFactory(applicationContext, intent, account, feedUrl)
    }
}
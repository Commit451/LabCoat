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

package com.commit451.gitlab.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViewsService;

import com.commit451.gitlab.model.Account;

import org.parceler.Parcels;

/**
 * Service that basically just defers everything to a Factory. Yay!
 */
public class ProjectFeedWidgetService extends RemoteViewsService {

    private static final String EXTRA_ACCOUNT = "account";
    private static final String EXTRA_FEED_URL = "feed_url";

    public static Intent newIntent(Context context, int widgetId, Account account, String feedUrl) {
        Intent intent = new Intent(context, ProjectFeedWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        intent.putExtra(EXTRA_ACCOUNT, Parcels.wrap(account));
        intent.putExtra(EXTRA_FEED_URL, feedUrl);
        return intent;
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Account account = Parcels.unwrap(intent.getParcelableExtra(EXTRA_ACCOUNT));
        String feedUrl = intent.getStringExtra(EXTRA_FEED_URL);
        return new FeedRemoteViewsFactory(getApplicationContext(), intent, account, feedUrl);
    }
}
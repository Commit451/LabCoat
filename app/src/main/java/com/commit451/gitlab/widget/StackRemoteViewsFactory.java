package com.commit451.gitlab.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.commit451.gitlab.R;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.api.GitLabRss;
import com.commit451.gitlab.model.Account;
import com.commit451.gitlab.model.rss.Entry;
import com.commit451.gitlab.model.rss.Feed;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Response;

/**
 * Remote all the views
 */
public class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final int mCount = 10;
    private Context mContext;
    private int mAppWidgetId;
    private GitLabRss mApi;
    private ArrayList<Entry> mEntries;

    public StackRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        mEntries = new ArrayList<>();
        //TODO remove, remove, remove
        Account account = new Account();
        account.setServerUrl(Uri.parse("https://gitlab.com/"));
        account.setPrivateToken("yyvWMKRW6DHxLVK6nzeF");
        mApi = GitLabClient.rssInstance(account);
    }

    @Override
    public void onDestroy() {
        // In onDestroy() you should tear down anything that was setup for your data source,
        // eg. cursors, connections, etc.
        mEntries.clear();
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        // position will always range from 0 to getCount() - 1.

        Entry entry = mEntries.get(position);

        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_item_entry);
        rv.setTextViewText(R.id.title, entry.getTitle());
        rv.setTextViewText(R.id.summary, entry.getSummary());

        // Next, we set a fill-intent which will be used to fill-in the pending intent template
        // which is set on the collection view in StackWidgetProvider.
        Bundle extras = new Bundle();
        extras.putInt(StackWidgetProvider.EXTRA_ITEM, position);
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        rv.setOnClickFillInIntent(R.id.root, fillInIntent);

        //Does not like that these do not come from the main thread
//        Picasso.with(mContext)
//                .load(entry.getThumbnail().getUrl())
//                .into(rv, R.id.image, new int[] { mAppWidgetId });

        // You can do heaving lifting in here, synchronously. For example, if you need to
        // process an image, fetch something from the network, etc., it is ok to do it here,
        // synchronously. A loading view will show up in lieu of the actual contents in the
        // interim.


        // Return the remote views object.
        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        // You can create a custom loading view (for create when getViewAt() is slow.) If you
        // return null here, you will get the default loading view.
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onDataSetChanged() {
        // This is triggered when you call AppWidgetManager notifyAppWidgetViewDataChanged
        // on the collection view corresponding to this factory. You can do heaving lifting in
        // here, synchronously. For example, if you need to process an image, fetch something
        // from the network, etc., it is ok to do it here, synchronously. The widget will remain
        // in its current state while work is being done here, so you don't need to worry about
        // locking up the widget.
        //TODO unhardcode this
        try {
            Response<Feed> feedResponse = mApi.getFeed("https://gitlab.com/Commit451/LabCoat.atom").execute();
            if (feedResponse.isSuccessful()) {
                if (feedResponse.body().getEntries() != null) {
                    mEntries.addAll(feedResponse.body().getEntries());
                }
            }

        } catch (IOException e) {
            //maybe let the user know somehow?
        }
    }
}

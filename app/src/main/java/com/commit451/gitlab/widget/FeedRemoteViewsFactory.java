package com.commit451.gitlab.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.commit451.gitlab.BuildConfig;
import com.commit451.gitlab.R;
import com.commit451.gitlab.api.GitLabRss;
import com.commit451.gitlab.api.GitLabRssFactory;
import com.commit451.gitlab.api.OkHttpClientFactory;
import com.commit451.gitlab.api.PicassoFactory;
import com.commit451.gitlab.model.Account;
import com.commit451.gitlab.model.rss.Entry;
import com.commit451.gitlab.model.rss.Feed;
import com.commit451.gitlab.transformation.CircleTransformation;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Remote all the views
 */
public class FeedRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final int mCount = 10;
    private Context mContext;
    private int mAppWidgetId;
    private String mFeedUrl;
    private ArrayList<Entry> mEntries;
    private Picasso mPicasso;
    private GitLabRss mRssClient;

    public FeedRemoteViewsFactory(Context context, Intent intent, Account account, String url) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        mFeedUrl = url;

        OkHttpClient.Builder gitlabRssClientBuilder = OkHttpClientFactory.create(account);
        if (BuildConfig.DEBUG) {
            gitlabRssClientBuilder.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
        }
        mRssClient = GitLabRssFactory.create(account, gitlabRssClientBuilder.build());
        OkHttpClient.Builder picassoClientBuilder = OkHttpClientFactory.create(account);
        mPicasso = PicassoFactory.createPicasso(picassoClientBuilder.build());
    }

    @Override
    public void onCreate() {
        mEntries = new ArrayList<>();
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

        if (position >= mEntries.size()) {
            return null;
        }
        Entry entry = mEntries.get(position);

        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_item_entry);
        rv.setTextViewText(R.id.title, entry.getTitle());
        rv.setTextViewText(R.id.summary, entry.getSummary());

        // Next, we set a fill-intent which will be used to fill-in the pending intent template
        // which is set on the collection view in UserFeedWidgetProvider.
        Intent fillInIntent = new Intent();
        fillInIntent.putExtra(UserFeedWidgetProvider.EXTRA_LINK, entry.getLink().getHref().toString());
        rv.setOnClickFillInIntent(R.id.root, fillInIntent);

        try {
            Bitmap image = mPicasso
                    .load(entry.getThumbnail().getUrl())
                    .transform(new CircleTransformation())
                    .get();
            rv.setImageViewBitmap(R.id.image, image);
        } catch (IOException e) {
            //well, thats too bad
        }

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

        try {
            Feed feed = mRssClient.getFeed(mFeedUrl)
                    .blockingGet();
            if (feed.getEntries() != null) {
                mEntries.addAll(feed.getEntries());
            }

        } catch (Exception e) {
            //maybe let the user know somehow?
        }
    }
}

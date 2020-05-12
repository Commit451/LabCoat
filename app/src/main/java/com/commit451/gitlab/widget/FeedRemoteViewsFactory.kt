package com.commit451.gitlab.widget

import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import coil.transform.CircleCropTransformation
import com.commit451.gitlab.BuildConfig
import com.commit451.gitlab.R
import com.commit451.gitlab.api.GitLabRss
import com.commit451.gitlab.api.GitLabRssFactory
import com.commit451.gitlab.api.OkHttpClientFactory
import com.commit451.gitlab.image.CoilCompat
import com.commit451.gitlab.model.Account
import com.commit451.gitlab.model.rss.Entry
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import java.util.*

/**
 * Remote all the views
 */
class FeedRemoteViewsFactory(
        private val context: Context,
        account: Account,
        private val feedUrl: String
) : RemoteViewsService.RemoteViewsFactory {

    companion object {
        const val COUNT = 10
    }

    private var entries = mutableListOf<Entry>()
    private val rssClient: GitLabRss

    init {
        val gitlabRssClientBuilder = OkHttpClientFactory.create(account)
        if (BuildConfig.DEBUG) {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            gitlabRssClientBuilder.addInterceptor(httpLoggingInterceptor.apply { httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY })
        }
        rssClient = GitLabRssFactory.create(account, gitlabRssClientBuilder.build())
    }

    override fun onCreate() {
        entries = ArrayList()
    }

    override fun onDestroy() {
        // In onDestroy() you should tear down anything that was setup for your data source,
        // eg. cursors, connections, etc.
        entries.clear()
    }

    override fun getCount(): Int {
        return COUNT
    }

    override fun getViewAt(position: Int): RemoteViews? {
        // position will always range from 0 to getCount() - 1.

        if (position >= entries.size) {
            return null
        }
        val entry = entries[position]

        val rv = RemoteViews(context.packageName, R.layout.widget_item_entry)
        rv.setTextViewText(R.id.title, entry.title)
        rv.setTextViewText(R.id.summary, entry.summary)

        // Next, we set a fill-intent which will be used to fill-in the pending intent template
        // which is set on the collection view in UserFeedWidgetProvider.
        val fillInIntent = Intent()
        fillInIntent.putExtra(UserFeedWidgetProvider.EXTRA_LINK, entry.link.href)
        rv.setOnClickFillInIntent(R.id.root, fillInIntent)

        try {
            val drawable = CoilCompat.getBlocking(context, entry.thumbnail.url) {
                transformations(CircleCropTransformation())
            }
            if (drawable is BitmapDrawable) {
                rv.setImageViewBitmap(R.id.image, drawable.bitmap)
            }
        } catch (e: Exception) {
            Timber.e(e)
        }

        return rv
    }

    override fun getLoadingView(): RemoteViews? {
        // You can create a custom loading view (for create when getViewAt() is slow.) If you
        // return null here, you will get the default loading view.
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun onDataSetChanged() {
        // This is triggered when you call AppWidgetManager notifyAppWidgetViewDataChanged
        // on the collection view corresponding to this factory. You can do heaving lifting in
        // here, synchronously. For example, if you need to process an image, fetch something
        // from the network, etc., it is ok to do it here, synchronously. The widget will remain
        // in its current state while work is being done here, so you don't need to worry about
        // locking up the widget.

        try {
            val feed = rssClient.getFeed(feedUrl)
                    .blockingGet()
            entries.clear()
            val nextEntries = feed.entries
            if (nextEntries != null) {
                entries.addAll(nextEntries)
            }
        } catch (e: Exception) {
            //maybe let the user know somehow?
            Timber.e(e)
        }
    }
}

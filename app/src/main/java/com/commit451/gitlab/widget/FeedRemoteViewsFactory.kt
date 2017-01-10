package com.commit451.gitlab.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.commit451.gitlab.BuildConfig
import com.commit451.gitlab.R
import com.commit451.gitlab.api.GitLabRss
import com.commit451.gitlab.api.GitLabRssFactory
import com.commit451.gitlab.api.OkHttpClientFactory
import com.commit451.gitlab.api.PicassoFactory
import com.commit451.gitlab.model.Account
import com.commit451.gitlab.model.rss.Entry
import com.commit451.gitlab.transformation.CircleTransformation
import com.squareup.picasso.Picasso
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import java.io.IOException
import java.util.*

/**
 * Remote all the views
 */
class FeedRemoteViewsFactory(private val context: Context, intent: Intent, account: Account, private val feedUrl: String) : RemoteViewsService.RemoteViewsFactory {

    companion object {
        val COUNT = 10
    }

    val appWidgetId: Int = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID)
    var entries: ArrayList<Entry>? = null
    val picasso: Picasso
    val rssClient: GitLabRss

    init {

        val gitlabRssClientBuilder = OkHttpClientFactory.create(account)
        if (BuildConfig.DEBUG) {
            gitlabRssClientBuilder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        }
        rssClient = GitLabRssFactory.create(account, gitlabRssClientBuilder.build())
        val picassoClientBuilder = OkHttpClientFactory.create(account)
        picasso = PicassoFactory.createPicasso(picassoClientBuilder.build())
    }

    override fun onCreate() {
        entries = ArrayList<Entry>()
    }

    override fun onDestroy() {
        // In onDestroy() you should tear down anything that was setup for your data source,
        // eg. cursors, connections, etc.
        entries!!.clear()
    }

    override fun getCount(): Int {
        return COUNT
    }

    override fun getViewAt(position: Int): RemoteViews? {
        // position will always range from 0 to getCount() - 1.

        if (position >= entries!!.size) {
            return null
        }
        val entry = entries!![position]

        val rv = RemoteViews(context.packageName, R.layout.widget_item_entry)
        rv.setTextViewText(R.id.title, entry.title)
        rv.setTextViewText(R.id.summary, entry.summary)

        // Next, we set a fill-intent which will be used to fill-in the pending intent template
        // which is set on the collection view in UserFeedWidgetProvider.
        val fillInIntent = Intent()
        fillInIntent.putExtra(UserFeedWidgetProvider.EXTRA_LINK, entry.link.href.toString())
        rv.setOnClickFillInIntent(R.id.root, fillInIntent)

        try {
            val image = picasso
                    .load(entry.thumbnail.url)
                    .transform(CircleTransformation())
                    .get()
            rv.setImageViewBitmap(R.id.image, image)
        } catch (e: IOException) {
            //well, thats too bad
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
            if (feed.entries != null) {
                entries!!.addAll(feed.entries)
            }
        } catch (e: Exception) {
            //maybe let the user know somehow?
            Timber.e(e)
        }
    }
}

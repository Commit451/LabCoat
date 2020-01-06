package com.commit451.gitlab.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.DividerItemDecoration
import com.commit451.gitlab.adapter.FeedAdapter
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.rss.Entry
import com.commit451.gitlab.navigation.Navigator
import com.google.android.material.snackbar.Snackbar
import com.novoda.simplechromecustomtabs.SimpleChromeCustomTabs
import kotlinx.android.synthetic.main.fragment_feed.*
import timber.log.Timber

/**
 * Takes an RSS feed url and shows the feed
 */
class FeedFragment : BaseFragment() {

    companion object {

        private const val EXTRA_FEED_URL = "extra_feed_url"

        fun newInstance(feedUrl: String): FeedFragment {
            val args = Bundle()
            args.putString(EXTRA_FEED_URL, feedUrl)

            val fragment = FeedFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var adapterFeed: FeedAdapter

    private var feedUrl: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        feedUrl = Uri.parse(arguments?.getString(EXTRA_FEED_URL))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapterFeed = FeedAdapter(object : FeedAdapter.Listener {
            override fun onFeedEntryClicked(entry: Entry) {
                if (entry.link.href.isEmpty()) {
                    Snackbar.make(swipeRefreshLayout, R.string.not_a_valid_url, Snackbar.LENGTH_SHORT)
                            .show()
                } else {
                    Navigator.navigateToUrl(baseActivty, entry.link.href, App.get().getAccount())
                }
            }
        })
        listEntries.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
        listEntries.addItemDecoration(DividerItemDecoration(baseActivty))
        listEntries.adapter = adapterFeed

        swipeRefreshLayout.setOnRefreshListener { loadData() }

        loadData()
    }

    override fun onResume() {
        super.onResume()
        SimpleChromeCustomTabs.getInstance().connectTo(baseActivty)
    }

    override fun onPause() {
        if (SimpleChromeCustomTabs.getInstance().isConnected) {
            SimpleChromeCustomTabs.getInstance().disconnectFrom(baseActivty)
        }
        super.onPause()
    }

    override fun loadData() {
        if (view == null) {
            return
        }
        if (feedUrl == null) {
            swipeRefreshLayout.isRefreshing = false
            textMessage.visibility = View.VISIBLE
            return
        }
        textMessage.visibility = View.GONE
        swipeRefreshLayout.isRefreshing = true
        App.get().gitLab.getFeed(feedUrl!!.toString())
                .with(this)
                .subscribe({
                    swipeRefreshLayout.isRefreshing = false
                    val entries = it.entries
                    if (entries != null && entries.isNotEmpty()) {
                        textMessage.visibility = View.GONE
                    } else {
                        Timber.d("No activity in the feed")
                        textMessage.visibility = View.VISIBLE
                        textMessage.setText(R.string.no_activity)
                    }
                    adapterFeed.setEntries(it.entries)
                }, {
                    Timber.e(it)
                    swipeRefreshLayout.isRefreshing = false
                    textMessage.visibility = View.VISIBLE
                    textMessage.setText(R.string.connection_error_feed)
                    adapterFeed.setEntries(null)
                })
    }
}

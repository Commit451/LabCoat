package com.commit451.gitlab.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.commit451.addendum.design.snackbar
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.BaseAdapter
import com.commit451.gitlab.model.rss.Entry
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.util.LoadHelper
import com.commit451.gitlab.viewHolder.FeedEntryViewHolder
import com.novoda.simplechromecustomtabs.SimpleChromeCustomTabs
import kotlinx.android.synthetic.main.fragment_feed.*
import kotlinx.android.synthetic.main.fragment_feed.swipeRefreshLayout

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

    private lateinit var adapter: BaseAdapter<Entry, FeedEntryViewHolder>
    private lateinit var loadHelper: LoadHelper<Entry>

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

        adapter = BaseAdapter(
                onCreateViewHolder = { parent, _ ->
                    val viewHolder = FeedEntryViewHolder.inflate(parent)
                    viewHolder.itemView.setOnClickListener {
                        val entry = adapter.items[viewHolder.adapterPosition]
                        if (entry.link.href.isEmpty()) {
                            root.snackbar(R.string.not_a_valid_url)
                        } else {
                            Navigator.navigateToUrl(baseActivty, entry.link.href, App.get().getAccount())
                        }
                    }
                    viewHolder
                },
                onBindViewHolder = { viewHolder, _, item -> viewHolder.bind(item) }
        )
        loadHelper = LoadHelper(
                lifecycleOwner = this,
                recyclerView = listEntries,
                baseAdapter = adapter,
                swipeRefreshLayout = swipeRefreshLayout,
                dividers = true,
                errorOrEmptyTextView = textMessage,
                loadInitial = {
                    gitLab.feed(feedUrl!!.toString())
                }
        )

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
        loadHelper.load()
    }
}

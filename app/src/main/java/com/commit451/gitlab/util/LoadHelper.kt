package com.commit451.gitlab.util

import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.BaseAdapter
import com.commit451.gitlab.api.BodyWithPagination
import com.commit451.gitlab.extension.with
import io.reactivex.Single
import timber.log.Timber

/**
 * Helps with the specific scenario of loading data into a RecyclerView with a SwipeRefreshLayout
 * with pagination and load more indication at the bottom. See also [BaseAdapter]
 * and [OnScrollLoadMoreListener]
 */
class LoadHelper<T, VH : RecyclerView.ViewHolder>(
        private val lifecycleOwner: LifecycleOwner,
        recyclerView: RecyclerView,
        private val swipeRefreshLayout: SwipeRefreshLayout,
        private val errorOrEmptyTextView: TextView,
        createLayoutManager: () -> LinearLayoutManager = { LinearLayoutManager(recyclerView.context) },
        createAdapter: () -> BaseAdapter<T, VH>,
        private val loadInitial: () -> Single<BodyWithPagination<List<T>>>,
        private val loadMore: (url: String) -> Single<BodyWithPagination<List<T>>>? = { null }
) {

    private var baseAdapter: BaseAdapter<T, VH> = createAdapter.invoke()
    private var nextPageUrl: String? = null

    init {
        val layoutManager = createLayoutManager.invoke()
        recyclerView.layoutManager = layoutManager
        recyclerView.addOnScrollListener(
                OnScrollLoadMoreListener(
                        layoutManager = layoutManager,
                        shouldLoadMore = { nextPageUrl != null },
                        loadMore = { loadNext() }
                )
        )
        baseAdapter = createAdapter.invoke()
        recyclerView.adapter = baseAdapter
        swipeRefreshLayout.setOnRefreshListener { load() }
    }

    /**
     * Load the initial data. This will do all the magic for you.
     */
    fun load() {
        errorOrEmptyTextView.isVisible = false
        swipeRefreshLayout.isRefreshing = true
        nextPageUrl = null
        baseAdapter.isLoading = false
        loadInitial.invoke()
                .with(lifecycleOwner)
                .subscribe({
                    baseAdapter.set(it.body)
                    swipeRefreshLayout.isRefreshing = false
                    if (it.body.isNotEmpty()) {
                        errorOrEmptyTextView.isVisible = false
                        nextPageUrl = it.paginationData.next
                    } else {
                        errorOrEmptyTextView.isVisible = true
                        errorOrEmptyTextView.setText(R.string.no_data_found)
                    }
                    baseAdapter.set(it.body)
                }, {
                    Timber.e(it)
                    errorOrEmptyTextView.isVisible = true
                    errorOrEmptyTextView.text = swipeRefreshLayout.context.getString(R.string.unable_to_load)
                })
    }

    /**
     * Load the next page of results.
     */
    private fun loadNext() {
        val url = nextPageUrl ?: return
        val single = loadMore.invoke(url) ?: return
        // reset this to null, so that we don't keep triggering the reload over and over
        nextPageUrl = null
        baseAdapter.isLoading = true
        single.with(lifecycleOwner)
                .subscribe({
                    baseAdapter.isLoading = false
                    baseAdapter.addAll(it.body)
                    nextPageUrl = it.paginationData.next
                }, {
                    Timber.e(it)
                    baseAdapter.isLoading = false
                })
    }
}

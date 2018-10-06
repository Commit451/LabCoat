package com.commit451.gitlab.util

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class OnScrollLoadMoreListener(private val layoutManager: LinearLayoutManager, private val shouldLoadMore: () -> Boolean, private val loadMore: () -> Unit) : RecyclerView.OnScrollListener() {

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        val visibleItemCount = layoutManager.childCount
        val totalItemCount = layoutManager.itemCount
        val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
        if (firstVisibleItem + visibleItemCount >= totalItemCount && shouldLoadMore.invoke()) {
            loadMore.invoke()
        }
    }
}
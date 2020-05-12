package com.commit451.gitlab.util

import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.commit451.gitlab.api.GitLab

class LoadHelper(
        gitLab: GitLab,
        recyclerView: RecyclerView,
        errorRoot: ViewGroup,
        errorView: TextView
) {

    private var loadedAll: Boolean = false
    private var nextPageUrl: String? = null

    init {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        recyclerView.addOnScrollListener(
                OnScrollLoadMoreListener(
                        layoutManager = layoutManager,
                        shouldLoadMore = { !loadedAll },
                        loadMore = {

                        }
                )
        )
    }

    fun load() {

    }
}

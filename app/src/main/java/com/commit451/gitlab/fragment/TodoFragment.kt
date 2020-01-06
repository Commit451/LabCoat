package com.commit451.gitlab.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.TodoAdapter
import com.commit451.gitlab.extension.mapResponseSuccessWithPaginationData
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.Todo
import com.commit451.gitlab.navigation.Navigator
import com.google.android.material.snackbar.Snackbar
import io.reactivex.Single
import kotlinx.android.synthetic.main.fragment_todo.*
import retrofit2.Response
import timber.log.Timber

class TodoFragment : BaseFragment() {

    companion object {

        private const val EXTRA_MODE = "extra_mode"

        const val MODE_TODO = 0
        const val MODE_DONE = 1

        fun newInstance(mode: Int): TodoFragment {
            val args = Bundle()
            args.putInt(EXTRA_MODE, mode)

            val fragment = TodoFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var layoutManagerTodos: LinearLayoutManager
    private lateinit var adapterTodos: TodoAdapter

    private var mode: Int = 0
    private var nextPageUrl: String? = null
    private var loading = false

    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val visibleItemCount = layoutManagerTodos.childCount
            val totalItemCount = layoutManagerTodos.itemCount
            val firstVisibleItem = layoutManagerTodos.findFirstVisibleItemPosition()
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !loading && nextPageUrl != null) {
                loadMore()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mode = arguments?.getInt(EXTRA_MODE)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_todo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapterTodos = TodoAdapter(object : TodoAdapter.Listener {
            override fun onTodoClicked(todo: Todo) {
                val targetUrl = todo.targetUrl
                if (targetUrl != null) {
                    Navigator.navigateToUrl(baseActivty, targetUrl, App.get().getAccount())
                } else {
                    Snackbar.make(swipeRefreshLayout, R.string.not_a_valid_url, Snackbar.LENGTH_SHORT)
                }
            }
        })
        layoutManagerTodos = LinearLayoutManager(activity)
        listTodos.layoutManager = layoutManagerTodos
        listTodos.adapter = adapterTodos
        listTodos.addOnScrollListener(onScrollListener)

        swipeRefreshLayout.setOnRefreshListener { loadData() }

        loadData()
    }

    override fun loadData() {
        if (view == null) {
            return
        }
        textMessage.visibility = View.GONE

        nextPageUrl = null

        when (mode) {
            MODE_TODO -> {
                showLoading()
                getTodos(App.get().gitLab.getTodos(Todo.STATE_PENDING))
            }
            MODE_DONE -> {
                showLoading()
                getTodos(App.get().gitLab.getTodos(Todo.STATE_DONE))
            }
            else -> throw IllegalStateException("$mode is not defined")
        }
    }

    private fun getTodos(observable: Single<Response<List<Todo>>>) {
        observable
                .mapResponseSuccessWithPaginationData()
                .with(this)
                .subscribe({
                    loading = false
                    swipeRefreshLayout.isRefreshing = false
                    if (it.body.isEmpty()) {
                        textMessage.visibility = View.VISIBLE
                        textMessage.setText(R.string.no_todos)
                    }
                    adapterTodos.setData(it.body)
                    nextPageUrl = it.paginationData.next
                    Timber.d("Next page url $nextPageUrl")
                }, {
                    loading = false
                    Timber.e(it)
                    swipeRefreshLayout.isRefreshing = false
                    textMessage.visibility = View.VISIBLE
                    textMessage.setText(R.string.connection_error)
                    adapterTodos.setData(null)
                    nextPageUrl = null
                })
    }

    fun loadMore() {
        if (view == null) {
            return
        }

        if (nextPageUrl == null) {
            return
        }
        loading = true
        adapterTodos.setLoading(true)
        Timber.d("loadMore called for " + nextPageUrl!!)
        App.get().gitLab.getTodosByUrl(nextPageUrl!!.toString())
                .mapResponseSuccessWithPaginationData()
                .with(this)
                .subscribe({
                    loading = false
                    adapterTodos.setLoading(false)
                    adapterTodos.addData(it.body)
                    nextPageUrl = it.paginationData.next
                    Timber.d("Next page url $nextPageUrl")
                }, {
                    loading = false
                    Timber.e(it)
                    adapterTodos.setLoading(false)
                })
    }

    private fun showLoading() {
        loading = true
        swipeRefreshLayout.isRefreshing = true
    }
}

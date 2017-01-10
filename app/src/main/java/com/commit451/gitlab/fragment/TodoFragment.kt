package com.commit451.gitlab.fragment

import android.net.Uri
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.TodoAdapter
import com.commit451.gitlab.model.api.Todo
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.rx.CustomResponseSingleObserver
import com.commit451.gitlab.util.LinkHeaderParser
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import timber.log.Timber

class TodoFragment : ButterKnifeFragment() {

    companion object {

        private val EXTRA_MODE = "extra_mode"

        val MODE_TODO = 0
        val MODE_DONE = 1

        fun newInstance(mode: Int): TodoFragment {
            val args = Bundle()
            args.putInt(EXTRA_MODE, mode)

            val fragment = TodoFragment()
            fragment.arguments = args
            return fragment
        }
    }

    @BindView(R.id.swipe_layout)
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.list)
    lateinit var listTodos: RecyclerView
    @BindView(R.id.message_text)
    lateinit var textMessage: TextView

    lateinit var layoutManagerTodos: LinearLayoutManager
    lateinit var adapterTodos: TodoAdapter

    var mode: Int = 0
    var nextPageUrl: Uri? = null
    var loading = false

    val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
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
        mode = arguments.getInt(EXTRA_MODE)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_todo, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapterTodos = TodoAdapter(object : TodoAdapter.Listener {
            override fun onTodoClicked(todo: Todo) {
                Navigator.navigateToUrl(activity, Uri.parse(todo.targetUrl), App.get().getAccount())
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
            else -> throw IllegalStateException(mode.toString() + " is not defined")
        }
    }

    fun getTodos(observable: Single<Response<List<Todo>>>) {
        observable
                .compose(this.bindToLifecycle<Response<List<Todo>>>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : CustomResponseSingleObserver<List<Todo>>() {

                    override fun error(e: Throwable) {
                        loading = false
                        Timber.e(e)
                        swipeRefreshLayout.isRefreshing = false
                        textMessage.visibility = View.VISIBLE
                        textMessage.setText(R.string.connection_error)
                        adapterTodos.setData(null)
                        nextPageUrl = null
                    }

                    override fun responseSuccess(todos: List<Todo>) {
                        loading = false

                        swipeRefreshLayout.isRefreshing = false
                        if (todos.isEmpty()) {
                            textMessage.visibility = View.VISIBLE
                            textMessage.setText(R.string.no_todos)
                        }
                        adapterTodos.setData(todos)
                        nextPageUrl = LinkHeaderParser.parse(response()).next
                        Timber.d("Next page url " + nextPageUrl)
                    }
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
                .compose(this.bindToLifecycle<Response<List<Todo>>>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : CustomResponseSingleObserver<List<Todo>>() {

                    override fun error(e: Throwable) {
                        loading = false
                        Timber.e(e)
                        adapterTodos.setLoading(false)
                    }

                    override fun responseSuccess(todos: List<Todo>) {
                        loading = false
                        adapterTodos.setLoading(false)
                        adapterTodos.addData(todos)
                        nextPageUrl = LinkHeaderParser.parse(response()).next
                        Timber.d("Next page url " + nextPageUrl)
                    }
                })
    }

    fun showLoading() {
        loading = true
        swipeRefreshLayout.isRefreshing = true
    }
}

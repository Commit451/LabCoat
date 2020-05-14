package com.commit451.gitlab.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.BaseAdapter
import com.commit451.gitlab.model.api.Todo
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.util.LoadHelper
import com.commit451.gitlab.viewHolder.CommitViewHolder
import com.commit451.gitlab.viewHolder.TodoViewHolder
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_commits.*
import kotlinx.android.synthetic.main.fragment_todo.swipeRefreshLayout
import kotlinx.android.synthetic.main.fragment_todo.textMessage

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

    private lateinit var adapter: BaseAdapter<Todo, TodoViewHolder>
    private lateinit var loadHelper: LoadHelper<Todo>

    private var mode: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mode = arguments?.getInt(EXTRA_MODE)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_todo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = BaseAdapter(
                onCreateViewHolder = { parent, _ ->
                    val viewHolder = CommitViewHolder.inflate(parent)
                    viewHolder.itemView.setOnClickListener {
                        val todo = adapter.items[viewHolder.adapterPosition]
                        val targetUrl = todo.targetUrl
                        if (targetUrl != null) {
                            Navigator.navigateToUrl(baseActivty, targetUrl, App.get().getAccount())
                        } else {
                            Snackbar.make(swipeRefreshLayout, R.string.not_a_valid_url, Snackbar.LENGTH_SHORT)
                        }
                    }
                    viewHolder
                },
                onBindViewHolder = { viewHolder, _, item -> viewHolder.bind(item) }
        )
        loadHelper = LoadHelper(
                lifecycleOwner = this,
                recyclerView = listCommits,
                baseAdapter = adapter,
                swipeRefreshLayout = swipeRefreshLayout,
                errorOrEmptyTextView = textMessage,
                loadInitial = {
                    when (mode) {
                        MODE_TODO -> {
                            gitLab.getTodos(Todo.STATE_PENDING)
                        }
                        MODE_DONE -> {
                            gitLab.getTodos(Todo.STATE_DONE)
                        }
                        else -> throw IllegalStateException("$mode is not defined")
                    }
                },
                loadMore = {
                    gitLab.loadAnyList(it)
                }
        )

        loadData()
    }

    override fun loadData() {
        loadHelper.load()
    }
}

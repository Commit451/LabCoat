package com.commit451.gitlab.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.BaseAdapter
import com.commit451.gitlab.extension.mapResponseSuccessWithPaginationData
import com.commit451.gitlab.model.api.User
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.util.LoadHelper
import com.commit451.gitlab.viewHolder.UserViewHolder
import kotlinx.android.synthetic.main.fragment_users.*
import kotlinx.android.synthetic.main.fragment_users.swipeRefreshLayout

class UsersFragment : BaseFragment() {

    companion object {

        private const val EXTRA_QUERY = "extra_query"

        fun newInstance(query: String? = null): UsersFragment {
            val args = Bundle()
            if (query != null) {
                args.putString(EXTRA_QUERY, query)
            } else {
                args.putString(EXTRA_QUERY, "")
            }

            val fragment = UsersFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private var query: String = ""

    private lateinit var adapter: BaseAdapter<User, UserViewHolder>
    private lateinit var loadHelper: LoadHelper<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        query = arguments?.getString(EXTRA_QUERY) ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_projects, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spanCount = 2
        val layoutManager = GridLayoutManager(activity, spanCount)
        layoutManager.spanSizeLookup = BaseAdapter.createSpanSizeLookup(adapter, spanCount)
        adapter = BaseAdapter(
                onCreateViewHolder = { parent, _ ->
                    val viewHolder = UserViewHolder.inflate(parent)
                    viewHolder.itemView.setOnClickListener {
                        val user = adapter.items[viewHolder.adapterPosition]
                        Navigator.navigateToUser(baseActivty, viewHolder.image, user)
                    }
                    viewHolder
                },
                onBindViewHolder = { viewHolder, _, item -> viewHolder.bind(item) }
        )
        loadHelper = LoadHelper(
                lifecycleOwner = this,
                recyclerView = listUsers,
                baseAdapter = adapter,
                layoutManager = GridLayoutManager(activity, 2),
                swipeRefreshLayout = swipeRefreshLayout,
                errorOrEmptyTextView = textMessage,
                loadInitial = {
                    gitLab.searchUsers(query)
                },
                loadMore = {
                    gitLab.loadAnyList(it)
                }
        )

        loadData()
    }

    override fun loadData() {
        if (query.isEmpty()) {
            return
        }
        loadHelper.load()
    }

    fun searchQuery(query: String) {
        this.query = query
        adapter.clear()
        loadData()
    }
}

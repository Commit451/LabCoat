package com.commit451.gitlab.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.UserAdapter
import com.commit451.gitlab.extension.mapResponseSuccessWithPaginationData
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.User
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.viewHolder.UserViewHolder
import kotlinx.android.synthetic.main.fragment_users.*
import timber.log.Timber

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

    lateinit var adapterUser: UserAdapter
    lateinit var layoutManagerUser: GridLayoutManager

    var query: String? = null
    var loading: Boolean = false
    var nextPageUrl: String? = null

    val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val visibleItemCount = layoutManagerUser.childCount
            val totalItemCount = layoutManagerUser.itemCount
            val firstVisibleItem = layoutManagerUser.findFirstVisibleItemPosition()
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !loading && nextPageUrl != null) {
                loadMore()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        query = arguments?.getString(EXTRA_QUERY)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_projects, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapterUser = UserAdapter(object : UserAdapter.Listener {
            override fun onUserClicked(user: User, userViewHolder: UserViewHolder) {
                Navigator.navigateToUser(baseActivty, userViewHolder.image, user)
            }
        })
        layoutManagerUser = GridLayoutManager(activity, 2)
        layoutManagerUser.spanSizeLookup = adapterUser.spanSizeLookup
        listUsers.layoutManager = layoutManagerUser
        listUsers.adapter = adapterUser
        listUsers.addOnScrollListener(onScrollListener)

        swipeRefreshLayout.setOnRefreshListener { loadData() }

        loadData()
    }

    override fun loadData() {
        loading = true
        if (view == null) {
            return
        }

        if (query.isNullOrEmpty()) {
            swipeRefreshLayout.isRefreshing = false
            return
        }

        textMessage.visibility = View.GONE
        swipeRefreshLayout.isRefreshing = true

        App.get().gitLab.searchUsers(query!!)
                .mapResponseSuccessWithPaginationData()
                .with(this)
                .subscribe({
                    swipeRefreshLayout.isRefreshing = false
                    loading = false
                    if (it.body.isEmpty()) {
                        textMessage.visibility = View.VISIBLE
                        textMessage.setText(R.string.no_users_found)
                    }
                    adapterUser.setData(it.body)
                    nextPageUrl = it.paginationData.next
                }, {
                    Timber.e(it)
                    loading = false
                    swipeRefreshLayout.isRefreshing = false
                    textMessage.setText(R.string.connection_error_users)
                    textMessage.visibility = View.VISIBLE
                    adapterUser.setData(null)
                })
    }

    fun loadMore() {
        loading = true
        adapterUser.setLoading(true)
        Timber.d("loadMore called for %s %s", nextPageUrl!!.toString(), query)
        App.get().gitLab.searchUsers(nextPageUrl!!.toString(), query!!)
                .mapResponseSuccessWithPaginationData()
                .with(this)
                .subscribe({
                    loading = false
                    swipeRefreshLayout.isRefreshing = false
                    adapterUser.addData(it.body)
                    nextPageUrl = it.paginationData.next
                    adapterUser.setLoading(false)
                }, {
                    Timber.e(it)
                    loading = false
                    swipeRefreshLayout.isRefreshing = false
                    adapterUser.setLoading(false)
                })
    }

    fun searchQuery(query: String) {
        this.query = query
        adapterUser.clearData()
        loadData()
    }
}

package com.commit451.gitlab.fragment

import android.net.Uri
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.UserAdapter
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.User
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.rx.CustomResponseSingleObserver
import com.commit451.gitlab.util.LinkHeaderParser
import com.commit451.gitlab.viewHolder.UserViewHolder
import timber.log.Timber

class UsersFragment : ButterKnifeFragment() {

    companion object {

        private val EXTRA_QUERY = "extra_query"

        @JvmOverloads
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

    @BindView(R.id.swipe_layout)
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.list)
    lateinit var listUsers: RecyclerView
    @BindView(R.id.message_text)
    lateinit var textMessage: TextView

    lateinit var adapterUser: UserAdapter
    lateinit var layoutManagerUser: GridLayoutManager

    var query: String? = null
    var loading: Boolean = false
    var nextPageUrl: Uri? = null

    val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
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
                .with(this)
                .subscribe(object : CustomResponseSingleObserver<List<User>>() {

                    override fun error(e: Throwable) {
                        Timber.e(e)
                        loading = false
                        swipeRefreshLayout.isRefreshing = false
                        textMessage.setText(R.string.connection_error_users)
                        textMessage.visibility = View.VISIBLE
                        adapterUser.setData(null)
                    }

                    override fun responseNonNullSuccess(users: List<User>) {
                        swipeRefreshLayout.isRefreshing = false
                        loading = false
                        if (users.isEmpty()) {
                            textMessage.visibility = View.VISIBLE
                            textMessage.setText(R.string.no_users_found)
                        }
                        adapterUser.setData(users)
                        nextPageUrl = LinkHeaderParser.parse(response()).next
                    }
                })
    }

    fun loadMore() {
        loading = true
        adapterUser.setLoading(true)
        Timber.d("loadMore called for %s %s", nextPageUrl!!.toString(), query)
        App.get().gitLab.searchUsers(nextPageUrl!!.toString(), query!!)
                .with(this)
                .subscribe(object : CustomResponseSingleObserver<List<User>>() {

                    override fun error(e: Throwable) {
                        Timber.e(e)
                        loading = false
                        swipeRefreshLayout.isRefreshing = false
                        adapterUser.setLoading(false)
                    }

                    override fun responseNonNullSuccess(users: List<User>) {
                        loading = false
                        swipeRefreshLayout.isRefreshing = false
                        adapterUser.addData(users)
                        nextPageUrl = LinkHeaderParser.parse(response()).next
                        adapterUser.setLoading(false)
                    }
                })
    }

    fun searchQuery(query: String) {
        this.query = query
        adapterUser.clearData()
        loadData()
    }
}

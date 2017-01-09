package com.commit451.gitlab.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.alexgwyn.recyclerviewsquire.DynamicGridLayoutManager
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.GroupAdapter
import com.commit451.gitlab.data.Prefs
import com.commit451.gitlab.event.CloseDrawerEvent
import com.commit451.gitlab.event.ReloadDataEvent
import com.commit451.gitlab.model.api.Group
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.rx.CustomResponseSingleObserver
import com.commit451.gitlab.util.LinkHeaderParser
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.Subscribe
import retrofit2.Response
import timber.log.Timber

/**
 * Displays the groups of the current user
 */
class GroupsActivity : BaseActivity() {

    companion object {

        fun newIntent(context: Context): Intent {
            val intent = Intent(context, GroupsActivity::class.java)
            return intent
        }
    }

    @BindView(R.id.drawer_layout)
    lateinit var drawerLayout: DrawerLayout
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar
    @BindView(R.id.swipe_layout)
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.list)
    lateinit var listGroups: RecyclerView
    @BindView(R.id.message_text)
    lateinit var textMessage: TextView

    lateinit var adapterGroup: GroupAdapter
    lateinit var layoutManager: DynamicGridLayoutManager

    var nextPageUrl: Uri? = null
    var loading = false

    val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount
            val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !loading && nextPageUrl != null) {
                loadMore()
            }
        }
    }

    val groupAdapterListener = GroupAdapter.Listener { group, groupViewHolder -> Navigator.navigateToGroup(this@GroupsActivity, groupViewHolder.image, group) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.get().prefs.startingView = Prefs.STARTING_VIEW_GROUPS
        setContentView(R.layout.activity_groups)
        ButterKnife.bind(this)
        App.bus().register(this)

        toolbar.setTitle(R.string.nav_groups)
        toolbar.setNavigationIcon(R.drawable.ic_menu_24dp)
        toolbar.setNavigationOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }
        swipeRefreshLayout.setOnRefreshListener { load() }
        textMessage.setOnClickListener { load() }
        layoutManager = DynamicGridLayoutManager(this)
        layoutManager.setMinimumWidthDimension(R.dimen.user_list_image_size)
        listGroups.layoutManager = layoutManager
        adapterGroup = GroupAdapter(groupAdapterListener)
        listGroups.adapter = adapterGroup
        listGroups.addOnScrollListener(onScrollListener)
        load()
    }

    override fun onDestroy() {
        super.onDestroy()
        App.bus().unregister(this)
    }

    fun load() {
        textMessage.visibility = View.GONE
        swipeRefreshLayout.isRefreshing = true

        nextPageUrl = null
        loading = true

        App.get().gitLab.groups
                .compose(this.bindToLifecycle<Response<List<Group>>>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : CustomResponseSingleObserver<List<Group>>() {

                    override fun error(e: Throwable) {
                        Timber.e(e)
                        swipeRefreshLayout.isRefreshing = false
                        loading = false
                        textMessage.visibility = View.VISIBLE
                        textMessage.setText(R.string.connection_error)
                    }

                    override fun responseSuccess(groups: List<Group>) {
                        loading = false
                        swipeRefreshLayout.isRefreshing = false
                        if (groups.isEmpty()) {
                            textMessage.setText(R.string.no_groups)
                            textMessage.visibility = View.VISIBLE
                            listGroups.visibility = View.GONE
                        } else {
                            adapterGroup.setGroups(groups)
                            textMessage.visibility = View.GONE
                            listGroups.visibility = View.VISIBLE
                            nextPageUrl = LinkHeaderParser.parse(response()).next
                        }
                    }
                })
    }

    fun loadMore() {
        if (nextPageUrl == null) {
            return
        }

        swipeRefreshLayout.isRefreshing = true

        loading = true

        Timber.d("loadMore called for %s", nextPageUrl)
        App.get().gitLab.getGroups(nextPageUrl!!.toString())
                .compose(this.bindToLifecycle<Response<List<Group>>>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : CustomResponseSingleObserver<List<Group>>() {

                    override fun error(e: Throwable) {
                        Timber.e(e)
                        loading = false
                    }

                    override fun responseSuccess(groups: List<Group>) {
                        loading = false
                        adapterGroup.addGroups(groups)
                        nextPageUrl = LinkHeaderParser.parse(response()).next
                    }
                })
    }

    @Subscribe
    fun onCloseDrawerEvent(event: CloseDrawerEvent) {
        drawerLayout.closeDrawers()
    }

    @Subscribe
    fun onReloadData(event: ReloadDataEvent) {
        load()
    }
}

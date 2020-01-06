package com.commit451.gitlab.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.RecyclerView
import com.commit451.aloy.DynamicGridLayoutManager
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.GroupAdapter
import com.commit451.gitlab.data.Prefs
import com.commit451.gitlab.event.CloseDrawerEvent
import com.commit451.gitlab.event.ReloadDataEvent
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.Group
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.rx.CustomResponseSingleObserver
import com.commit451.gitlab.util.LinkHeaderParser
import com.commit451.gitlab.viewHolder.GroupViewHolder
import kotlinx.android.synthetic.main.activity_groups.*
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber

/**
 * Displays the groups of the current user
 */
class GroupsActivity : BaseActivity() {

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, GroupsActivity::class.java)
        }
    }

    private lateinit var adapterGroup: GroupAdapter
    private lateinit var layoutManager: DynamicGridLayoutManager

    private var nextPageUrl: Uri? = null
    private var loading = false

    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount
            val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !loading && nextPageUrl != null) {
                loadMore()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Prefs.startingView = Prefs.STARTING_VIEW_GROUPS
        setContentView(R.layout.activity_groups)
        App.bus().register(this)

        toolbar.setTitle(R.string.nav_groups)
        toolbar.setNavigationIcon(R.drawable.ic_menu_24dp)
        toolbar.setNavigationOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }
        swipeRefreshLayout.setOnRefreshListener { load() }
        textMessage.setOnClickListener { load() }
        layoutManager = DynamicGridLayoutManager(this)
        layoutManager.setMinimumSpanSize(resources.getDimensionPixelSize(R.dimen.user_list_image_size))
        listGroups.layoutManager = layoutManager
        adapterGroup = GroupAdapter(this, object : GroupAdapter.Listener {
            override fun onGroupClicked(group: Group, groupViewHolder: GroupViewHolder) {
                Navigator.navigateToGroup(this@GroupsActivity, groupViewHolder.image, group)
            }
        })
        listGroups.adapter = adapterGroup
        listGroups.addOnScrollListener(onScrollListener)
        load()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
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

        App.get().gitLab.getGroups()
                .with(this)
                .subscribe(object : CustomResponseSingleObserver<List<Group>>() {

                    override fun error(e: Throwable) {
                        Timber.e(e)
                        swipeRefreshLayout.isRefreshing = false
                        loading = false
                        textMessage.visibility = View.VISIBLE
                        textMessage.setText(R.string.connection_error)
                    }

                    override fun responseNonNullSuccess(groups: List<Group>) {
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
                .with(this)
                .subscribe(object : CustomResponseSingleObserver<List<Group>>() {

                    override fun error(e: Throwable) {
                        Timber.e(e)
                        loading = false
                    }

                    override fun responseNonNullSuccess(groups: List<Group>) {
                        loading = false
                        adapterGroup.addGroups(groups)
                        nextPageUrl = LinkHeaderParser.parse(response()).next
                    }
                })
    }

    @Suppress("UNUSED_PARAMETER")
    @Subscribe
    fun onEvent(event: CloseDrawerEvent) {
        drawerLayout.closeDrawers()
    }

    @Suppress("UNUSED_PARAMETER")
    @Subscribe
    fun onEvent(event: ReloadDataEvent) {
        load()
    }
}

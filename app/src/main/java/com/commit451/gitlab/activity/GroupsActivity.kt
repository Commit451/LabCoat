package com.commit451.gitlab.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.GravityCompat
import com.commit451.aloy.DynamicGridLayoutManager
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.BaseAdapter
import com.commit451.gitlab.data.Prefs
import com.commit451.gitlab.event.CloseDrawerEvent
import com.commit451.gitlab.event.ReloadDataEvent
import com.commit451.gitlab.model.api.Group
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.util.LoadHelper
import com.commit451.gitlab.viewHolder.GroupViewHolder
import kotlinx.android.synthetic.main.activity_groups.*
import org.greenrobot.eventbus.Subscribe

/**
 * Displays the groups of the current user
 */
class GroupsActivity : BaseActivity() {

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, GroupsActivity::class.java)
        }
    }

    private lateinit var adapter: BaseAdapter<Group, GroupViewHolder>
    private lateinit var loadHelper: LoadHelper<Group>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Prefs.startingView = Prefs.STARTING_VIEW_GROUPS
        setContentView(R.layout.activity_groups)
        App.bus().register(this)

        toolbar.setTitle(R.string.nav_groups)
        toolbar.setNavigationIcon(R.drawable.ic_menu_24dp)
        toolbar.setNavigationOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }
        val layoutManager = DynamicGridLayoutManager(this)
        layoutManager.setMinimumSpanSize(resources.getDimensionPixelSize(R.dimen.user_list_image_size))

        val colors: IntArray = resources.getIntArray(R.array.cool_colors)
        adapter = BaseAdapter(
                onCreateViewHolder = { parent, _ ->
                    val viewHolder = GroupViewHolder.inflate(parent)
                    viewHolder.itemView.setOnClickListener {
                        val group = adapter.items[viewHolder.adapterPosition]
                        Navigator.navigateToGroup(this@GroupsActivity, viewHolder.image, group)
                    }
                    viewHolder
                },
                onBindViewHolder = { viewHolder, position, item -> viewHolder.bind(item, colors[position % colors.size]) }
        )
        loadHelper = LoadHelper(
                lifecycleOwner = this,
                recyclerView = listGroups,
                baseAdapter = adapter,
                layoutManager = layoutManager,
                swipeRefreshLayout = swipeRefreshLayout,
                errorOrEmptyTextView = textMessage,
                loadInitial = { gitLab.getGroups() },
                loadMore = { gitLab.loadAnyList(it) }
        )
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
        loadHelper.load()
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

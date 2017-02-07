package com.commit451.gitlab.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.Toolbar
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.ProjectPagerAdapter
import com.commit451.gitlab.event.CloseDrawerEvent
import com.commit451.gitlab.navigation.Navigator
import org.greenrobot.eventbus.Subscribe


/**
 * Shows the projects
 */
class ProjectsActivity : BaseActivity() {

    companion object {

        fun newIntent(context: Context): Intent {
            val intent = Intent(context, ProjectsActivity::class.java)
            return intent
        }
    }

    @BindView(R.id.toolbar) lateinit var toolbar: Toolbar
    @BindView(R.id.tabs) lateinit var tabLayout: TabLayout
    @BindView(R.id.pager) lateinit var viewPager: ViewPager
    @BindView(R.id.drawer_layout) lateinit var drawerLayout: DrawerLayout

    val onMenuItemClickListener = Toolbar.OnMenuItemClickListener { item ->
        when (item.itemId) {
            R.id.action_search -> {
                Navigator.navigateToSearch(this@ProjectsActivity)
                return@OnMenuItemClickListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_projects)
        ButterKnife.bind(this)
        App.bus().register(this)

        toolbar.setTitle(R.string.projects)
        toolbar.setNavigationIcon(R.drawable.ic_menu_24dp)
        toolbar.setNavigationOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }
        toolbar.inflateMenu(R.menu.search)
        toolbar.setOnMenuItemClickListener(onMenuItemClickListener)
        viewPager.adapter = ProjectPagerAdapter(this, supportFragmentManager)
        tabLayout.setupWithViewPager(viewPager)
    }

    override fun onDestroy() {
        super.onDestroy()
        App.bus().unregister(this)
    }

    @Subscribe
    fun onEvent(event: CloseDrawerEvent) {
        drawerLayout.closeDrawers()
    }
}

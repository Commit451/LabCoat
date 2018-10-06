package com.commit451.gitlab.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import androidx.core.view.GravityCompat
import androidx.viewpager.widget.ViewPager
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.widget.Toolbar
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.TodoPagerAdapter
import com.commit451.gitlab.data.Prefs
import com.commit451.gitlab.event.CloseDrawerEvent
import org.greenrobot.eventbus.Subscribe

/**
 * Shows the projects
 */
class TodosActivity : BaseActivity() {

    companion object {

        fun newIntent(context: Context): Intent {
            val intent = Intent(context, TodosActivity::class.java)
            return intent
        }
    }

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar
    @BindView(R.id.tabs)
    lateinit var tabLayout: TabLayout
    @BindView(R.id.pager)
    lateinit var viewPager: androidx.viewpager.widget.ViewPager
    @BindView(R.id.drawer_layout)
    lateinit var drawerLayout: androidx.drawerlayout.widget.DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Prefs.startingView = Prefs.STARTING_VIEW_TODOS
        setContentView(R.layout.activity_todos)
        ButterKnife.bind(this)
        App.bus().register(this)

        toolbar.setTitle(R.string.nav_todos)
        toolbar.setNavigationIcon(R.drawable.ic_menu_24dp)
        toolbar.setNavigationOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }
        viewPager.adapter = TodoPagerAdapter(this, supportFragmentManager)
        tabLayout.setupWithViewPager(viewPager)
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

    override fun hasBrowsableLinks(): Boolean {
        return true
    }

    @Suppress("UNUSED_PARAMETER")
    @Subscribe
    fun onEvent(event: CloseDrawerEvent) {
        drawerLayout.closeDrawers()
    }
}

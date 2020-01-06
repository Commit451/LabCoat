package com.commit451.gitlab.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.ProjectsPagerAdapter
import com.commit451.gitlab.event.CloseDrawerEvent
import com.commit451.gitlab.navigation.Navigator
import kotlinx.android.synthetic.main.activity_projects.*
import org.greenrobot.eventbus.Subscribe

/**
 * Shows the projects
 */
class ProjectsActivity : BaseActivity() {

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, ProjectsActivity::class.java)
        }
    }

    private val onMenuItemClickListener = Toolbar.OnMenuItemClickListener { item ->
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
        App.bus().register(this)

        toolbar.setTitle(R.string.projects)
        toolbar.setNavigationIcon(R.drawable.ic_menu_24dp)
        toolbar.setNavigationOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }
        toolbar.inflateMenu(R.menu.search)
        toolbar.setOnMenuItemClickListener(onMenuItemClickListener)
        viewPager.adapter = ProjectsPagerAdapter(this, supportFragmentManager)
        tabLayout.setupWithViewPager(viewPager)
    }

    override fun onDestroy() {
        super.onDestroy()
        App.bus().unregister(this)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    @Subscribe
    fun onEvent(event: CloseDrawerEvent) {
        drawerLayout.closeDrawers()
    }
}

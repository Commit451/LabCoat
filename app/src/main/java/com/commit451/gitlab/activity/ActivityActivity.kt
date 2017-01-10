package com.commit451.gitlab.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.Toolbar
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.data.Prefs
import com.commit451.gitlab.event.CloseDrawerEvent
import com.commit451.gitlab.fragment.FeedFragment
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber

/**
 * Displays the current users projects feed
 */
class ActivityActivity : BaseActivity() {

    companion object {

        private val TAG_FEED_FRAGMENT = "feed_fragment"

        fun newIntent(context: Context): Intent {
            val intent = Intent(context, ActivityActivity::class.java)
            return intent
        }
    }

    @BindView(R.id.drawer_layout)
    lateinit var drawerLayout: DrawerLayout
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.get().prefs.setStartingView(Prefs.STARTING_VIEW_ACTIVITY)
        setContentView(R.layout.activity_activity)
        ButterKnife.bind(this)

        App.bus().register(this)

        toolbar.setTitle(R.string.nav_activity)
        toolbar.setNavigationIcon(R.drawable.ic_menu_24dp)
        toolbar.setNavigationOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }

        var feedFragment: FeedFragment? = supportFragmentManager.findFragmentByTag(TAG_FEED_FRAGMENT) as? FeedFragment
        if (feedFragment == null) {
            var feedUri = App.get().getAccount().serverUrl

            feedUri = feedUri.buildUpon()
                    .appendPath("dashboard")
                    .appendPath("projects.atom")
                    .build()
            Timber.d("Showing activity feed for: %s", feedUri.toString())

            feedFragment = FeedFragment.newInstance(feedUri)
            supportFragmentManager.beginTransaction()
                    .replace(R.id.root_fragment, feedFragment, TAG_FEED_FRAGMENT)
                    .commit()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        App.bus().unregister(this)
    }

    @Subscribe
    fun onCloseDrawerEvent(event: CloseDrawerEvent) {
        drawerLayout.closeDrawers()
    }
}

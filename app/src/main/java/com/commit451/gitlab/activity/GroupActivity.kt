package com.commit451.gitlab.activity

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.palette.graphics.Palette
import androidx.viewpager.widget.ViewPager
import butterknife.BindView
import butterknife.ButterKnife
import coil.api.load
import com.commit451.addendum.themeAttrColor
import com.commit451.alakazam.navigationBarColorAnimator
import com.commit451.easel.Easel
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.GroupPagerAdapter
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.image.PaletteImageViewTarget
import com.commit451.gitlab.model.api.Group
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import timber.log.Timber

/**
 * See the things about the group
 */
class GroupActivity : BaseActivity() {

    companion object {

        private const val KEY_GROUP = "key_group"
        private const val KEY_GROUP_ID = "key_group_id"

        fun newIntent(context: Context, group: Group): Intent {
            val intent = Intent(context, GroupActivity::class.java)
            intent.putExtra(KEY_GROUP, group)
            return intent
        }

        fun newIntent(context: Context, groupId: Long): Intent {
            val intent = Intent(context, GroupActivity::class.java)
            intent.putExtra(KEY_GROUP_ID, groupId)
            return intent
        }
    }

    @BindView(R.id.root)
    lateinit var root: View
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar
    @BindView(R.id.collapsing_toolbar)
    lateinit var collapsingToolbarLayout: CollapsingToolbarLayout
    @BindView(R.id.viewpager)
    lateinit var viewPager: ViewPager
    @BindView(R.id.tabs)
    lateinit var tabLayout: TabLayout
    @BindView(R.id.backdrop)
    lateinit var backdrop: ImageView
    @BindView(R.id.progress)
    lateinit var progress: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group)
        ButterKnife.bind(this)

        // Default content and scrim colors

        toolbar.setNavigationIcon(R.drawable.ic_back_24dp)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        if (intent.hasExtra(KEY_GROUP)) {
            val group = intent.getParcelableExtra<Group>(KEY_GROUP)!!
            bind(group)
        } else {
            progress.visibility = View.VISIBLE
            val groupId = intent.getLongExtra(KEY_GROUP_ID, -1)
            App.get().gitLab.getGroup(groupId)
                    .with(this)
                    .subscribe({
                        progress.visibility = View.GONE
                        bind(it)
                    }, {
                        Timber.e(it)
                        progress.visibility = View.GONE
                        showError()
                    })
        }
    }

    override fun onBackPressed() {
        supportFinishAfterTransition()
    }

    override fun hasBrowsableLinks(): Boolean {
        return true
    }

    fun bind(group: Group) {
        val paletteTarget = PaletteImageViewTarget(backdrop) {
            bindPalette(it)
        }
        backdrop.load(group.avatarUrl) {
            target(paletteTarget)
        }

        viewPager.adapter = GroupPagerAdapter(this, supportFragmentManager, group)
        tabLayout.setupWithViewPager(viewPager)
    }

    private fun bindPalette(palette: Palette) {
        val animationTime = 1000
        val vibrantColor = palette.getVibrantColor(this.themeAttrColor(R.attr.colorAccent))
        val darkerColor = Easel.darkerColor(vibrantColor)

        window.navigationBarColorAnimator(darkerColor)
                .setDuration(animationTime.toLong())
                .start()

        ObjectAnimator.ofObject(collapsingToolbarLayout, "contentScrimColor", ArgbEvaluator(),
                this.themeAttrColor(R.attr.colorPrimary), vibrantColor)
                .setDuration(animationTime.toLong())
                .start()

        ObjectAnimator.ofObject(collapsingToolbarLayout, "statusBarScrimColor", ArgbEvaluator(),
                this.themeAttrColor(R.attr.colorPrimaryDark), darkerColor)
                .setDuration(animationTime.toLong())
                .start()

        ObjectAnimator.ofObject(toolbar, "titleTextColor", ArgbEvaluator(),
                Color.WHITE, palette.getDarkMutedColor(Color.BLACK))
                .setDuration(animationTime.toLong())
                .start()
    }

    private fun showError() {
        Snackbar.make(root, R.string.connection_error, Snackbar.LENGTH_SHORT)
                .show()
    }
}

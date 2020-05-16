package com.commit451.gitlab.activity

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.palette.graphics.Palette
import coil.api.load
import com.commit451.addendum.design.snackbar
import com.commit451.addendum.themeAttrColor
import com.commit451.alakazam.navigationBarColorAnimator
import com.commit451.easel.Easel
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.GroupPagerAdapter
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.image.PaletteImageViewTarget
import com.commit451.gitlab.model.api.Group
import kotlinx.android.synthetic.main.activity_group.*
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group)

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
            allowHardware(false)
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
        root.snackbar(R.string.connection_error)
    }
}

package com.commit451.gitlab.activity

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v7.graphics.Palette
import android.support.v7.widget.Toolbar
import android.widget.ImageView
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.alakazam.Alakazam
import com.commit451.easel.Easel
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.fragment.FeedFragment
import com.commit451.gitlab.model.api.UserBasic
import com.commit451.gitlab.transformation.PaletteTransformation
import com.commit451.gitlab.util.ImageUtil
import org.parceler.Parcels

/**
 * User activity, which shows the user!
 */
class UserActivity : BaseActivity() {

    companion object {

        private val KEY_USER = "user"

        fun newIntent(context: Context, user: UserBasic): Intent {
            val intent = Intent(context, UserActivity::class.java)
            intent.putExtra(KEY_USER, Parcels.wrap(user))
            return intent
        }
    }

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar
    @BindView(R.id.collapsing_toolbar)
    lateinit var collapsingToolbarLayout: CollapsingToolbarLayout
    @BindView(R.id.backdrop)
    lateinit var backdrop: ImageView

    lateinit var user: UserBasic

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        ButterKnife.bind(this)
        user = Parcels.unwrap<UserBasic>(intent.getParcelableExtra<Parcelable>(KEY_USER))

        // Default content and scrim colors
        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE)
        collapsingToolbarLayout.setExpandedTitleColor(Color.WHITE)

        toolbar.setNavigationIcon(R.drawable.ic_back_24dp)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        toolbar.title = user.username
        val url = ImageUtil.getAvatarUrl(user, resources.getDimensionPixelSize(R.dimen.user_header_image_size))

        App.get().picasso
                .load(url)
                .transform(PaletteTransformation.instance())
                .into(backdrop, object : PaletteTransformation.PaletteCallback(backdrop) {
                    override fun onSuccess(palette: Palette) {
                        bindPalette(palette)
                    }

                    override fun onError() {}
                })

        if (savedInstanceState == null) {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction
                    .add(R.id.user_feed, FeedFragment.newInstance(user.feedUrl))
                    .commit()
        }
    }

    override fun onBackPressed() {
        supportFinishAfterTransition()
    }

    fun bindPalette(palette: Palette) {
        val animationTime = 1000
        val vibrantColor = palette.getVibrantColor(Easel.getThemeAttrColor(this, R.attr.colorPrimary))
        val darkerColor = Easel.getDarkerColor(vibrantColor)

        if (Build.VERSION.SDK_INT >= 21) {
            Alakazam.navigationBarColorAnimator(window, darkerColor)
                    .setDuration(animationTime.toLong())
                    .start()
            window.statusBarColor = darkerColor
        }

        ObjectAnimator.ofObject(collapsingToolbarLayout, "contentScrimColor", ArgbEvaluator(),
                Easel.getThemeAttrColor(this, R.attr.colorPrimary), vibrantColor)
                .setDuration(animationTime.toLong())
                .start()

        ObjectAnimator.ofObject(collapsingToolbarLayout, "statusBarScrimColor", ArgbEvaluator(),
                Easel.getThemeAttrColor(this, R.attr.colorPrimaryDark), darkerColor)
                .setDuration(animationTime.toLong())
                .start()

        ObjectAnimator.ofObject(toolbar, "titleTextColor", ArgbEvaluator(),
                Color.WHITE, palette.getDarkMutedColor(Color.BLACK))
                .setDuration(animationTime.toLong())
                .start()
    }
}

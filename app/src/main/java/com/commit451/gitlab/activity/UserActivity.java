package com.commit451.gitlab.activity;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.afollestad.appthemeengine.util.ATEUtil;
import com.commit451.easel.Easel;
import com.commit451.gitlab.R;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.fragment.FeedFragment;
import com.commit451.gitlab.model.api.UserBasic;
import com.commit451.gitlab.util.AppThemeUtil;
import com.commit451.gitlab.transformation.PaletteTransformation;
import com.commit451.gitlab.util.ImageUtil;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * User activity, which shows the user!
 */
public class UserActivity extends BaseActivity implements ATEActivityThemeCustomizer {

    @Override
    public int getActivityTheme() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", true) ?
                R.style.Activity_User : R.style.ActivityLight_User;
    }

    private static final String KEY_USER = "user";

    public static Intent newInstance(Context context, UserBasic user) {
        Intent intent = new Intent(context, UserActivity.class);
        intent.putExtra(KEY_USER, Parcels.wrap(user));
        return intent;
    }

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.collapsing_toolbar) CollapsingToolbarLayout mCollapsingToolbarLayout;
    @BindView(R.id.backdrop) ImageView mBackdrop;

    UserBasic mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        ButterKnife.bind(this);
        mUser = Parcels.unwrap(getIntent().getParcelableExtra(KEY_USER));

        // Default content and scrim colors
        mCollapsingToolbarLayout.setContentScrimColor(
                Config.primaryColor(this, AppThemeUtil.resolveThemeKey(this)));
        mCollapsingToolbarLayout.setStatusBarScrimColor(
                Config.primaryColorDark(this, AppThemeUtil.resolveThemeKey(this)));
        mCollapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
        mCollapsingToolbarLayout.setExpandedTitleColor(Color.WHITE);

        mToolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mToolbar.setTitle(mUser.getUsername());
        Uri url = ImageUtil.getAvatarUrl(mUser, getResources().getDimensionPixelSize(R.dimen.user_header_image_size));

        GitLabClient.getPicasso()
                .load(url)
                .transform(PaletteTransformation.instance())
                .into(mBackdrop, new PaletteTransformation.PaletteCallback(mBackdrop) {
                    @Override
                    protected void onSuccess(Palette palette) {
                        bindPalette(palette);
                    }

                    @Override
                    public void onError() {}
                });

        if (savedInstanceState == null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.user_feed, FeedFragment.newInstance(mUser.getFeedUrl())).commit();
        }
    }

    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
    }

    private void bindPalette(Palette palette) {
        int animationTime = 1000;
        int vibrantColor = palette.getVibrantColor(AppThemeUtil.resolvePrimaryColor(this));
        int darkerColor = ATEUtil.darkenColor(vibrantColor);

        if (Build.VERSION.SDK_INT >= 21) {
            Easel.getNavigationBarColorAnimator(getWindow(), darkerColor)
                    .setDuration(animationTime)
                    .start();
            getWindow().setStatusBarColor(darkerColor);
        }

        ObjectAnimator.ofObject(mCollapsingToolbarLayout, "contentScrimColor", new ArgbEvaluator(),
                ((ColorDrawable)mCollapsingToolbarLayout.getContentScrim()).getColor(), vibrantColor)
                .setDuration(animationTime)
                .start();

        ObjectAnimator.ofObject(mCollapsingToolbarLayout, "statusBarScrimColor", new ArgbEvaluator(),
                ((ColorDrawable) mCollapsingToolbarLayout.getStatusBarScrim()).getColor(), darkerColor)
                .setDuration(animationTime)
                .start();

        ObjectAnimator.ofObject(mToolbar, "titleTextColor", new ArgbEvaluator(),
                Color.WHITE, palette.getDarkMutedColor(Color.BLACK))
                .setDuration(animationTime)
                .start();
    }
}

package com.commit451.gitlab.activity;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.commit451.alakazam.Alakazam;
import com.commit451.easel.Easel;
import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.fragment.FeedFragment;
import com.commit451.gitlab.model.api.UserBasic;
import com.commit451.gitlab.transformation.PaletteTransformation;
import com.commit451.gitlab.util.ImageUtil;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * User activity, which shows the user!
 */
public class UserActivity extends BaseActivity {

    private static final String KEY_USER = "user";

    public static Intent newIntent(Context context, UserBasic user) {
        Intent intent = new Intent(context, UserActivity.class);
        intent.putExtra(KEY_USER, Parcels.wrap(user));
        return intent;
    }

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.backdrop)
    ImageView backdrop;

    UserBasic user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        ButterKnife.bind(this);
        user = Parcels.unwrap(getIntent().getParcelableExtra(KEY_USER));

        // Default content and scrim colors
        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
        collapsingToolbarLayout.setExpandedTitleColor(Color.WHITE);

        toolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setTitle(user.getUsername());
        Uri url = ImageUtil.getAvatarUrl(user, getResources().getDimensionPixelSize(R.dimen.user_header_image_size));

        App.get().getPicasso()
                .load(url)
                .transform(PaletteTransformation.instance())
                .into(backdrop, new PaletteTransformation.PaletteCallback(backdrop) {
                    @Override
                    protected void onSuccess(Palette palette) {
                        bindPalette(palette);
                    }

                    @Override
                    public void onError() {
                    }
                });

        if (savedInstanceState == null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.user_feed, FeedFragment.newInstance(user.getFeedUrl())).commit();
        }
    }

    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
    }

    private void bindPalette(Palette palette) {
        int animationTime = 1000;
        int vibrantColor = palette.getVibrantColor(Easel.getThemeAttrColor(this, R.attr.colorPrimary));
        int darkerColor = Easel.getDarkerColor(vibrantColor);

        if (Build.VERSION.SDK_INT >= 21) {
            Alakazam.navigationBarColorAnimator(getWindow(), darkerColor)
                    .setDuration(animationTime)
                    .start();
            getWindow().setStatusBarColor(darkerColor);
        }

        ObjectAnimator.ofObject(collapsingToolbarLayout, "contentScrimColor", new ArgbEvaluator(),
                Easel.getThemeAttrColor(this, R.attr.colorPrimary), vibrantColor)
                .setDuration(animationTime)
                .start();

        ObjectAnimator.ofObject(collapsingToolbarLayout, "statusBarScrimColor", new ArgbEvaluator(),
                Easel.getThemeAttrColor(this, R.attr.colorPrimaryDark), darkerColor)
                .setDuration(animationTime)
                .start();

        ObjectAnimator.ofObject(toolbar, "titleTextColor", new ArgbEvaluator(),
                Color.WHITE, palette.getDarkMutedColor(Color.BLACK))
                .setDuration(animationTime)
                .start();
    }
}

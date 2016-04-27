package com.commit451.gitlab.activity;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.commit451.easel.Easel;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.GroupPagerAdapter;
import com.commit451.gitlab.api.EasyCallback;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.api.Group;
import com.commit451.gitlab.model.api.GroupDetail;
import com.commit451.gitlab.transformation.PaletteTransformation;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Callback;
import timber.log.Timber;

/**
 * See the things about the group
 */
public class GroupActivity extends BaseActivity {

    private static final String KEY_GROUP = "key_group";
    private static final String KEY_GROUP_ID = "key_group_id";

    public static Intent newInstance(Context context, Group group) {
        Intent intent = new Intent(context, GroupActivity.class);
        intent.putExtra(KEY_GROUP, Parcels.wrap(group));
        return intent;
    }

    public static Intent newInstance(Context context, long groupId) {
        Intent intent = new Intent(context, GroupActivity.class);
        intent.putExtra(KEY_GROUP_ID, groupId);
        return intent;
    }

    @BindView(R.id.root) View mRoot;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.collapsing_toolbar) CollapsingToolbarLayout mCollapsingToolbarLayout;
    @BindView(R.id.viewpager) ViewPager mViewPager;
    @BindView(R.id.tabs) TabLayout mTabLayout;
    @BindView(R.id.backdrop) ImageView mBackdrop;
    @BindView(R.id.progress) View mProgress;

    private final Callback<GroupDetail> mGroupCallback = new EasyCallback<GroupDetail>() {
        @Override
        public void onResponse(@NonNull GroupDetail response) {
            mProgress.setVisibility(View.GONE);
            bind(response);
        }

        @Override
        public void onAllFailure(Throwable t) {
            Timber.e(t, null);
            mProgress.setVisibility(View.GONE);
            showError();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        ButterKnife.bind(this);

        // Default content and scrim colors

        mToolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        if (getIntent().hasExtra(KEY_GROUP)) {
            Group group = Parcels.unwrap(getIntent().getParcelableExtra(KEY_GROUP));
            bind(group);
        } else {
            mProgress.setVisibility(View.VISIBLE);
            long groupId = getIntent().getLongExtra(KEY_GROUP_ID, -1);
            GitLabClient.instance().getGroup(groupId).enqueue(mGroupCallback);
        }
    }

    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
    }

    private void bind(Group group) {
        GitLabClient.getPicasso()
                .load(group.getAvatarUrl())
                .transform(PaletteTransformation.instance())
                .into(mBackdrop, new PaletteTransformation.PaletteCallback(mBackdrop) {
                    @Override
                    protected void onSuccess(Palette palette) {
                        bindPalette(palette);
                    }

                    @Override
                    public void onError() {}
                });

        mViewPager.setAdapter(new GroupPagerAdapter(this, getSupportFragmentManager(), group));
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private void bindPalette(Palette palette) {
        int animationTime = 1000;
        int vibrantColor = palette.getVibrantColor(Easel.getThemeAttrColor(this, R.attr.colorAccent));
        int darkerColor = Easel.getDarkerColor(vibrantColor);

        if (Build.VERSION.SDK_INT >= 21) {
            Easel.getNavigationBarColorAnimator(getWindow(), darkerColor)
                    .setDuration(animationTime)
                    .start();
        }

        ObjectAnimator.ofObject(mCollapsingToolbarLayout, "contentScrimColor", new ArgbEvaluator(),
                ((ColorDrawable) mCollapsingToolbarLayout.getContentScrim()).getColor(), vibrantColor)
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

    private void showError() {
        Snackbar.make(mRoot, R.string.connection_error, Snackbar.LENGTH_SHORT)
                .show();
    }
}

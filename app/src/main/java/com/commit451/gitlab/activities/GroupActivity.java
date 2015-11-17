package com.commit451.gitlab.activities;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.graphics.Palette;
import android.support.v7.internal.widget.ThemeUtils;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.easel.Easel;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.ProjectsAdapter;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.Group;
import com.commit451.gitlab.model.Project;
import com.commit451.gitlab.tools.NavigationManager;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.parceler.Parcels;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * See the things about the group
 * Created by John on 10/14/15.
 */
public class GroupActivity extends BaseActivity {

    private static final String KEY_GROUP = "key_group";

    public static Intent newInstance(Context context, Group group) {
        Intent intent = new Intent(context, GroupActivity.class);
        intent.putExtra(KEY_GROUP, Parcels.wrap(group));
        return intent;
    }

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.collapsing_toolbar) CollapsingToolbarLayout mCollapsingToolbarLayout;
    @Bind(R.id.backdrop) ImageView mBackdrop;
    @Bind(R.id.list) RecyclerView mProjectsRecyclerView;
    ProjectsAdapter mProjectsAdapter;
    @Bind(R.id.progress) ProgressWheel mProgress;
    @Bind(R.id.message) TextView mMessageView;
    @OnClick(R.id.fab_add_user)
    public void onClickAddUser() {
        startActivity(AddUserActivity.newInstance(this, mGroup.getId()));
    }

    private final Target mImageLoadTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            mBackdrop.setImageBitmap(bitmap);
            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                public void onGenerated(Palette p) {
                    bindPalette(p);
                }
            });
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {}

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {}
    };

    private final Callback<Group> mGroupCallback = new Callback<Group>() {
        @Override
        public void onResponse(Response<Group> response, Retrofit retrofit) {
            mProgress.setVisibility(View.GONE);
            if (!response.isSuccess()) {
                showMessage(R.string.connection_error);
                return;
            }
            mGroup = response.body();
            if (mGroup.getProjects().isEmpty()) {
                showMessage(R.string.no_projects);
            } else {
                mMessageView.setVisibility(View.GONE);
                mProjectsRecyclerView.setVisibility(View.VISIBLE);
                mProjectsAdapter.setData(mGroup.getProjects());
            }
        }

        @Override
        public void onFailure(Throwable t) {
            mProgress.setVisibility(View.GONE);
            showMessage(R.string.connection_error);
        }
    };

    private final ProjectsAdapter.Listener mProjectsAdapterListener = new ProjectsAdapter.Listener() {
        @Override
        public void onProjectClicked(Project project) {
            NavigationManager.navigateToProject(GroupActivity.this, project);
        }
    };

    Group mGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        ButterKnife.bind(this);
        mGroup = Parcels.unwrap(getIntent().getParcelableExtra(KEY_GROUP));
        mToolbar.setTitle(mGroup.getName());
        mToolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        Picasso.with(this)
                .load(mGroup.getAvatarUrl())
                .into(mImageLoadTarget);
        mProjectsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mProjectsAdapter = new ProjectsAdapter(this, mProjectsAdapterListener);
        mProjectsRecyclerView.setAdapter(mProjectsAdapter);
        load();
    }

    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
    }

    private void bindPalette(Palette palette) {
        int animationTime = 1000;
        int vibrantColor = palette.getVibrantColor(ThemeUtils.getThemeAttrColor(this, R.attr.colorPrimary));
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

        ObjectAnimator.ofObject(mProgress, "barColor", new ArgbEvaluator(),
                mProgress.getBarColor(), vibrantColor)
                .setDuration(animationTime)
                .start();
    }

    private void load() {
        mProgress.setVisibility(View.VISIBLE);
        GitLabClient.instance().getGroupDetails(mGroup.getId()).enqueue(mGroupCallback);
    }

    private void showMessage(int stringResId) {
        mMessageView.setVisibility(View.VISIBLE);
        mMessageView.setText(stringResId);
        mProjectsRecyclerView.setVisibility(View.GONE);
    }
}

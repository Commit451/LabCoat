package com.commit451.gitlab.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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

import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.FeedAdapter;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.User;
import com.commit451.gitlab.model.rss.Entry;
import com.commit451.gitlab.model.rss.UserFeed;
import com.commit451.gitlab.tools.ColorUtil;
import com.commit451.gitlab.tools.ImageUtil;
import com.commit451.gitlab.tools.IntentUtil;
import com.commit451.gitlab.tools.Prefs;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.parceler.Parcels;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

/**
 * User activity, which shows the user!
 * Created by Jawn on 9/21/2015.
 */
public class UserActivity extends BaseActivity {

    private static final String KEY_USER = "user";

    public static Intent newInstance(Context context, User user) {
        Intent intent = new Intent(context, UserActivity.class);
        intent.putExtra(KEY_USER, Parcels.wrap(user));
        return intent;
    }

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.collapsing_toolbar) CollapsingToolbarLayout mCollapsingToolbarLayout;
    @Bind(R.id.backdrop) ImageView mBackdrop;
    @Bind(R.id.list) RecyclerView mActivityRecyclerView;
    FeedAdapter mFeedAdapter;
    @Bind(R.id.progress) View mProgress;
    @Bind(R.id.message) TextView mMessageView;

    User mUser;

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

    private final Callback<UserFeed> mUserFeedCallback = new Callback<UserFeed>() {
        @Override
        public void onResponse(Response<UserFeed> response, Retrofit retrofit) {
            mProgress.setVisibility(View.GONE);
            if (!response.isSuccess()) {
                Timber.e("Feed response was not a success: %d", response.code());
                return;
            }
            if (response.body().getEntries() == null || response.body().getEntries().isEmpty()) {
                mMessageView.setVisibility(View.VISIBLE);
                mMessageView.setText(R.string.no_activity);
            } else {
                mFeedAdapter.setEntries(response.body().getEntries());
            }
        }

        @Override
        public void onFailure(Throwable t) {
            mProgress.setVisibility(View.GONE);
            mMessageView.setVisibility(View.VISIBLE);
            mMessageView.setText(R.string.connection_error);
            Timber.e(t.toString());
        }
    };

    private final FeedAdapter.Listener mFeedAdapterListener = new FeedAdapter.Listener() {
        @Override
        public void onFeedEntryClicked(Entry entry) {
            IntentUtil.openPage(getWindow().getDecorView(), entry.getLink().getHref());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        ButterKnife.bind(this);
        mUser = Parcels.unwrap(getIntent().getParcelableExtra(KEY_USER));
        mToolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mToolbar.setTitle(mUser.getUsername());
        String url = ImageUtil.getGravatarUrl(mUser, getResources().getDimensionPixelSize(R.dimen.user_header_image_size));
        Picasso.with(this)
                .load(url)
                .into(mImageLoadTarget);
        mActivityRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mFeedAdapter = new FeedAdapter(mFeedAdapterListener);
        mActivityRecyclerView.setAdapter(mFeedAdapter);
        load();
    }

    private void bindPalette(Palette palette) {
        int vibrantColor = palette.getVibrantColor(ThemeUtils.getThemeAttrColor(this, R.attr.colorPrimary));

        ColorUtil.animateStatusBarAndNavBarColors(getWindow(), ColorUtil.getDarkerColor(vibrantColor));
        //TODO animate this too
        mCollapsingToolbarLayout.setContentScrimColor(vibrantColor);
        mToolbar.setTitleTextColor(palette.getDarkMutedColor(Color.BLACK));
    }

    private void load() {
        mMessageView.setVisibility(View.GONE);
        mProgress.setVisibility(View.VISIBLE);
        GitLabClient.rssInstance().getUserFeed(mUser.getFeedUrl(Prefs.getServerUrl(this))).enqueue(mUserFeedCallback);
    }
}

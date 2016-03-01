package com.commit451.gitlab.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.GroupAdapter;
import com.commit451.gitlab.api.EasyCallback;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.api.Group;
import com.commit451.gitlab.util.NavigationManager;
import com.commit451.gitlab.util.PaginationUtil;
import com.commit451.gitlab.viewHolder.GroupViewHolder;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import timber.log.Timber;

/**
 * Displays the groups of the current user
 * Created by Jawn on 10/4/2015.
 */
public class GroupsActivity extends BaseActivity implements ATEActivityThemeCustomizer {

    @Override
    public int getActivityTheme() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", true) ?
                R.style.Activity_Groups : R.style.ActivityLight_Groups;
    }

    public static Intent newInstance(Context context) {
        Intent intent = new Intent(context, GroupsActivity.class);
        return intent;
    }

    @Bind(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.swipe_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.list) RecyclerView mGroupRecyclerView;
    @Bind(R.id.message_text) TextView mMessageText;
    GroupAdapter mGroupAdapter;
    LinearLayoutManager mGroupLayoutManager;

    private Uri mNextPageUrl;
    private boolean mLoading = false;

    private final RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = mGroupLayoutManager.getChildCount();
            int totalItemCount = mGroupLayoutManager.getItemCount();
            int firstVisibleItem = mGroupLayoutManager.findFirstVisibleItemPosition();
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !mLoading && mNextPageUrl != null) {
                loadMore();
            }
        }
    };

    private final Callback<List<Group>> mGroupsCallback = new EasyCallback<List<Group>>() {
        @Override
        public void onResponse(@NonNull List<Group> response) {
            mLoading = false;
            mSwipeRefreshLayout.setRefreshing(false);
            if (response.isEmpty()) {
                mMessageText.setText(R.string.no_groups);
                mMessageText.setVisibility(View.VISIBLE);
                mGroupRecyclerView.setVisibility(View.GONE);
            } else {
                mGroupAdapter.setGroups(response);
                mMessageText.setVisibility(View.GONE);
                mGroupRecyclerView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onAllFailure(Throwable t) {
            Timber.e(t, null);
            mSwipeRefreshLayout.setRefreshing(false);
            mLoading = false;
            mMessageText.setVisibility(View.VISIBLE);
            mMessageText.setText(R.string.connection_error);
        }
    };

    private final Callback<List<Group>> mMoreGroupsCallback = new EasyCallback<List<Group>>() {
        @Override
        public void onResponse(@NonNull List<Group> response) {
            mLoading = false;
            mGroupAdapter.addGroups(response);
            mNextPageUrl = PaginationUtil.parse(getResponse()).getNext();
        }

        @Override
        public void onAllFailure(Throwable t) {
            Timber.e(t, null);
            mLoading = false;
        }
    };

    private final GroupAdapter.Listener mGroupAdapterListener = new GroupAdapter.Listener() {
        @Override
        public void onGroupClicked(Group group, GroupViewHolder groupViewHolder) {
            NavigationManager.navigateToGroup(GroupsActivity.this, groupViewHolder.mImageView, group);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);
        ButterKnife.bind(this);
        mToolbar.setTitle(R.string.nav_groups);
        mToolbar.setNavigationIcon(R.drawable.ic_menu_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                load();
            }
        });
        mMessageText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                load();
            }
        });
        mGroupLayoutManager = new GridLayoutManager(this, 2);
        mGroupRecyclerView.setLayoutManager(mGroupLayoutManager);
        mGroupAdapter = new GroupAdapter(mGroupAdapterListener);
        mGroupRecyclerView.setAdapter(mGroupAdapter);
        mGroupRecyclerView.addOnScrollListener(mOnScrollListener);
        load();
    }

    private void load() {
        mMessageText.setVisibility(View.GONE);
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            }
        });

        mNextPageUrl = null;
        mLoading = true;

        GitLabClient.instance().getGroups().enqueue(mGroupsCallback);
    }

    private void loadMore() {
        if (mNextPageUrl == null) {
            return;
        }

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            }
        });

        mLoading = true;

        Timber.d("loadMore called for " + mNextPageUrl);
        GitLabClient.instance().getGroups(mNextPageUrl.toString()).enqueue(mMoreGroupsCallback);
    }
}

package com.commit451.gitlab.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.GroupAdapter;
import com.commit451.gitlab.event.CloseDrawerEvent;
import com.commit451.gitlab.event.ReloadDataEvent;
import com.commit451.gitlab.model.api.Group;
import com.commit451.gitlab.navigation.Navigator;
import com.commit451.gitlab.util.DynamicGridLayoutManager;
import com.commit451.gitlab.util.LinkHeaderParser;
import com.commit451.gitlab.viewHolder.GroupViewHolder;
import com.commit451.reptar.retrofit.ResponseSingleObserver;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Displays the groups of the current user
 */
public class GroupsActivity extends BaseActivity {

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, GroupsActivity.class);
        return intent;
    }

    @BindView(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.swipe_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.list) RecyclerView mGroupRecyclerView;
    @BindView(R.id.message_text) TextView mMessageText;
    GroupAdapter mGroupAdapter;
    DynamicGridLayoutManager mGroupLayoutManager;

    private Uri mNextPageUrl;
    private boolean mLoading = false;
    EventReceiver mEventReceiver;

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

    private final GroupAdapter.Listener mGroupAdapterListener = new GroupAdapter.Listener() {
        @Override
        public void onGroupClicked(Group group, GroupViewHolder groupViewHolder) {
            Navigator.navigateToGroup(GroupsActivity.this, groupViewHolder.mImageView, group);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);
        ButterKnife.bind(this);
        mEventReceiver = new EventReceiver();
        App.bus().register(mEventReceiver);

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
        mGroupLayoutManager = new DynamicGridLayoutManager(this);
        mGroupLayoutManager.setMinimumWidthDimension(R.dimen.user_list_image_size);
        mGroupRecyclerView.setLayoutManager(mGroupLayoutManager);
        mGroupAdapter = new GroupAdapter(mGroupAdapterListener);
        mGroupRecyclerView.setAdapter(mGroupAdapter);
        mGroupRecyclerView.addOnScrollListener(mOnScrollListener);
        load();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        App.bus().unregister(mEventReceiver);
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

        App.get().getGitLab().getGroups()
                .compose(this.<Response<List<Group>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ResponseSingleObserver<List<Group>>() {

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                        mSwipeRefreshLayout.setRefreshing(false);
                        mLoading = false;
                        mMessageText.setVisibility(View.VISIBLE);
                        mMessageText.setText(R.string.connection_error);
                    }

                    @Override
                    protected void onResponseSuccess(List<Group> groups) {
                        mLoading = false;
                        mSwipeRefreshLayout.setRefreshing(false);
                        if (groups.isEmpty()) {
                            mMessageText.setText(R.string.no_groups);
                            mMessageText.setVisibility(View.VISIBLE);
                            mGroupRecyclerView.setVisibility(View.GONE);
                        } else {
                            mGroupAdapter.setGroups(groups);
                            mMessageText.setVisibility(View.GONE);
                            mGroupRecyclerView.setVisibility(View.VISIBLE);
                            mNextPageUrl = LinkHeaderParser.parse(response()).getNext();
                        }
                    }
                });
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

        Timber.d("loadMore called for %s", mNextPageUrl);
        App.get().getGitLab().getGroups(mNextPageUrl.toString())
                .compose(this.<Response<List<Group>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ResponseSingleObserver<List<Group>>() {

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                        mLoading = false;
                    }

                    @Override
                    protected void onResponseSuccess(List<Group> groups) {
                        mLoading = false;
                        mGroupAdapter.addGroups(groups);
                        mNextPageUrl = LinkHeaderParser.parse(response()).getNext();
                    }
                });
    }

    private class EventReceiver {

        @Subscribe
        public void onCloseDrawerEvent(CloseDrawerEvent event) {
            mDrawerLayout.closeDrawers();
        }

        @Subscribe
        public void onReloadData(ReloadDataEvent event) {
            load();
        }
    }
}

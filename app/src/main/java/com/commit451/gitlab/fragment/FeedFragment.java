package com.commit451.gitlab.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.gitlab.LabCoatApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.DividerItemDecoration;
import com.commit451.gitlab.adapter.FeedAdapter;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.rss.Entry;
import com.commit451.gitlab.model.rss.Feed;
import com.commit451.gitlab.util.NavigationManager;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class FeedFragment extends BaseFragment {

    private static final String EXTRA_FEED_URL = "extra_feed_url";

    public static FeedFragment newInstance(Uri feedUrl) {
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_FEED_URL, feedUrl);

        FeedFragment fragment = new FeedFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Bind(R.id.swipe_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.list) RecyclerView mEntryListView;
    @Bind(R.id.message_text) TextView mMessageView;

    private Uri mFeedUrl;
    private EventReceiver mEventReceiver;
    private FeedAdapter mFeedAdapter;

    private final Callback<Feed> mUserFeedCallback = new Callback<Feed>() {
        @Override
        public void onResponse(Response<Feed> response, Retrofit retrofit) {
            if (getView() == null) {
                return;
            }

            mSwipeRefreshLayout.setRefreshing(false);

            if (!response.isSuccess()) {
                Timber.e("Feed response was not a success: %d", response.code());
                mMessageView.setVisibility(View.VISIBLE);
                mMessageView.setText(R.string.connection_error_feed);
                mFeedAdapter.setEntries(null);
                return;
            }

            if (response.body().getEntries() != null && !response.body().getEntries().isEmpty()) {
                mMessageView.setVisibility(View.GONE);
            } else {
                Timber.d("No activity in the feed");
                mMessageView.setVisibility(View.VISIBLE);
                mMessageView.setText(R.string.no_activity);
            }

            mFeedAdapter.setEntries(response.body().getEntries());
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t, null);

            if (getView() == null) {
                return;
            }

            mSwipeRefreshLayout.setRefreshing(false);

            mMessageView.setVisibility(View.VISIBLE);
            mMessageView.setText(R.string.connection_error);
            mFeedAdapter.setEntries(null);
        }
    };

    private final FeedAdapter.Listener mFeedAdapterListener = new FeedAdapter.Listener() {
        @Override
        public void onFeedEntryClicked(Entry entry) {
            NavigationManager.navigateToUrl(getActivity(), entry.getLink().getHref(), GitLabClient.getAccount());
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFeedUrl = getArguments().getParcelable(EXTRA_FEED_URL);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        mEventReceiver = new EventReceiver();
        LabCoatApp.bus().register(mEventReceiver);

        mFeedAdapter = new FeedAdapter(mFeedAdapterListener);
        mEntryListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mEntryListView.addItemDecoration(new DividerItemDecoration(getActivity()));
        mEntryListView.setAdapter(mFeedAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });

        loadData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        LabCoatApp.bus().unregister(mEventReceiver);
    }

    @Override
    protected void loadData() {
        if (getView() == null) {
            return;
        }

        if (mFeedUrl == null) {
            mSwipeRefreshLayout.setRefreshing(false);
            mMessageView.setVisibility(View.VISIBLE);
            return;
        }
        mMessageView.setVisibility(View.GONE);

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            }
        });

        GitLabClient.rssInstance().getFeed(mFeedUrl.toString()).enqueue(mUserFeedCallback);
    }

    private class EventReceiver {
    }
}

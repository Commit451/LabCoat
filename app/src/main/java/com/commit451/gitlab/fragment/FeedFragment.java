package com.commit451.gitlab.fragment;

import com.commit451.gitlab.GitLabApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.FeedAdapter;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.rss.Entry;
import com.commit451.gitlab.model.rss.Feed;
import com.commit451.gitlab.util.IntentUtil;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class FeedFragment extends BaseFragment {

    private static final String EXTRA_FEED_URL = "extra_feed_url";

    public static FeedFragment newInstance(String feedUrl) {
        Bundle args = new Bundle();
        args.putString(EXTRA_FEED_URL, feedUrl);
        FeedFragment fragment = new FeedFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Bind(R.id.error_text) TextView mErrorText;
    @Bind(R.id.swipe_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.list) RecyclerView mEntryList;

    String mFeedUrl;
    EventReceiver mEventReceiver;
    FeedAdapter mFeedAdapter;

    private final Callback<Feed> mUserFeedCallback = new Callback<Feed>() {
        @Override
        public void onResponse(Response<Feed> response, Retrofit retrofit) {
            if (mSwipeRefreshLayout == null) {
                return;
            }

            mSwipeRefreshLayout.setRefreshing(false);
            if (!response.isSuccess()) {
                Timber.e("Feed response was not a success: %d", response.code());
                return;
            }
            if (response.body().getEntries() == null || response.body().getEntries().isEmpty()) {
                mErrorText.setVisibility(View.VISIBLE);
                mErrorText.setText(R.string.no_activity);
            } else {
                mFeedAdapter.setEntries(response.body().getEntries());
            }
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t, null);

            if (mSwipeRefreshLayout == null) {
                return;
            }

            mSwipeRefreshLayout.setRefreshing(false);
            mErrorText.setVisibility(View.VISIBLE);
            mErrorText.setText(R.string.connection_error);
        }
    };

    private final FeedAdapter.Listener mFeedAdapterListener = new FeedAdapter.Listener() {
        @Override
        public void onFeedEntryClicked(Entry entry) {
            IntentUtil.openPage(getActivity().getWindow().getDecorView(), entry.getLink().getHref());
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFeedUrl = getArguments().getString(EXTRA_FEED_URL);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        mEntryList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mFeedAdapter = new FeedAdapter(mFeedAdapterListener);
        mEntryList.setAdapter(mFeedAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });

        mEventReceiver = new EventReceiver();
        GitLabApp.bus().register(mEventReceiver);

        loadData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        GitLabApp.bus().unregister(mEventReceiver);
        ButterKnife.unbind(this);
    }

    @Override
    protected void loadData() {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            }
        });

        GitLabClient.rssInstance().getFeed(mFeedUrl).enqueue(mUserFeedCallback);
    }

    private class EventReceiver {

    }
}

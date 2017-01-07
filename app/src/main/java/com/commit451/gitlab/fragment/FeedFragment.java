package com.commit451.gitlab.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.DividerItemDecoration;
import com.commit451.gitlab.adapter.FeedAdapter;
import com.commit451.gitlab.model.rss.Entry;
import com.commit451.gitlab.model.rss.Feed;
import com.commit451.gitlab.navigation.Navigator;
import com.commit451.gitlab.rx.CustomSingleObserver;
import com.novoda.simplechromecustomtabs.SimpleChromeCustomTabs;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Takes an RSS feed url and shows the feed
 */
public class FeedFragment extends ButterKnifeFragment {

    private static final String EXTRA_FEED_URL = "extra_feed_url";

    public static FeedFragment newInstance(Uri feedUrl) {
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_FEED_URL, feedUrl);

        FeedFragment fragment = new FeedFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.list)
    RecyclerView mEntryListView;
    @BindView(R.id.message_text)
    TextView mMessageView;

    private Uri mFeedUrl;
    private FeedAdapter mFeedAdapter;

    private final FeedAdapter.Listener mFeedAdapterListener = new FeedAdapter.Listener() {
        @Override
        public void onFeedEntryClicked(Entry entry) {
            Navigator.navigateToUrl(getActivity(), entry.getLink().getHref(), App.get().getAccount());
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
    public void onResume() {
        super.onResume();
        SimpleChromeCustomTabs.getInstance().connectTo(getActivity());
    }

    @Override
    public void onPause() {
        if (SimpleChromeCustomTabs.getInstance().isConnected()) {
            SimpleChromeCustomTabs.getInstance().disconnectFrom(getActivity());
        }
        super.onPause();
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
        App.get().getGitLabRss().getFeed(mFeedUrl.toString())
                .compose(this.<Feed>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomSingleObserver<Feed>() {
                    @Override
                    public void success(@NonNull Feed feed) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        if (feed.getEntries() != null && !feed.getEntries().isEmpty()) {
                            mMessageView.setVisibility(View.GONE);
                        } else {
                            Timber.d("No activity in the feed");
                            mMessageView.setVisibility(View.VISIBLE);
                            mMessageView.setText(R.string.no_activity);
                        }
                        mFeedAdapter.setEntries(feed.getEntries());
                    }

                    @Override
                    public void error(@NonNull Throwable e) {
                        Timber.e(e);
                        mSwipeRefreshLayout.setRefreshing(false);
                        mMessageView.setVisibility(View.VISIBLE);
                        mMessageView.setText(R.string.connection_error_feed);
                        mFeedAdapter.setEntries(null);
                    }
                });
    }
}

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
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.list)
    RecyclerView listEntries;
    @BindView(R.id.message_text)
    TextView textMessage;

    private Uri feedUrl;
    private FeedAdapter adapterFeed;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        feedUrl = getArguments().getParcelable(EXTRA_FEED_URL);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapterFeed = new FeedAdapter(new FeedAdapter.Listener() {
            @Override
            public void onFeedEntryClicked(Entry entry) {
                Navigator.navigateToUrl(getActivity(), entry.getLink().getHref(), App.get().getAccount());
            }
        });
        listEntries.setLayoutManager(new LinearLayoutManager(getActivity()));
        listEntries.addItemDecoration(new DividerItemDecoration(getActivity()));
        listEntries.setAdapter(adapterFeed);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
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
        if (feedUrl == null) {
            swipeRefreshLayout.setRefreshing(false);
            textMessage.setVisibility(View.VISIBLE);
            return;
        }
        textMessage.setVisibility(View.GONE);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(true);
                }
            }
        });
        App.get().getGitLabRss().getFeed(feedUrl.toString())
                .compose(this.<Feed>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomSingleObserver<Feed>() {
                    @Override
                    public void success(@NonNull Feed feed) {
                        swipeRefreshLayout.setRefreshing(false);
                        if (feed.getEntries() != null && !feed.getEntries().isEmpty()) {
                            textMessage.setVisibility(View.GONE);
                        } else {
                            Timber.d("No activity in the feed");
                            textMessage.setVisibility(View.VISIBLE);
                            textMessage.setText(R.string.no_activity);
                        }
                        adapterFeed.setEntries(feed.getEntries());
                    }

                    @Override
                    public void error(@NonNull Throwable e) {
                        Timber.e(e);
                        swipeRefreshLayout.setRefreshing(false);
                        textMessage.setVisibility(View.VISIBLE);
                        textMessage.setText(R.string.connection_error_feed);
                        adapterFeed.setEntries(null);
                    }
                });
    }
}

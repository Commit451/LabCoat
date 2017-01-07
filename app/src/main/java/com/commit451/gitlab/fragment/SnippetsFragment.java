package com.commit451.gitlab.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.activity.ProjectActivity;
import com.commit451.gitlab.adapter.DividerItemDecoration;
import com.commit451.gitlab.adapter.SnippetAdapter;
import com.commit451.gitlab.event.ProjectReloadEvent;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.model.api.Snippet;
import com.commit451.gitlab.navigation.Navigator;
import com.commit451.gitlab.rx.CustomResponseSingleObserver;
import com.commit451.gitlab.util.LinkHeaderParser;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;
import timber.log.Timber;

public class SnippetsFragment extends ButterKnifeFragment {

    public static SnippetsFragment newInstance() {
        return new SnippetsFragment();
    }

    @BindView(R.id.root)
    ViewGroup mRoot;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.list)
    RecyclerView mRecyclerView;
    @BindView(R.id.message_text)
    TextView mMessageView;
    @BindView(R.id.state_spinner)
    Spinner mSpinner;

    private Project mProject;
    private EventReceiver mEventReceiver;
    private SnippetAdapter mSnippetAdapter;
    private LinearLayoutManager mMilestoneLayoutManager;

    private String mState;
    private String[] mStates;
    private boolean mLoading = false;
    private Uri mNextPageUrl;

    @OnClick(R.id.add)
    public void onAddClicked(View fab) {
        if (mProject != null) {
            Navigator.navigateToAddMilestone(getActivity(), fab, mProject);
        } else {
            Snackbar.make(mRoot, getString(R.string.wait_for_project_to_load), Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

    private final AdapterView.OnItemSelectedListener mSpinnerItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mState = mStates[position];
            loadData();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    private final SnippetAdapter.Listener mMilestoneListener = new SnippetAdapter.Listener() {

        @Override
        public void onSnippetClicked(Snippet snippet) {

        }
    };

    private final RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = mMilestoneLayoutManager.getChildCount();
            int totalItemCount = mMilestoneLayoutManager.getItemCount();
            int firstVisibleItem = mMilestoneLayoutManager.findFirstVisibleItemPosition();
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !mLoading && mNextPageUrl != null) {
                loadMore();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mState = getResources().getString(R.string.milestone_state_value_default);
        mStates = getResources().getStringArray(R.array.milestone_state_values);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_snippets, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEventReceiver = new EventReceiver();
        App.bus().register(mEventReceiver);

        mSnippetAdapter = new SnippetAdapter(mMilestoneListener);
        mMilestoneLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mMilestoneLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity()));
        mRecyclerView.setAdapter(mSnippetAdapter);
        mRecyclerView.addOnScrollListener(mOnScrollListener);

        mSpinner.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, getResources().getStringArray(R.array.milestone_state_names)));
        mSpinner.setOnItemSelectedListener(mSpinnerItemSelectedListener);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });

        if (getActivity() instanceof ProjectActivity) {
            mProject = ((ProjectActivity) getActivity()).getProject();
            loadData();
        } else {
            throw new IllegalStateException("Incorrect parent activity");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        App.bus().unregister(mEventReceiver);
    }

    @Override
    protected void loadData() {
        if (getView() == null) {
            return;
        }
        if (mProject == null) {
            mSwipeRefreshLayout.setRefreshing(false);
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
        mNextPageUrl = null;
        mLoading = true;
        App.get().getGitLab().getSnippets(mProject.getId())
                .compose(this.<Response<List<Snippet>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomResponseSingleObserver<List<Snippet>>() {

                    @Override
                    public void error(Throwable e) {
                        mLoading = false;
                        Timber.e(e);
                        mSwipeRefreshLayout.setRefreshing(false);
                        mMessageView.setVisibility(View.VISIBLE);
                        mMessageView.setText(R.string.connection_error_milestones);
                        mSnippetAdapter.setData(null);
                        mNextPageUrl = null;
                    }

                    @Override
                    public void responseSuccess(List<Snippet> snippets) {
                        mLoading = false;
                        mSwipeRefreshLayout.setRefreshing(false);
                        if (snippets.isEmpty()) {
                            mMessageView.setVisibility(View.VISIBLE);
                            mMessageView.setText(R.string.no_milestones);
                        }
                        mSnippetAdapter.setData(snippets);
                        mNextPageUrl = LinkHeaderParser.parse(response()).getNext();
                        Timber.d("Next page url %s", mNextPageUrl);
                    }
                });
    }

    private void loadMore() {
        if (getView() == null) {
            return;
        }

        if (mNextPageUrl == null) {
            return;
        }

        mLoading = true;
        mSnippetAdapter.setLoading(true);

        Timber.d("loadMore called for %s", mNextPageUrl);
        App.get().getGitLab().getSnippets(mNextPageUrl.toString())
                .compose(this.<Response<List<Snippet>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomResponseSingleObserver<List<Snippet>>() {

                    @Override
                    public void error(Throwable e) {
                        Timber.e(e);
                        mSnippetAdapter.setLoading(false);
                        mLoading = false;
                    }

                    @Override
                    public void responseSuccess(List<Snippet> snippets) {
                        mLoading = false;
                        mSnippetAdapter.setLoading(false);
                        mNextPageUrl = LinkHeaderParser.parse(response()).getNext();
                        mSnippetAdapter.addData(snippets);
                    }
                });
    }

    private class EventReceiver {
        @Subscribe
        public void onProjectReload(ProjectReloadEvent event) {
            mProject = event.mProject;
            loadData();
        }
    }
}

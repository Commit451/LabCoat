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
import com.commit451.gitlab.adapter.BuildsAdapter;
import com.commit451.gitlab.adapter.DividerItemDecoration;
import com.commit451.gitlab.event.BuildChangedEvent;
import com.commit451.gitlab.event.ProjectReloadEvent;
import com.commit451.gitlab.model.api.Build;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.navigation.Navigator;
import com.commit451.gitlab.rx.CustomResponseSingleObserver;
import com.commit451.gitlab.util.LinkHeaderParser;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Shows the builds of a project
 */
public class BuildsFragment extends ButterKnifeFragment {

    public static BuildsFragment newInstance() {
        return new BuildsFragment();
    }

    @BindView(R.id.root)
    ViewGroup mRoot;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.list)
    RecyclerView mListBuilds;
    @BindView(R.id.message_text)
    TextView mMessageView;
    @BindView(R.id.issue_spinner)
    Spinner mSpinner;

    private Project mProject;
    private BuildsAdapter mBuildsAdapter;
    private LinearLayoutManager mLayoutManagerBuilds;
    private EventReceiver mEventReceiver;

    String mScope;
    private String[] mScopes;
    private Uri mNextPageUrl;
    private boolean mLoading = false;

    private final RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = mLayoutManagerBuilds.getChildCount();
            int totalItemCount = mLayoutManagerBuilds.getItemCount();
            int firstVisibleItem = mLayoutManagerBuilds.findFirstVisibleItemPosition();
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !mLoading && mNextPageUrl != null) {
                loadMore();
            }
        }
    };

    private final BuildsAdapter.Listener mAdapterListener = new BuildsAdapter.Listener() {
        @Override
        public void onBuildClicked(Build build) {
            if (mProject != null) {
                Navigator.navigateToBuild(getActivity(), mProject, build);
            } else {
                Snackbar.make(mRoot, getString(R.string.wait_for_project_to_load), Snackbar.LENGTH_SHORT)
                        .show();
            }
        }
    };

    private final AdapterView.OnItemSelectedListener mSpinnerItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mScope = mScopes[position];
            loadData();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScopes = getResources().getStringArray(R.array.build_scope_values);
        mScope = mScopes[0];
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_builds, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEventReceiver = new EventReceiver();
        App.bus().register(mEventReceiver);

        mBuildsAdapter = new BuildsAdapter(mAdapterListener);
        mLayoutManagerBuilds = new LinearLayoutManager(getActivity());
        mListBuilds.setLayoutManager(mLayoutManagerBuilds);
        mListBuilds.addItemDecoration(new DividerItemDecoration(getActivity()));
        mListBuilds.setAdapter(mBuildsAdapter);
        mListBuilds.addOnScrollListener(mOnScrollListener);

        mSpinner.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1,
                android.R.id.text1, getResources().getStringArray(R.array.build_scope_names)));
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
        App.get().getGitLab().getBuilds(mProject.getId(), mScope)
                .compose(this.<Response<List<Build>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomResponseSingleObserver<List<Build>>() {

                    @Override
                    public void error(Throwable e) {
                        mLoading = false;
                        Timber.e(e);
                        mSwipeRefreshLayout.setRefreshing(false);
                        mMessageView.setVisibility(View.VISIBLE);
                        mMessageView.setText(R.string.failed_to_load_builds);
                        mBuildsAdapter.setValues(null);
                        mNextPageUrl = null;
                    }

                    @Override
                    public void responseSuccess(List<Build> builds) {
                        mLoading = false;

                        mSwipeRefreshLayout.setRefreshing(false);
                        if (builds.isEmpty()) {
                            mMessageView.setVisibility(View.VISIBLE);
                            mMessageView.setText(R.string.no_builds);
                        }
                        mBuildsAdapter.setValues(builds);
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

        mBuildsAdapter.setLoading(true);
        mLoading = true;

        Timber.d("loadMore called for %s", mNextPageUrl);
        App.get().getGitLab().getBuilds(mNextPageUrl.toString(), mScope)
                .compose(this.<Response<List<Build>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomResponseSingleObserver<List<Build>>() {

                    @Override
                    public void error(Throwable e) {
                        Timber.e(e);
                        mLoading = false;
                        mBuildsAdapter.setLoading(false);
                    }

                    @Override
                    public void responseSuccess(List<Build> builds) {
                        mLoading = false;
                        mBuildsAdapter.setLoading(false);
                        mNextPageUrl = LinkHeaderParser.parse(response()).getNext();
                        mBuildsAdapter.addValues(builds);
                    }
                });
    }

    private class EventReceiver {
        @Subscribe
        public void onProjectReload(ProjectReloadEvent event) {
            mProject = event.mProject;
            loadData();
        }

        @Subscribe
        public void onBuildChangedEvent(BuildChangedEvent event) {
            mBuildsAdapter.updateBuild(event.build);
        }
    }
}
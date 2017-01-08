package com.commit451.gitlab.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.commit451.gitlab.adapter.BuildAdapter;
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
    ViewGroup root;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.list)
    RecyclerView listBuilds;
    @BindView(R.id.message_text)
    TextView textMessage;
    @BindView(R.id.issue_spinner)
    Spinner spinnerIssue;

    BuildAdapter adapterBuilds;
    LinearLayoutManager layoutManagerBuilds;

    Project project;
    String scope;
    String[] scopes;
    Uri nextPageUrl;
    boolean loading;

    private final RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = layoutManagerBuilds.getChildCount();
            int totalItemCount = layoutManagerBuilds.getItemCount();
            int firstVisibleItem = layoutManagerBuilds.findFirstVisibleItemPosition();
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !loading && nextPageUrl != null) {
                loadMore();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scopes = getResources().getStringArray(R.array.build_scope_values);
        scope = scopes[0];
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_builds, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        App.bus().register(this);

        adapterBuilds = new BuildAdapter(new BuildAdapter.Listener() {
            @Override
            public void onBuildClicked(Build build) {
                if (project != null) {
                    Navigator.navigateToBuild(getActivity(), project, build);
                } else {
                    Snackbar.make(root, getString(R.string.wait_for_project_to_load), Snackbar.LENGTH_SHORT)
                            .show();
                }
            }
        });
        layoutManagerBuilds = new LinearLayoutManager(getActivity());
        listBuilds.setLayoutManager(layoutManagerBuilds);
        listBuilds.addItemDecoration(new DividerItemDecoration(getActivity()));
        listBuilds.setAdapter(adapterBuilds);
        listBuilds.addOnScrollListener(onScrollListener);

        spinnerIssue.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1,
                android.R.id.text1, getResources().getStringArray(R.array.build_scope_names)));
        spinnerIssue.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                scope = scopes[position];
                loadData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });

        if (getActivity() instanceof ProjectActivity) {
            project = ((ProjectActivity) getActivity()).getProject();
            loadData();
        } else {
            throw new IllegalStateException("Incorrect parent activity");
        }
    }

    @Override
    public void onDestroyView() {
        App.bus().unregister(this);
        super.onDestroyView();
    }

    @Override
    protected void loadData() {
        if (getView() == null) {
            return;
        }
        if (project == null) {
            swipeRefreshLayout.setRefreshing(false);
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
        nextPageUrl = null;
        loading = true;
        App.get().getGitLab().getBuilds(project.getId(), scope)
                .compose(this.<Response<List<Build>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomResponseSingleObserver<List<Build>>() {

                    @Override
                    public void error(@NonNull Throwable e) {
                        loading = false;
                        Timber.e(e);
                        swipeRefreshLayout.setRefreshing(false);
                        textMessage.setVisibility(View.VISIBLE);
                        textMessage.setText(R.string.failed_to_load_builds);
                        adapterBuilds.setValues(null);
                        nextPageUrl = null;
                    }

                    @Override
                    public void responseSuccess(@NonNull List<Build> builds) {
                        loading = false;

                        swipeRefreshLayout.setRefreshing(false);
                        if (builds.isEmpty()) {
                            textMessage.setVisibility(View.VISIBLE);
                            textMessage.setText(R.string.no_builds);
                        }
                        adapterBuilds.setValues(builds);
                        nextPageUrl = LinkHeaderParser.parse(response()).getNext();
                        Timber.d("Next page url %s", nextPageUrl);
                    }
                });
    }

    private void loadMore() {
        if (getView() == null) {
            return;
        }

        if (nextPageUrl == null) {
            return;
        }

        adapterBuilds.setLoading(true);
        loading = true;

        Timber.d("loadMore called for %s", nextPageUrl);
        App.get().getGitLab().getBuilds(nextPageUrl.toString(), scope)
                .compose(this.<Response<List<Build>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomResponseSingleObserver<List<Build>>() {

                    @Override
                    public void error(@NonNull Throwable e) {
                        Timber.e(e);
                        loading = false;
                        adapterBuilds.setLoading(false);
                    }

                    @Override
                    public void responseSuccess(@NonNull List<Build> builds) {
                        loading = false;
                        adapterBuilds.setLoading(false);
                        nextPageUrl = LinkHeaderParser.parse(response()).getNext();
                        adapterBuilds.addValues(builds);
                    }
                });
    }

    @Subscribe
    public void onProjectReload(ProjectReloadEvent event) {
        project = event.project;
        loadData();
    }

    @Subscribe
    public void onBuildChangedEvent(BuildChangedEvent event) {
        adapterBuilds.updateBuild(event.build);
    }
}
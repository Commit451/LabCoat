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
import com.commit451.gitlab.adapter.DividerItemDecoration;
import com.commit451.gitlab.adapter.IssueAdapter;
import com.commit451.gitlab.event.IssueChangedEvent;
import com.commit451.gitlab.event.IssueCreatedEvent;
import com.commit451.gitlab.event.IssueReloadEvent;
import com.commit451.gitlab.event.ProjectReloadEvent;
import com.commit451.gitlab.model.api.Issue;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.navigation.Navigator;
import com.commit451.gitlab.rx.CustomResponseSingleObserver;
import com.commit451.gitlab.rx.CustomSingleObserver;
import com.commit451.gitlab.util.LinkHeaderParser;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;
import timber.log.Timber;

public class IssuesFragment extends ButterKnifeFragment {

    public static IssuesFragment newInstance() {
        return new IssuesFragment();
    }

    @BindView(R.id.root)
    ViewGroup root;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.list)
    RecyclerView listIssues;
    @BindView(R.id.message_text)
    TextView textMessage;
    @BindView(R.id.issue_spinner)
    Spinner spinnerIssue;

    IssueAdapter adapterIssue;
    LinearLayoutManager mIssuesLayoutManager;

    Project project;
    String state;
    String[] states;
    Uri nextPageUrl;
    boolean loading = false;

    private final RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = mIssuesLayoutManager.getChildCount();
            int totalItemCount = mIssuesLayoutManager.getItemCount();
            int firstVisibleItem = mIssuesLayoutManager.findFirstVisibleItemPosition();
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !loading && nextPageUrl != null) {
                loadMore();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        state = getResources().getString(R.string.issue_state_value_default);
        states = getResources().getStringArray(R.array.issue_state_values);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_issues, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        App.bus().register(this);

        adapterIssue = new IssueAdapter(new IssueAdapter.Listener() {
            @Override
            public void onIssueClicked(Issue issue) {
                if (project != null) {
                    Navigator.navigateToIssue(getActivity(), project, issue);
                } else {
                    Snackbar.make(root, getString(R.string.wait_for_project_to_load), Snackbar.LENGTH_SHORT)
                            .show();
                }
            }
        });
        mIssuesLayoutManager = new LinearLayoutManager(getActivity());
        listIssues.setLayoutManager(mIssuesLayoutManager);
        listIssues.addItemDecoration(new DividerItemDecoration(getActivity()));
        listIssues.setAdapter(adapterIssue);
        listIssues.addOnScrollListener(onScrollListener);

        spinnerIssue.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, getResources().getStringArray(R.array.issue_state_names)));
        spinnerIssue.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                state = states[position];
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

    @OnClick(R.id.add_issue_button)
    public void onAddIssueClick(View fab) {
        if (project != null) {
            Navigator.navigateToAddIssue(getActivity(), fab, project);
        } else {
            Snackbar.make(root, getString(R.string.wait_for_project_to_load), Snackbar.LENGTH_SHORT)
                    .show();
        }
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
        App.get().getGitLab().getIssues(project.getId(), state)
                .compose(this.<Response<List<Issue>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomResponseSingleObserver<List<Issue>>() {

                    @Override
                    public void error(@NonNull Throwable e) {
                        loading = false;
                        Timber.e(e);
                        swipeRefreshLayout.setRefreshing(false);
                        textMessage.setVisibility(View.VISIBLE);
                        textMessage.setText(R.string.connection_error_issues);
                        adapterIssue.setIssues(null);
                        nextPageUrl = null;
                    }

                    @Override
                    public void responseSuccess(@NonNull List<Issue> issues) {
                        loading = false;
                        swipeRefreshLayout.setRefreshing(false);
                        if (issues.isEmpty()) {
                            textMessage.setVisibility(View.VISIBLE);
                            textMessage.setText(R.string.no_issues);
                        }
                        adapterIssue.setIssues(issues);
                        nextPageUrl = LinkHeaderParser.parse(response()).getNext();
                        Timber.d("Next page url " + nextPageUrl);
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

        adapterIssue.setLoading(true);
        loading = true;

        Timber.d("loadMore called for " + nextPageUrl);
        App.get().getGitLab().getIssues(nextPageUrl.toString())
                .compose(this.<Response<List<Issue>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomSingleObserver<Response<List<Issue>>>() {

                    @Override
                    public void error(@NonNull Throwable e) {
                        Timber.e(e);
                        loading = false;
                        adapterIssue.setLoading(false);
                    }

                    @Override
                    public void success(@NonNull Response<List<Issue>> listResponse) {
                        loading = false;
                        adapterIssue.setLoading(false);
                        nextPageUrl = LinkHeaderParser.parse(listResponse).getNext();
                        adapterIssue.addIssues(listResponse.body());
                    }
                });
    }

    @Subscribe
    public void onProjectReload(ProjectReloadEvent event) {
        project = event.project;
        loadData();
    }

    @Subscribe
    public void onIssueCreated(IssueCreatedEvent event) {
        adapterIssue.addIssue(event.issue);
        if (getView() != null) {
            textMessage.setVisibility(View.GONE);
            listIssues.smoothScrollToPosition(0);
        }
    }

    @Subscribe
    public void onIssueChanged(IssueChangedEvent event) {
        adapterIssue.updateIssue(event.issue);
    }

    @Subscribe
    public void onIssueReload(IssueReloadEvent event) {
        loadData();
    }
}
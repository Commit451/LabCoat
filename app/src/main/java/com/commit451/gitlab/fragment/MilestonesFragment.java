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
import com.commit451.gitlab.adapter.MilestoneAdapter;
import com.commit451.gitlab.event.MilestoneChangedEvent;
import com.commit451.gitlab.event.MilestoneCreatedEvent;
import com.commit451.gitlab.event.ProjectReloadEvent;
import com.commit451.gitlab.model.api.Milestone;
import com.commit451.gitlab.model.api.Project;
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

public class MilestonesFragment extends ButterKnifeFragment {

    public static MilestonesFragment newInstance() {
        return new MilestonesFragment();
    }

    @BindView(R.id.root)
    ViewGroup root;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.list)
    RecyclerView listMilestones;
    @BindView(R.id.message_text)
    TextView textMessage;
    @BindView(R.id.state_spinner)
    Spinner spinnerStates;

    MilestoneAdapter adapterMilestones;
    LinearLayoutManager layoutManagerMilestones;

    Project project;
    String state;
    String[] states;
    boolean loading = false;
    Uri nextPageUrl;

    @OnClick(R.id.add)
    public void onAddClicked(View fab) {
        if (project != null) {
            Navigator.navigateToAddMilestone(getActivity(), fab, project);
        } else {
            Snackbar.make(root, getString(R.string.wait_for_project_to_load), Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

    private final RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = layoutManagerMilestones.getChildCount();
            int totalItemCount = layoutManagerMilestones.getItemCount();
            int firstVisibleItem = layoutManagerMilestones.findFirstVisibleItemPosition();
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !loading && nextPageUrl != null) {
                loadMore();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        state = getResources().getString(R.string.milestone_state_value_default);
        states = getResources().getStringArray(R.array.milestone_state_values);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_milestones, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        App.bus().register(this);

        adapterMilestones = new MilestoneAdapter(new MilestoneAdapter.Listener() {
            @Override
            public void onMilestoneClicked(Milestone milestone) {
                Navigator.navigateToMilestone(getActivity(), project, milestone);
            }
        });
        layoutManagerMilestones = new LinearLayoutManager(getActivity());
        listMilestones.setLayoutManager(layoutManagerMilestones);
        listMilestones.addItemDecoration(new DividerItemDecoration(getActivity()));
        listMilestones.setAdapter(adapterMilestones);
        listMilestones.addOnScrollListener(mOnScrollListener);

        spinnerStates.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, getResources().getStringArray(R.array.milestone_state_names)));
        spinnerStates.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
        App.get().getGitLab().getMilestones(project.getId(), state)
                .compose(this.<Response<List<Milestone>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomResponseSingleObserver<List<Milestone>>() {

                    @Override
                    public void error(@NonNull Throwable e) {
                        loading = false;
                        Timber.e(e);
                        swipeRefreshLayout.setRefreshing(false);
                        textMessage.setVisibility(View.VISIBLE);
                        textMessage.setText(R.string.connection_error_milestones);
                        adapterMilestones.setData(null);
                        nextPageUrl = null;
                    }

                    @Override
                    public void responseSuccess(@NonNull List<Milestone> milestones) {
                        loading = false;
                        swipeRefreshLayout.setRefreshing(false);
                        if (milestones.isEmpty()) {
                            textMessage.setVisibility(View.VISIBLE);
                            textMessage.setText(R.string.no_milestones);
                        }
                        adapterMilestones.setData(milestones);
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

        loading = true;
        adapterMilestones.setLoading(true);

        Timber.d("loadMore called for " + nextPageUrl);
        App.get().getGitLab().getMilestones(nextPageUrl.toString())
                .compose(this.<Response<List<Milestone>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomResponseSingleObserver<List<Milestone>>() {

                    @Override
                    public void error(@NonNull Throwable e) {
                        Timber.e(e);
                        adapterMilestones.setLoading(false);
                        loading = false;
                    }

                    @Override
                    public void responseSuccess(@NonNull List<Milestone> milestones) {
                        loading = false;
                        adapterMilestones.setLoading(false);
                        nextPageUrl = LinkHeaderParser.parse(response()).getNext();
                        adapterMilestones.addData(milestones);
                    }
                });
    }

    @Subscribe
    public void onProjectReload(ProjectReloadEvent event) {
        project = event.project;
        loadData();
    }

    @Subscribe
    public void onMilestoneCreated(MilestoneCreatedEvent event) {
        adapterMilestones.addMilestone(event.milestone);
        if (getView() != null) {
            textMessage.setVisibility(View.GONE);
            listMilestones.smoothScrollToPosition(0);
        }
    }

    @Subscribe
    public void onMilestoneChanged(MilestoneChangedEvent event) {
        adapterMilestones.updateIssue(event.milestone);
    }
}

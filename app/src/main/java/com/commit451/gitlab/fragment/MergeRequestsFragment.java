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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.activity.ProjectActivity;
import com.commit451.gitlab.adapter.DividerItemDecoration;
import com.commit451.gitlab.adapter.MergeRequestAdapter;
import com.commit451.gitlab.event.MergeRequestChangedEvent;
import com.commit451.gitlab.event.ProjectReloadEvent;
import com.commit451.gitlab.model.api.MergeRequest;
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

public class MergeRequestsFragment extends ButterKnifeFragment {

    public static MergeRequestsFragment newInstance() {
        return new MergeRequestsFragment();
    }

    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.list)
    RecyclerView listMergeRequests;
    @BindView(R.id.message_text)
    TextView textMessage;
    @BindView(R.id.state_spinner)
    Spinner spinnerState;

    MergeRequestAdapter adapterMergeRequests;
    LinearLayoutManager layoutManagerMergeRequests;

    Project project;
    String state;
    String[] states;
    Uri nextPageUrl;
    boolean loading = false;

    private final AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            state = states[position];
            loadData();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    private final RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = layoutManagerMergeRequests.getChildCount();
            int totalItemCount = layoutManagerMergeRequests.getItemCount();
            int firstVisibleItem = layoutManagerMergeRequests.findFirstVisibleItemPosition();
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !loading && nextPageUrl != null) {
                loadMore();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        state = getContext().getResources().getString(R.string.merge_request_state_value_default);
        states = getContext().getResources().getStringArray(R.array.merge_request_state_values);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_merge_request, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        App.bus().register(this);

        adapterMergeRequests = new MergeRequestAdapter(new MergeRequestAdapter.Listener() {
            @Override
            public void onMergeRequestClicked(MergeRequest mergeRequest) {
                Navigator.navigateToMergeRequest(getActivity(), project, mergeRequest);
            }
        });
        layoutManagerMergeRequests = new LinearLayoutManager(getActivity());
        listMergeRequests.setLayoutManager(layoutManagerMergeRequests);
        listMergeRequests.addItemDecoration(new DividerItemDecoration(getActivity()));
        listMergeRequests.setAdapter(adapterMergeRequests);
        listMergeRequests.addOnScrollListener(onScrollListener);

        spinnerState.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, getResources().getStringArray(R.array.merge_request_state_names)));
        spinnerState.setOnItemSelectedListener(onItemSelectedListener);

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
        App.get().getGitLab().getMergeRequests(project.getId(), state)
                .compose(this.<Response<List<MergeRequest>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomResponseSingleObserver<List<MergeRequest>>() {

                    @Override
                    public void error(@NonNull Throwable e) {
                        loading = false;
                        Timber.e(e);
                        swipeRefreshLayout.setRefreshing(false);
                        textMessage.setVisibility(View.VISIBLE);
                        textMessage.setText(R.string.connection_error_merge_requests);
                        adapterMergeRequests.setData(null);
                        nextPageUrl = null;
                    }

                    @Override
                    public void responseSuccess(@NonNull List<MergeRequest> mergeRequests) {
                        loading = false;
                        swipeRefreshLayout.setRefreshing(false);
                        if (mergeRequests.isEmpty()) {
                            textMessage.setVisibility(View.VISIBLE);
                            textMessage.setText(R.string.no_merge_requests);
                        }
                        adapterMergeRequests.setData(mergeRequests);
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
        adapterMergeRequests.setLoading(true);
        loading = true;
        Timber.d("loadMore called for " + nextPageUrl);
        App.get().getGitLab().getMergeRequests(nextPageUrl.toString(), state)
                .compose(this.<Response<List<MergeRequest>>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomResponseSingleObserver<List<MergeRequest>>() {

                    @Override
                    public void error(@NonNull Throwable e) {
                        Timber.e(e);
                        adapterMergeRequests.setLoading(false);
                        loading = false;
                    }

                    @Override
                    public void responseSuccess(@NonNull List<MergeRequest> mergeRequests) {
                        loading = false;
                        adapterMergeRequests.setLoading(false);
                        nextPageUrl = LinkHeaderParser.parse(response()).getNext();
                        adapterMergeRequests.addData(mergeRequests);
                    }
                });
    }

    @Subscribe
    public void onProjectReload(ProjectReloadEvent event) {
        project = event.project;
        loadData();
    }

    @Subscribe
    public void onMergeRequestChanged(MergeRequestChangedEvent event) {
        loadData();
    }
}

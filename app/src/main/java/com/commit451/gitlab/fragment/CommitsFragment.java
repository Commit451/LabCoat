package com.commit451.gitlab.fragment;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.GitLabApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.activity.DiffActivity;
import com.commit451.gitlab.activity.ProjectActivity;
import com.commit451.gitlab.adapter.CommitsAdapter;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.event.ProjectReloadEvent;
import com.commit451.gitlab.model.Commit;
import com.commit451.gitlab.model.Project;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class CommitsFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    public static CommitsFragment newInstance() {
        return new CommitsFragment();
    }

    @Bind(R.id.list) RecyclerView listView;
    CommitsAdapter adapter;
    @Bind(R.id.swipe_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.message_text) View messageView;

    EventReceiver mEventReceiver;
    Project mProject;
    String mBranchName;

    private final CommitsAdapter.Listener mCommitsAdapterListener = new CommitsAdapter.Listener() {
        @Override
        public void onCommitClicked(Commit commit) {
            getActivity().startActivity(DiffActivity.newInstance(getActivity(), mProject, commit));
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new CommitsAdapter(mCommitsAdapterListener);
        mEventReceiver = new EventReceiver();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_commits, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        GitLabApp.bus().register(mEventReceiver);
        listView.setLayoutManager(new LinearLayoutManager(getActivity()));
        listView.setAdapter(adapter);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        if (getActivity() instanceof ProjectActivity) {
            mProject = ((ProjectActivity) getActivity()).getProject();
            mBranchName = ((ProjectActivity) getActivity()).getBranchName();
            if (!TextUtils.isEmpty(mBranchName) && mProject != null) {
                loadData();
            }
        } else {
            throw new IllegalStateException("Incorrect parent activity");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        GitLabApp.bus().unregister(mEventReceiver);
        ButterKnife.unbind(this);
    }

    @Override
    public void onRefresh() {
        loadData();
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
        GitLabClient.instance().getCommits(mProject.getId(), mBranchName).enqueue(commitsCallback);
    }

    public boolean onBackPressed() {
        return false;
    }

    private Callback<List<Commit>> commitsCallback = new Callback<List<Commit>>() {


        @Override
        public void onResponse(Response<List<Commit>> response, Retrofit retrofit) {
            if (getView() == null) {
                return;
            }
            if (!response.isSuccess()) {
                return;
            }
            mSwipeRefreshLayout.setRefreshing(false);

            if(response.body().size() > 0) {
                messageView.setVisibility(View.GONE);
            }
            else {
                Timber.d("No commits have been made");
                messageView.setVisibility(View.VISIBLE);
            }
            adapter.setData(response.body());
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t, null);
            if (getView() == null) {
                return;
            }

            if(mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
            messageView.setVisibility(View.VISIBLE);

            Snackbar.make(getActivity().getWindow().getDecorView(), getString(R.string.connection_error_commits), Snackbar.LENGTH_SHORT)
                    .show();
            adapter.setData(null);
        }
    };

    private class EventReceiver {

        @Subscribe
        public void onLoadReady(ProjectReloadEvent event) {
            mProject = event.project;
            mBranchName = event.branchName;
            loadData();
        }
    }
}
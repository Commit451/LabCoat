package com.commit451.gitlab.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.gitlab.LabCoatApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.activity.ProjectActivity;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.event.ProjectReloadEvent;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.model.api.RepositoryFile;
import com.commit451.gitlab.model.api.RepositoryTreeObject;
import com.commit451.gitlab.util.NavigationManager;
import com.commit451.gitlab.util.PicassoImageGetter;
import com.squareup.otto.Subscribe;

import java.nio.charset.Charset;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import in.uncod.android.bypass.Bypass;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class OverviewFragment extends BaseFragment {

    public static OverviewFragment newInstance() {
        return new OverviewFragment();
    }

    @Bind(R.id.swipe_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.creator) TextView mCreatorView;
    @Bind(R.id.star_count) TextView mStarCountView;
    @Bind(R.id.forks_count) TextView mForksCountView;
    @Bind(R.id.overview_text) TextView mOverviewVew;

    private Project mProject;
    private String mBranchName;
    private EventReceiver mEventReceiver;
    private Bypass mBypass;

    @OnClick(R.id.creator)
    void onCreatorClick() {
        if (mProject != null) {
            if (mProject.belongsToGroup()) {
                NavigationManager.navigateToGroup(getActivity(), mProject.getNamespace().getId());
            } else {
                NavigationManager.navigateToUser(getActivity(), mProject.getOwner());
            }
        }
    }

    private final Callback<List<RepositoryTreeObject>> mFilesCallback = new Callback<List<RepositoryTreeObject>>() {
        @Override
        public void onResponse(Response<List<RepositoryTreeObject>> response, Retrofit retrofit) {
            if (getView() == null) {
                return;
            }

            if (!response.isSuccess()) {
                Timber.e("Files response was not a success: %d", response.code());
                mSwipeRefreshLayout.setRefreshing(false);
                mOverviewVew.setText(R.string.connection_error_readme);
                return;
            }

            for (RepositoryTreeObject treeItem : response.body()) {
                if (treeItem.getName().equalsIgnoreCase("README.md")) {
                    GitLabClient.instance().getFile(mProject.getId(), treeItem.getName(), mBranchName).enqueue(mFileCallback);
                    return;
                }
            }

            mSwipeRefreshLayout.setRefreshing(false);
            mOverviewVew.setText(R.string.no_readme_found);
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t, null);

            if (getView() == null) {
                return;
            }

            mSwipeRefreshLayout.setRefreshing(false);

            mOverviewVew.setText(R.string.connection_error);
        }
    };

    private Callback<RepositoryFile> mFileCallback = new Callback<RepositoryFile>() {
        @Override
        public void onResponse(Response<RepositoryFile> response, Retrofit retrofit) {
            if (getView() == null) {
                return;
            }

            mSwipeRefreshLayout.setRefreshing(false);

            if (!response.isSuccess()) {
                Timber.e("File response was not a success: %d", response.code());
                mOverviewVew.setText(R.string.connection_error_readme);
                return;
            }

            String text = new String(Base64.decode(response.body().getContent(), Base64.DEFAULT), Charset.forName("UTF-8"));

            mOverviewVew.setText(mBypass.markdownToSpannable(text,
                    new PicassoImageGetter(mOverviewVew, GitLabClient.getPicasso())));
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t, null);

            if (getView() == null) {
                return;
            }

            mSwipeRefreshLayout.setRefreshing(false);

            mOverviewVew.setText(R.string.connection_error);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBypass = new Bypass(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_overview, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        mEventReceiver = new EventReceiver();
        LabCoatApp.bus().register(mEventReceiver);

        mOverviewVew.setMovementMethod(LinkMovementMethod.getInstance());

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });

        if (getActivity() instanceof ProjectActivity) {
            mProject = ((ProjectActivity) getActivity()).getProject();
            mBranchName = ((ProjectActivity) getActivity()).getBranchName();
            bindProject(mProject);
            loadData();
        } else {
            throw new IllegalStateException("Incorrect parent activity");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        LabCoatApp.bus().unregister(mEventReceiver);
    }

    @Override
    protected void loadData() {
        if (getView() == null) {
            return;
        }

        if (mProject == null || TextUtils.isEmpty(mBranchName)) {
            mSwipeRefreshLayout.setRefreshing(false);
            return;
        }

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            }
        });

        GitLabClient.instance().getTree(mProject.getId(), mBranchName, null).enqueue(mFilesCallback);
    }

    private void bindProject(Project project) {
        if (project.belongsToGroup()) {
            mCreatorView.setText(String.format(getString(R.string.created_by), project.getNamespace().getName()));
        } else {
            mCreatorView.setText(String.format(getString(R.string.created_by), project.getOwner().getUsername()));
        }
        mStarCountView.setText(String.valueOf(project.getStarCount()));
        mForksCountView.setText(String.valueOf(project.getForksCount()));
    }

    private class EventReceiver {
        @Subscribe
        public void onProjectReload(ProjectReloadEvent event) {
            mProject = event.mProject;
            mBranchName = event.mBranchName;
            loadData();
        }
    }
}

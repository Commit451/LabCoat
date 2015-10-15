package com.commit451.gitlab.fragments;

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

import com.commit451.gitlab.GitLabApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.activities.ProjectActivity;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.events.ProjectReloadEvent;
import com.commit451.gitlab.model.FileResponse;
import com.commit451.gitlab.model.Project;
import com.commit451.gitlab.model.TreeItem;
import com.commit451.gitlab.tools.PicassoImageGetter;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import java.io.UnsupportedEncodingException;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.uncod.android.bypass.Bypass;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

/**
 * Displays the README of the project if it exists
 * Created by John on 10/3/15.
 */
public class OverviewFragment extends BaseFragment {

    public static OverviewFragment newInstance() {

        Bundle args = new Bundle();

        OverviewFragment fragment = new OverviewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Bind(R.id.swipe_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.overview_text) TextView mOverview;
    @Bind(R.id.error_text) TextView mErrorText;

    EventReceiver mEventReceiver;
    Project mProject;
    String mBranchName;
    Bypass mBypass;

    private Callback<List<TreeItem>> mFilesCallback = new Callback<List<TreeItem>>() {
        @Override
        public void onResponse(Response<List<TreeItem>> response, Retrofit retrofit) {
            if (!response.isSuccess()) {
                mErrorText.setText(R.string.connection_error);
                return;
            }
            if (getView() == null) {
                return;
            }
            mSwipeRefreshLayout.setRefreshing(false);
            for (TreeItem treeItem : response.body()) {
                if (treeItem.getName().equalsIgnoreCase("README.md")) {
                    GitLabClient.instance().getFile(mProject.getId(), treeItem.getName(), mBranchName).enqueue(mFileCallback);
                    return;
                }
            }
            showError(getString(R.string.no_readme_found));
        }

        @Override
        public void onFailure(Throwable t) {
            if (getView() != null) {
                showError(getString(R.string.failed_to_load));
            }
        }
    };

    private Callback<FileResponse> mFileCallback = new Callback<FileResponse>() {
        @Override
        public void onResponse(Response<FileResponse> response, Retrofit retrofit) {
            if (!response.isSuccess()) {
                return;
            }
            if (getView() == null) {
                return;
            }
            try {
                String text = new String(Base64.decode(response.body().getContent(), Base64.DEFAULT), "UTF-8");
                mOverview.setText(mBypass.markdownToSpannable(text,
                        new PicassoImageGetter(mOverview, getResources(), Picasso.with(getActivity()))));
            } catch (UnsupportedEncodingException e) {
                Timber.e(e.toString());
                showError(getString(R.string.failed_to_load));
            }

        }

        @Override
        public void onFailure(Throwable t) {
            showError(getString(R.string.failed_to_load));
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEventReceiver = new EventReceiver();
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
        GitLabApp.bus().register(mEventReceiver);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });
        mOverview.setMovementMethod(LinkMovementMethod.getInstance());
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
    protected void loadData() {
        super.loadData();
        mErrorText.setVisibility(View.GONE);
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
        GitLabClient.instance().getTree(mProject.getId(), mBranchName, null).enqueue(mFilesCallback);
    }

    private void showError(String error) {
        mErrorText.setVisibility(View.VISIBLE);
        mOverview.setVisibility(View.GONE);
        mErrorText.setText(error);
    }

    public boolean onBackPressed() {
        return false;
    }

    private class EventReceiver {

        @Subscribe
        public void onProjectChanged(ProjectReloadEvent event) {
            mProject = event.project;
            mBranchName = event.branchName;
            loadData();
        }
    }
}

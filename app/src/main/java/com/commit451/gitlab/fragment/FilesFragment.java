package com.commit451.gitlab.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.commit451.gitlab.GitLabApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.activity.ProjectActivity;
import com.commit451.gitlab.adapter.BreadcrumbAdapter;
import com.commit451.gitlab.adapter.FilesAdapter;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.event.ProjectReloadEvent;
import com.commit451.gitlab.model.api.RepositoryTreeObject;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.util.IntentUtil;
import com.commit451.gitlab.util.NavigationManager;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class FilesFragment extends BaseFragment {

    public static FilesFragment newInstance() {
        Bundle args = new Bundle();
        FilesFragment fragment = new FilesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Bind(R.id.error_text) TextView mErrorText;
    @Bind(R.id.swipe_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.list) RecyclerView mFilesList;
    @Bind(R.id.breadcrumb) RecyclerView mBreadcrumbList;

    EventReceiver mEventReceiver;
    Project mProject;
    String mBranchName;
    FilesAdapter mFilesAdapter;
    BreadcrumbAdapter mBreadcrumbAdapter;
    String mCurrentPath = "";

    private FilesAdapter.Listener mFilesAdapterListener = new FilesAdapter.Listener() {

        @Override
        public void onFolderClicked(RepositoryTreeObject treeItem) {
            loadData(mCurrentPath + treeItem.getName() + "/");
        }

        @Override
        public void onFileClicked(RepositoryTreeObject treeItem) {
            String path = mCurrentPath + treeItem.getName();
            NavigationManager.navigateToFile(getActivity(), mProject.getId(), path, mBranchName);
        }

        @Override
        public void onCopyClicked(RepositoryTreeObject treeItem) {
            ClipboardManager clipboard = (ClipboardManager)
                    getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            // Creates a new text clip to put on the clipboard
            ClipData clip = ClipData.newPlainText(treeItem.getName(), treeItem.getUrl(mProject, mBranchName, mCurrentPath).toString());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getActivity(), R.string.copied_to_clipboard, Toast.LENGTH_SHORT)
                    .show();
        }

        @Override
        public void onShareClicked(RepositoryTreeObject treeItem){
            IntentUtil.share(getView(), treeItem.getUrl(mProject, mBranchName, mCurrentPath));
        }

        @Override
        public void onOpenInBrowserClicked(RepositoryTreeObject treeItem){
            IntentUtil.openPage(getView(), treeItem.getUrl(mProject, mBranchName, mCurrentPath));
        }
    };

    private class FilesCallback implements Callback<List<RepositoryTreeObject>> {
        String newPath;

        public FilesCallback(String newPath) {
            this.newPath = newPath;
        }

        @Override
        public void onResponse(Response<List<RepositoryTreeObject>> response, Retrofit retrofit) {
            if (getView() == null) {
                return;
            }
            mSwipeRefreshLayout.setRefreshing(false);
            if (!response.isSuccess()) {
                mFilesAdapter.clear();
                mErrorText.setVisibility(View.VISIBLE);
                return;
            }
            if (response.body().isEmpty()) {
                mFilesAdapter.clear();
                mErrorText.setVisibility(View.VISIBLE);
            } else {
                mFilesList.setVisibility(View.VISIBLE);
                mFilesAdapter.setData(response.body());
                mErrorText.setVisibility(View.GONE);

                mCurrentPath = newPath;
                updateBreadcrumbs();
            }
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t, null);
            if (getView() == null) {
                return;
            }
            mSwipeRefreshLayout.setRefreshing(false);
            Snackbar.make(getActivity().getWindow().getDecorView(), getString(R.string.connection_error_files), Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_files, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        mFilesAdapter = new FilesAdapter(mFilesAdapterListener);
        mFilesList.setAdapter(mFilesAdapter);
        mFilesList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mBreadcrumbAdapter = new BreadcrumbAdapter();
        mBreadcrumbList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        mBreadcrumbList.setAdapter(mBreadcrumbAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });

        mEventReceiver = new EventReceiver();
        GitLabApp.bus().register(mEventReceiver);

        if (getActivity() instanceof ProjectActivity) {
            mProject = ((ProjectActivity) getActivity()).getProject();
            mBranchName = ((ProjectActivity) getActivity()).getBranchName();
            if (!TextUtils.isEmpty(mBranchName) && mProject != null) {
                loadData("");
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
        loadData(mCurrentPath);
    }

    public void loadData(String newPath) {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            }
        });

        GitLabClient.instance().getTree(mProject.getId(), mBranchName, newPath).enqueue(new FilesCallback(newPath));
    }

    @Override
    public boolean onBackPressed() {
        if (mBreadcrumbAdapter.getItemCount() > 1) {
            BreadcrumbAdapter.Breadcrumb breadcrumb = mBreadcrumbAdapter.getValueAt(mBreadcrumbAdapter.getItemCount() - 2);
            if (breadcrumb != null && breadcrumb.getListener() != null) {
                breadcrumb.getListener().onClick();
                return true;
            }
        }

        return false;
    }

    private void updateBreadcrumbs() {
        List<BreadcrumbAdapter.Breadcrumb> breadcrumbs = new ArrayList<>();
        breadcrumbs.add(new BreadcrumbAdapter.Breadcrumb("ROOT", new BreadcrumbAdapter.Listener() {
            @Override
            public void onClick() {
                loadData("");
            }
        }));

        String newPath = "";

        String[] segments = mCurrentPath.split("/");
        for (String segment : segments) {
            if (segment.isEmpty()) {
                continue;
            }

            newPath += segment + "/";

            final String finalPath = newPath;
            breadcrumbs.add(new BreadcrumbAdapter.Breadcrumb(segment, new BreadcrumbAdapter.Listener() {
                @Override
                public void onClick() {
                    loadData(finalPath);
                }
            }));
        }

        mBreadcrumbAdapter.setData(breadcrumbs);
        mBreadcrumbList.scrollToPosition(mBreadcrumbAdapter.getItemCount() - 1);
    }

    private class EventReceiver {

        @Subscribe
        public void onLoadReady(ProjectReloadEvent event) {
            mProject = event.project;
            mBranchName = event.branchName;

            loadData("");
        }
    }
}
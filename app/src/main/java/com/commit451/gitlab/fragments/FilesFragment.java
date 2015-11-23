package com.commit451.gitlab.fragments;

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
import com.commit451.gitlab.activities.ProjectActivity;
import com.commit451.gitlab.adapter.BreadcrumbAdapter;
import com.commit451.gitlab.adapter.FilesAdapter;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.events.ProjectReloadEvent;
import com.commit451.gitlab.model.Project;
import com.commit451.gitlab.model.TreeItem;
import com.commit451.gitlab.tools.IntentUtil;
import com.commit451.gitlab.tools.NavigationManager;
import com.squareup.otto.Subscribe;

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

    private BreadcrumbAdapter.Listener mBreadcrumbAdapterListener = new BreadcrumbAdapter.Listener() {
        @Override
        public void onBreadcrumbClicked() {
            loadData();
        }
    };

    private FilesAdapter.Listener mFilesAdapterListener = new FilesAdapter.Listener() {

        @Override
        public void onFolderClicked(TreeItem treeItem) {
            mBreadcrumbAdapter.addBreadcrumb(treeItem.getName());
            mBreadcrumbList.scrollToPosition(mBreadcrumbAdapter.getItemCount() - 1);
            loadData();
        }

        @Override
        public void onFileClicked(TreeItem treeItem) {
            String pathExtra = mBreadcrumbAdapter.getCurrentPath();
            pathExtra = pathExtra + treeItem.getName();
            NavigationManager.navigateToFile(getActivity(), mProject.getId(), pathExtra, mBranchName);
        }

        @Override
        public void onCopyClicked(TreeItem treeItem) {
            ClipboardManager clipboard = (ClipboardManager)
                    getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            // Creates a new text clip to put on the clipboard
            ClipData clip = ClipData.newPlainText(treeItem.getName(), treeItem.getUrl(mProject, mBranchName, mBreadcrumbAdapter.getCurrentPath()));
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getActivity(), R.string.copied_to_clipboard, Toast.LENGTH_SHORT)
                    .show();
        }

        @Override
        public void onShareClicked(TreeItem treeItem){
            IntentUtil.share(getView(), treeItem.getUrl(mProject, mBranchName, mBreadcrumbAdapter.getCurrentPath()));
        }

        @Override
        public void onOpenInBrowserClicked(TreeItem treeItem){
            IntentUtil.openPage(getView(), treeItem.getUrl(mProject, mBranchName, mBreadcrumbAdapter.getCurrentPath()));
        }
    };

	private Callback<List<TreeItem>> mFilesCallback = new Callback<List<TreeItem>>() {

		@Override
		public void onResponse(Response<List<TreeItem>> response, Retrofit retrofit) {
            if (getView() == null) {
                return;
            }
            mSwipeRefreshLayout.setRefreshing(false);
			if (!response.isSuccess()) {
                mBreadcrumbAdapter.clear();
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
			}
		}

		@Override
		public void onFailure(Throwable t) {
            Timber.e(t.toString());
            if (getView() == null) {
                return;
            }
            mSwipeRefreshLayout.setRefreshing(false);
			Snackbar.make(getActivity().getWindow().getDecorView(), getString(R.string.connection_error_files), Snackbar.LENGTH_SHORT)
					.show();
		}
	};

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
        mBreadcrumbAdapter = new BreadcrumbAdapter(mBreadcrumbAdapterListener);
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
		mSwipeRefreshLayout.post(new Runnable() {
			@Override
			public void run() {
				if (mSwipeRefreshLayout != null) {
					mSwipeRefreshLayout.setRefreshing(true);
				}
			}
		});

        GitLabClient.instance().getTree(mProject.getId(), mBranchName, mBreadcrumbAdapter.getCurrentPath()).enqueue(mFilesCallback);
    }
	
	public boolean onBackPressed() {
//		if(mPath.size() > 0) {
//            mPath.remove(mPath.size() - 1);
//            loadData();
//			return true;
//		}
//
		return false;
	}

	private class EventReceiver {

		@Subscribe
		public void onLoadReady(ProjectReloadEvent event) {
            mBreadcrumbAdapter.clear();
            mProject = event.project;
            mBranchName = event.branchName;
			loadData();
		}
	}
}
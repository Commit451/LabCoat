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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.commit451.gitlab.GitLabApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.activities.FileActivity;
import com.commit451.gitlab.activities.ProjectActivity;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.events.ProjectReloadEvent;
import com.commit451.gitlab.model.Project;
import com.commit451.gitlab.model.TreeItem;
import com.commit451.gitlab.tools.IntentUtil;
import com.commit451.gitlab.viewHolders.FileViewHolder;
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

	@Bind(R.id.error_text) TextView errorText;
    @Bind(R.id.swipe_layout) SwipeRefreshLayout mSwipeRefreshLayout;
	@Bind(R.id.list) RecyclerView list;

	EventReceiver eventReceiver;
    Project mProject;
    String mBranchName;
    ArrayList<String> mPath;

	private Callback<List<TreeItem>> mFilesCallback = new Callback<List<TreeItem>>() {

		@Override
		public void onResponse(Response<List<TreeItem>> response, Retrofit retrofit) {
			if (!response.isSuccess()) {
				if(response.code() == 404) {
					errorText.setVisibility(View.VISIBLE);
					list.setVisibility(View.GONE);
				}
				else {
					if(mPath.size() > 0) {
						mPath.remove(mPath.size() - 1);
					}
					list.setAdapter(null);

					if(response.code() != 500) {
						Snackbar.make(getActivity().getWindow().getDecorView(), getString(R.string.connection_error_files), Snackbar.LENGTH_SHORT)
								.show();
					}
				}
				return;
			}
			if (getView() == null) {
				return;
			}
			mSwipeRefreshLayout.setRefreshing(false);
			if (response.body() != null && !response.body().isEmpty()) {
				list.setVisibility(View.VISIBLE);
				list.setAdapter(new FilesAdapter(response.body()));
				errorText.setVisibility(View.GONE);
			} else {
				errorText.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void onFailure(Throwable t) {
			if(mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
				mSwipeRefreshLayout.setRefreshing(false);
			}
			Snackbar.make(getActivity().getWindow().getDecorView(), getString(R.string.connection_error_files), Snackbar.LENGTH_SHORT)
					.show();
			Timber.e(t.toString());

		}
	};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPath = new ArrayList<>();
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_files, container, false);
		ButterKnife.bind(this, view);

		list.setLayoutManager(new LinearLayoutManager(getActivity()));

		mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });

		eventReceiver = new EventReceiver();
		GitLabApp.bus().register(eventReceiver);

		if (getActivity() instanceof ProjectActivity) {
            mProject = ((ProjectActivity) getActivity()).getProject();
			mBranchName = ((ProjectActivity) getActivity()).getBranchName();
			if (!TextUtils.isEmpty(mBranchName) && mProject != null) {
				loadData();
			}
		} else {
			throw new IllegalStateException("Incorrect parent activity");
		}
		
		return view;
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
        GitLabApp.bus().unregister(eventReceiver);
        ButterKnife.unbind(this);
	}

	@Override
	protected void loadData() {
        Timber.d("loadData");
		mSwipeRefreshLayout.post(new Runnable() {
			@Override
			public void run() {
				if (mSwipeRefreshLayout != null) {
					mSwipeRefreshLayout.setRefreshing(true);
				}
			}
		});

        String currentPath = "";
        for(String p : mPath) {
            currentPath += p;
        }

        GitLabClient.instance().getTree(mProject.getId(), mBranchName, currentPath).enqueue(mFilesCallback);
    }
	
	public boolean onBackPressed() {
		if(mPath.size() > 0) {
            mPath.remove(mPath.size() - 1);
            loadData();
			return true;
		}
		
		return false;
	}

	private class EventReceiver {

		@Subscribe
		public void onLoadReady(ProjectReloadEvent event) {
            mPath.clear();
            mProject = event.project;
            mBranchName = event.branchName;
			loadData();
		}
	}

	public class FilesAdapter extends RecyclerView.Adapter<FileViewHolder> {

		private List<TreeItem> mValues;

		public TreeItem getValueAt(int position) {
			return mValues.get(position);
		}

		public FilesAdapter(List<TreeItem> items) {
			mValues = items;
		}

		private final View.OnClickListener onProjectClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int position = (int) v.getTag(R.id.list_position);
				TreeItem treeItem = getValueAt(position);

				if(treeItem.getType().equals("tree")) {
                    mPath.add(treeItem.getName() + "/");
					loadData();
				}
				else if(treeItem.getType().equals("blob")) {
					String pathExtra = "";
					for(String p : mPath) {
						pathExtra += p;
					}
                    pathExtra = pathExtra + treeItem.getName();
					startActivity(FileActivity.newIntent(getActivity(), mProject.getId(), pathExtra, mBranchName));
				}
			}
		};

		@Override
		public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			FileViewHolder holder = FileViewHolder.newInstance(parent);
			holder.itemView.setOnClickListener(onProjectClickListener);
			return holder;
		}

		@Override
		public void onBindViewHolder(final FileViewHolder holder, int position) {
			final TreeItem treeItem = getValueAt(position);
			holder.bind(treeItem);
			holder.itemView.setTag(R.id.list_position, position);
            holder.popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_copy:
                            ClipboardManager clipboard = (ClipboardManager)
                                    getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                            // Creates a new text clip to put on the clipboard
                            ClipData clip = ClipData.newPlainText(treeItem.getName(), treeItem.getUrl(mProject, mBranchName, mPath));
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(getActivity(), R.string.copied_to_clipboard, Toast.LENGTH_SHORT)
                                    .show();
                            return true;
                        case R.id.action_share:
                            IntentUtil.share(getView(), treeItem.getUrl(mProject, mBranchName, mPath));
                            return true;
                        case R.id.action_open:
                            IntentUtil.openPage(getView(), treeItem.getUrl(mProject, mBranchName, mPath));
                    }
                    return false;
                }
            });
		}

		@Override
		public int getItemCount() {
			return mValues.size();
		}
	}
}
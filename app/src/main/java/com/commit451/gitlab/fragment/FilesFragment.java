package com.commit451.gitlab.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.activity.ProjectActivity;
import com.commit451.gitlab.adapter.BreadcrumbAdapter;
import com.commit451.gitlab.adapter.DividerItemDecoration;
import com.commit451.gitlab.adapter.FileAdapter;
import com.commit451.gitlab.event.ProjectReloadEvent;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.model.api.RepositoryTreeObject;
import com.commit451.gitlab.navigation.Navigator;
import com.commit451.gitlab.rx.CustomSingleObserver;
import com.commit451.gitlab.util.IntentUtil;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class FilesFragment extends ButterKnifeFragment {

    public static FilesFragment newInstance() {
        return new FilesFragment();
    }

    @BindView(R.id.root)
    View root;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.list)
    RecyclerView list;
    @BindView(R.id.breadcrumb)
    RecyclerView listBreadcrumbs;
    @BindView(R.id.message_text)
    TextView textMessage;

    private Project project;
    private String branchName;
    private FileAdapter adapterFiles;
    private BreadcrumbAdapter adapterBreadcrumb;
    private String currentPath = "";

    private final FileAdapter.Listener mFilesAdapterListener = new FileAdapter.Listener() {
        @Override
        public void onFolderClicked(RepositoryTreeObject treeItem) {
            loadData(currentPath + treeItem.getName() + "/");
        }

        @Override
        public void onFileClicked(RepositoryTreeObject treeItem) {
            String path = currentPath + treeItem.getName();
            Navigator.navigateToFile(getActivity(), project.getId(), path, branchName);
        }

        @Override
        public void onCopyClicked(RepositoryTreeObject treeItem) {
            ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);

            // Creates a new text clip to put on the clipboard
            ClipData clip = ClipData.newPlainText(treeItem.getName(), treeItem.getUrl(project, branchName, currentPath).toString());
            clipboard.setPrimaryClip(clip);
            Snackbar.make(root, R.string.copied_to_clipboard, Snackbar.LENGTH_SHORT)
                    .show();
        }

        @Override
        public void onShareClicked(RepositoryTreeObject treeItem) {
            IntentUtil.share(getView(), treeItem.getUrl(project, branchName, currentPath));
        }

        @Override
        public void onOpenInBrowserClicked(RepositoryTreeObject treeItem) {
            IntentUtil.openPage(getActivity(), treeItem.getUrl(project, branchName, currentPath).toString());
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_files, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        App.bus().register(this);

        adapterFiles = new FileAdapter(mFilesAdapterListener);
        list.setLayoutManager(new LinearLayoutManager(getActivity()));
        list.addItemDecoration(new DividerItemDecoration(getActivity()));
        list.setAdapter(adapterFiles);

        adapterBreadcrumb = new BreadcrumbAdapter();
        listBreadcrumbs.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        listBreadcrumbs.setAdapter(adapterBreadcrumb);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });

        if (getActivity() instanceof ProjectActivity) {
            project = ((ProjectActivity) getActivity()).getProject();
            branchName = ((ProjectActivity) getActivity()).getRef();
            loadData("");
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
        loadData(currentPath);
    }

    public void loadData(final String newPath) {
        if (getView() == null) {
            return;
        }

        if (project == null || TextUtils.isEmpty(branchName)) {
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(true);
                }
            }
        });

        App.get().getGitLab().getTree(project.getId(), branchName, newPath)
                .compose(this.<List<RepositoryTreeObject>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomSingleObserver<List<RepositoryTreeObject>>() {

                    @Override
                    public void error(@NonNull Throwable e) {
                        Timber.e(e);
                        swipeRefreshLayout.setRefreshing(false);
                        textMessage.setVisibility(View.VISIBLE);
                        textMessage.setText(R.string.connection_error_files);
                        adapterFiles.setData(null);
                        currentPath = newPath;
                        updateBreadcrumbs();
                    }

                    @Override
                    public void success(@NonNull List<RepositoryTreeObject> repositoryTreeObjects) {
                        swipeRefreshLayout.setRefreshing(false);
                        if (!repositoryTreeObjects.isEmpty()) {
                            textMessage.setVisibility(View.GONE);
                        } else {
                            Timber.d("No files found");
                            textMessage.setVisibility(View.VISIBLE);
                            textMessage.setText(R.string.no_files_found);
                        }

                        adapterFiles.setData(repositoryTreeObjects);
                        list.scrollToPosition(0);
                        currentPath = newPath;
                        updateBreadcrumbs();
                    }
                });
    }

    @Override
    public boolean onBackPressed() {
        if (adapterBreadcrumb.getItemCount() > 1) {
            BreadcrumbAdapter.Breadcrumb breadcrumb = adapterBreadcrumb.getValueAt(adapterBreadcrumb.getItemCount() - 2);
            if (breadcrumb != null && breadcrumb.getListener() != null) {
                breadcrumb.getListener().onClick();
                return true;
            }
        }

        return false;
    }

    private void updateBreadcrumbs() {
        List<BreadcrumbAdapter.Breadcrumb> breadcrumbs = new ArrayList<>();
        breadcrumbs.add(new BreadcrumbAdapter.Breadcrumb(getString(R.string.root), new BreadcrumbAdapter.Listener() {
            @Override
            public void onClick() {
                loadData("");
            }
        }));

        String newPath = "";

        String[] segments = currentPath.split("/");
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

        adapterBreadcrumb.setData(breadcrumbs);
        listBreadcrumbs.scrollToPosition(adapterBreadcrumb.getItemCount() - 1);
    }


    @Subscribe
    public void onProjectReload(ProjectReloadEvent event) {
        project = event.project;
        branchName = event.branchName;

        loadData("");
    }
}
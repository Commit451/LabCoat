package com.commit451.gitlab.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.activity.ProjectActivity;
import com.commit451.gitlab.event.ProjectReloadEvent;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.model.api.RepositoryFile;
import com.commit451.gitlab.model.api.RepositoryTreeObject;
import com.commit451.gitlab.navigation.Navigator;
import com.commit451.gitlab.rx.CustomSingleObserver;
import com.commit451.gitlab.rx.DecodeObservableFactory;
import com.commit451.gitlab.util.BypassImageGetterFactory;
import com.commit451.gitlab.util.InternalLinkMovementMethod;
import com.commit451.reptar.Result;
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;
import com.vdurmont.emoji.EmojiParser;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import in.uncod.android.bypass.Bypass;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Shows the overview of the project
 */
public class ProjectFragment extends ButterKnifeFragment {

    private static final int README_TYPE_UNKNOWN = -1;
    private static final int README_TYPE_MARKDOWN = 0;
    private static final int README_TYPE_TEXT = 1;
    private static final int README_TYPE_HTML = 2;
    private static final int README_TYPE_NO_EXTENSION = 3;

    public static ProjectFragment newInstance() {
        return new ProjectFragment();
    }

    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.creator)
    TextView textCreator;
    @BindView(R.id.star_count)
    TextView textStarCount;
    @BindView(R.id.forks_count)
    TextView textForksCount;
    @BindView(R.id.overview_text)
    TextView textOverview;

    Project project;
    String branchName;
    Bypass bypass;

    @OnClick(R.id.creator)
    void onCreatorClick() {
        if (project != null) {
            if (project.belongsToGroup()) {
                Navigator.navigateToGroup(getActivity(), project.getNamespace().getId());
            } else {
                Navigator.navigateToUser(getActivity(), project.getOwner());
            }
        }
    }

    @OnClick(R.id.root_fork)
    void onForkClicked() {
        if (project != null) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.project_fork_title)
                    .setMessage(R.string.project_fork_message)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            App.get().getGitLab().forkProject(project.getId())
                                    .compose(ProjectFragment.this.<String>bindToLifecycle())
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new CustomSingleObserver<String>() {

                                        @Override
                                        public void error(@NonNull Throwable t) {
                                            Snackbar.make(swipeRefreshLayout, R.string.fork_failed, Snackbar.LENGTH_SHORT)
                                                    .show();
                                        }

                                        @Override
                                        public void success(@NonNull String s) {
                                            Snackbar.make(swipeRefreshLayout, R.string.project_forked, Snackbar.LENGTH_SHORT)
                                                    .show();
                                        }
                                    });
                        }
                    })
                    .show();
        }
    }

    @OnClick(R.id.root_star)
    void onStarClicked() {
        if (project != null) {
            App.get().getGitLab().starProject(project.getId())
                    .compose(this.<Response<Project>>bindToLifecycle())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new CustomSingleObserver<Response<Project>>() {

                        @Override
                        public void error(@NonNull Throwable t) {
                            if (t instanceof HttpException) {
                                if (((HttpException) t).response().code() == 304) {
                                    Snackbar.make(swipeRefreshLayout, R.string.project_already_starred, Snackbar.LENGTH_SHORT)
                                            .setAction(R.string.project_unstar, new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    unstarProject();
                                                }
                                            })
                                            .show();
                                    return;
                                }
                            }
                            Snackbar.make(swipeRefreshLayout, R.string.project_star_failed, Snackbar.LENGTH_SHORT)
                                    .show();
                        }

                        @Override
                        public void success(@NonNull Response<Project> projectResponse) {
                            Snackbar.make(swipeRefreshLayout, R.string.project_starred, Snackbar.LENGTH_SHORT)
                                    .show();
                        }
                    });
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bypass = new Bypass(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_project, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        App.bus().register(this);

        textOverview.setMovementMethod(new InternalLinkMovementMethod(App.get().getAccount().getServerUrl()));

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });

        if (getActivity() instanceof ProjectActivity) {
            project = ((ProjectActivity) getActivity()).getProject();
            branchName = ((ProjectActivity) getActivity()).getRefRef();
            bindProject(project);
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

        final ReadmeResult result = new ReadmeResult();
        App.get().getGitLab().getTree(project.getId(), branchName, null)
                .flatMap(new Function<List<RepositoryTreeObject>, SingleSource<Result<RepositoryTreeObject>>>() {
                    @Override
                    public SingleSource<Result<RepositoryTreeObject>> apply(List<RepositoryTreeObject> repositoryTreeObjects) throws Exception {
                        for (RepositoryTreeObject treeItem : repositoryTreeObjects) {
                            if (getReadmeType(treeItem.getName()) != README_TYPE_UNKNOWN) {
                                return Single.just(new Result<>(treeItem));
                            }
                        }
                        return Single.just(Result.<RepositoryTreeObject>empty());
                    }
                })
                .flatMap(new Function<Result<RepositoryTreeObject>, SingleSource<Result<RepositoryFile>>>() {
                    @Override
                    public SingleSource<Result<RepositoryFile>> apply(Result<RepositoryTreeObject> repositoryTreeObjectResult) throws Exception {
                        if (repositoryTreeObjectResult.isPresent()) {
                            RepositoryFile repositoryFile = App.get().getGitLab().getFile(project.getId(), repositoryTreeObjectResult.get().getName(), branchName)
                                    .blockingGet();
                            result.repositoryFile = repositoryFile;
                            return Single.just(new Result<>(repositoryFile));
                        }
                        return Single.just(Result.<RepositoryFile>empty());
                    }
                })
                .flatMap(new Function<Result<RepositoryFile>, SingleSource<ReadmeResult>>() {
                    @Override
                    public SingleSource<ReadmeResult> apply(Result<RepositoryFile> repositoryFileResult) throws Exception {
                        if (repositoryFileResult.isPresent()) {
                            result.bytes = DecodeObservableFactory.newDecode(repositoryFileResult.get().getContent())
                                    .blockingGet();
                            return Single.just(result);
                        }
                        return Single.just(result);
                    }
                })
                .compose(this.<ReadmeResult>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomSingleObserver<ReadmeResult>() {

                    @Override
                    public void error(@NonNull Throwable t) {
                        Timber.e(t);
                        swipeRefreshLayout.setRefreshing(false);
                        textOverview.setText(R.string.connection_error_readme);
                    }

                    @Override
                    public void success(@NonNull ReadmeResult readmeResult) {
                        swipeRefreshLayout.setRefreshing(false);
                        if (result.repositoryFile != null && result.bytes != null) {
                            String text = new String(result.bytes);
                            switch (getReadmeType(result.repositoryFile.getFileName())) {
                                case README_TYPE_MARKDOWN:
                                    text = EmojiParser.parseToUnicode(text);
                                    textOverview.setText(bypass.markdownToSpannable(text,
                                            BypassImageGetterFactory.create(textOverview,
                                                    App.get().getPicasso(),
                                                    App.get().getAccount().getServerUrl().toString(),
                                                    project)));
                                    break;
                                case README_TYPE_HTML:
                                    textOverview.setText(Html.fromHtml(text));
                                    break;
                                case README_TYPE_TEXT:
                                    textOverview.setText(text);
                                    break;
                                case README_TYPE_NO_EXTENSION:
                                    textOverview.setText(text);
                                    break;
                            }
                        } else {
                            textOverview.setText(R.string.no_readme_found);
                        }
                    }
                });
    }

    private void bindProject(Project project) {
        if (project == null) {
            return;
        }

        if (project.belongsToGroup()) {
            textCreator.setText(String.format(getString(R.string.created_by), project.getNamespace().getName()));
        } else {
            textCreator.setText(String.format(getString(R.string.created_by), project.getOwner().getUsername()));
        }
        textStarCount.setText(String.valueOf(project.getStarCount()));
        textForksCount.setText(String.valueOf(project.getForksCount()));
    }

    private int getReadmeType(String filename) {
        switch (filename.toLowerCase()) {
            case "readme.md":
                return README_TYPE_MARKDOWN;
            case "readme.html":
            case "readme.htm":
                return README_TYPE_HTML;
            case "readme.txt":
                return README_TYPE_TEXT;
            case "readme":
                return README_TYPE_NO_EXTENSION;
        }
        return README_TYPE_UNKNOWN;
    }

    private void unstarProject() {
        App.get().getGitLab().unstarProject(project.getId())
                .compose(this.<Project>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomSingleObserver<Project>() {

                    @Override
                    public void error(@NonNull Throwable t) {
                        Snackbar.make(swipeRefreshLayout, R.string.unstar_failed, Snackbar.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void success(@NonNull Project project) {
                        Snackbar.make(swipeRefreshLayout, com.commit451.gitlab.R.string.project_unstarred, Snackbar.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    private static class ReadmeResult {
        byte[] bytes;
        RepositoryFile repositoryFile;
    }

    @Subscribe
    public void onProjectReload(ProjectReloadEvent event) {
        project = event.project;
        branchName = event.branchName;
        loadData();
    }

}

package com.commit451.gitlab.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.commit451.alakazam.HideRunnable;
import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.ProjectSectionsPagerAdapter;
import com.commit451.gitlab.event.ProjectReloadEvent;
import com.commit451.gitlab.fragment.BaseFragment;
import com.commit451.gitlab.model.Ref;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.navigation.Navigator;
import com.commit451.gitlab.rx.CustomSingleObserver;
import com.commit451.gitlab.util.IntentUtil;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class ProjectActivity extends BaseActivity {

    private static final String EXTRA_PROJECT = "extra_project";
    private static final String EXTRA_PROJECT_ID = "extra_project_id";
    private static final String EXTRA_PROJECT_NAMESPACE = "extra_project_namespace";
    private static final String EXTRA_PROJECT_NAME = "extra_project_name";

    private static final String STATE_REF = "ref";
    private static final String STATE_PROJECT = "project";

    private static final int REQUEST_BRANCH_OR_TAG = 1;

    public static Intent newIntent(Context context, Project project) {
        Intent intent = new Intent(context, ProjectActivity.class);
        intent.putExtra(EXTRA_PROJECT, Parcels.wrap(project));
        return intent;
    }

    public static Intent newIntent(Context context, String projectId) {
        Intent intent = new Intent(context, ProjectActivity.class);
        intent.putExtra(EXTRA_PROJECT_ID, projectId);
        return intent;
    }

    public static Intent newIntent(Context context, String projectNamespace, String projectName) {
        Intent intent = new Intent(context, ProjectActivity.class);
        intent.putExtra(EXTRA_PROJECT_NAMESPACE, projectNamespace);
        intent.putExtra(EXTRA_PROJECT_NAME, projectName);
        return intent;
    }

    @BindView(R.id.root)
    ViewGroup root;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tabs)
    TabLayout tabLayout;
    @BindView(R.id.progress)
    View progress;
    @BindView(R.id.pager)
    ViewPager viewPager;

    Project project;
    Ref ref;

    private final Toolbar.OnMenuItemClickListener onMenuItemClickListener = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_branch:
                    if (project != null) {
                        Navigator.navigateToPickBranchOrTag(ProjectActivity.this, project.getId(), ref, REQUEST_BRANCH_OR_TAG);
                    }
                    return true;
                case R.id.action_share:
                    if (project != null) {
                        IntentUtil.share(root, project.getWebUrl());
                    }
                    return true;
                case R.id.action_copy_git_https:
                    if (project == null || project.getHttpUrlToRepo() == null) {
                        Toast.makeText(ProjectActivity.this, R.string.failed_to_copy_to_clipboard, Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        copyToClipboard(project.getHttpUrlToRepo());
                    }
                    return true;
                case R.id.action_copy_git_ssh:
                    if (project == null || project.getHttpUrlToRepo() == null) {
                        Toast.makeText(ProjectActivity.this, R.string.failed_to_copy_to_clipboard, Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        copyToClipboard(project.getSshUrlToRepo());
                    }
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);
        ButterKnife.bind(this);
        Project project = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_PROJECT));

        if (savedInstanceState != null) {
            project = Parcels.unwrap(savedInstanceState.getParcelable(STATE_PROJECT));
            ref = Parcels.unwrap(savedInstanceState.getParcelable(STATE_REF));
        }
        toolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolbar.inflateMenu(R.menu.menu_project);
        toolbar.setOnMenuItemClickListener(onMenuItemClickListener);

        if (project == null) {
            String projectId = getIntent().getStringExtra(EXTRA_PROJECT_ID);
            String projectNamespace = getIntent().getStringExtra(EXTRA_PROJECT_NAMESPACE);
            if (projectId != null) {
                loadProject(projectId);
            } else if (projectNamespace != null) {
                String projectName = getIntent().getStringExtra(EXTRA_PROJECT_NAME);
                loadProject(projectNamespace, projectName);
            } else {
                throw new IllegalStateException("You did something wrong and now we don't know what project to load. :(");
            }
        } else {
            bindProject(project);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_BRANCH_OR_TAG:
                if (resultCode == RESULT_OK) {
                    ref = Parcels.unwrap(data.getParcelableExtra(PickBranchOrTagActivity.EXTRA_REF));
                    broadcastLoad();
                }
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_REF, Parcels.wrap(ref));
        outState.putParcelable(STATE_PROJECT, Parcels.wrap(project));
    }

    private void loadProject(String projectId) {
        showProgress();
        loadProject(App.get().getGitLab().getProject(projectId));
    }

    private void loadProject(String projectNamespace, String projectName) {
        showProgress();
        loadProject(App.get().getGitLab().getProject(projectNamespace, projectName));
    }

    private void loadProject(Single<Project> observable) {
        observable.compose(this.<Project>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomSingleObserver<Project>() {

                    @Override
                    public void error(@NonNull Throwable t) {
                        Timber.e(t);
                        progress.animate()
                                .alpha(0.0f)
                                .withEndAction(new HideRunnable(progress));
                        Snackbar.make(root, getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void success(@NonNull Project project) {
                        progress.animate()
                                .alpha(0.0f)
                                .withEndAction(new HideRunnable(progress));
                        bindProject(project);
                    }
                });
    }

    private void showProgress() {
        progress.setAlpha(0.0f);
        progress.setVisibility(View.VISIBLE);
        progress.animate().alpha(1.0f);
    }

    private void broadcastLoad() {
        App.bus().post(new ProjectReloadEvent(project, ref.getRef()));
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + viewPager.getCurrentItem());
        if (fragment instanceof BaseFragment) {
            if (((BaseFragment) fragment).onBackPressed()) {
                return;
            }
        }

        super.onBackPressed();
    }

    public String getRef() {
        if (ref == null) {
            return null;
        }
        return ref.getRef();
    }

    public Project getProject() {
        return project;
    }

    private void bindProject(Project project) {
        this.project = project;
        if (ref == null) {
            ref = new Ref(Ref.TYPE_BRANCH, this.project.getDefaultBranch());
        }
        toolbar.setTitle(this.project.getName());
        toolbar.setSubtitle(this.project.getNamespace().getName());
        setupTabs();
    }

    private void setupTabs() {
        ProjectSectionsPagerAdapter projectSectionsPagerAdapter = new ProjectSectionsPagerAdapter(this, getSupportFragmentManager());
        viewPager.setAdapter(projectSectionsPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void copyToClipboard(String url) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        // Creates a new text clip to put on the clipboard
        ClipData clip = ClipData.newPlainText(project.getName(), url);
        clipboard.setPrimaryClip(clip);
        Snackbar.make(root, R.string.copied_to_clipboard, Snackbar.LENGTH_SHORT)
                .show();
    }
}

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

import com.commit451.easycallback.EasyCallback;
import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.ProjectSectionsPagerAdapter;
import com.commit451.gitlab.animation.HideRunnable;
import com.commit451.gitlab.event.ProjectReloadEvent;
import com.commit451.gitlab.fragment.BaseFragment;
import com.commit451.gitlab.model.Ref;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.navigation.Navigator;
import com.commit451.gitlab.util.IntentUtil;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Callback;
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
    ViewGroup mRoot;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.tabs)
    TabLayout mTabLayout;
    @BindView(R.id.progress)
    View mProgress;
    @BindView(R.id.pager)
    ViewPager mViewPager;

    Project mProject;
    Ref mRef;

    private final Callback<Project> mProjectCallback = new EasyCallback<Project>() {
        @Override
        public void success(@NonNull Project response) {
            mProgress.animate()
                    .alpha(0.0f)
                    .withEndAction(new HideRunnable(mProgress));
            bindProject(response);
        }

        @Override
        public void failure(Throwable t) {
            Timber.e(t);
            mProgress.animate()
                    .alpha(0.0f)
                    .withEndAction(new HideRunnable(mProgress));
            Snackbar.make(mRoot, getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                    .show();
        }
    };

    private final Toolbar.OnMenuItemClickListener mOnMenuItemClickListener = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_branch:
                    if (mProject != null) {
                        Navigator.navigateToPickBranchOrTag(ProjectActivity.this, mProject.getId(), mRef, REQUEST_BRANCH_OR_TAG);
                    }
                    return true;
                case R.id.action_share:
                    if (mProject != null) {
                        IntentUtil.share(mRoot, mProject.getWebUrl());
                    }
                    return true;
                case R.id.action_copy_git_https:
                    if (mProject == null || mProject.getHttpUrlToRepo() == null) {
                        Toast.makeText(ProjectActivity.this, R.string.failed_to_copy_to_clipboard, Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        copyToClipboard(mProject.getHttpUrlToRepo());
                    }
                    return true;
                case R.id.action_copy_git_ssh:
                    if (mProject == null || mProject.getHttpUrlToRepo() == null) {
                        Toast.makeText(ProjectActivity.this, R.string.failed_to_copy_to_clipboard, Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        copyToClipboard(mProject.getSshUrlToRepo());
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
            mRef = Parcels.unwrap(savedInstanceState.getParcelable(STATE_REF));
        }
        mToolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mToolbar.inflateMenu(R.menu.menu_project);
        mToolbar.setOnMenuItemClickListener(mOnMenuItemClickListener);

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
                    mRef = Parcels.unwrap(data.getParcelableExtra(PickBranchOrTagActivity.EXTRA_REF));
                    broadcastLoad();
                }
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_REF, Parcels.wrap(mRef));
        outState.putParcelable(STATE_PROJECT, Parcels.wrap(mProject));
    }

    private void loadProject(String projectId) {
        showProgress();
        App.get().getGitLab().getProject(projectId).enqueue(mProjectCallback);
    }

    private void loadProject(String projectNamespace, String projectName) {
        showProgress();
        App.get().getGitLab().getProject(projectNamespace, projectName).enqueue(mProjectCallback);
    }

    private void showProgress() {
        mProgress.setAlpha(0.0f);
        mProgress.setVisibility(View.VISIBLE);
        mProgress.animate().alpha(1.0f);
    }

    private void broadcastLoad() {
        App.bus().post(new ProjectReloadEvent(mProject, mRef.getRef()));
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + mViewPager.getCurrentItem());
        if (fragment instanceof BaseFragment) {
            if (((BaseFragment) fragment).onBackPressed()) {
                return;
            }
        }

        super.onBackPressed();
    }

    public String getRef() {
        if (mRef == null) {
            return null;
        }
        return mRef.getRef();
    }

    public Project getProject() {
        return mProject;
    }

    private void bindProject(Project project) {
        mProject = project;
        if (mRef == null) {
            mRef = new Ref(Ref.TYPE_BRANCH, mProject.getDefaultBranch());
        }
        mToolbar.setTitle(mProject.getName());
        mToolbar.setSubtitle(mProject.getNamespace().getName());
        setupTabs();
    }

    private void setupTabs() {
        ProjectSectionsPagerAdapter projectSectionsPagerAdapter = new ProjectSectionsPagerAdapter(this, getSupportFragmentManager());
        mViewPager.setAdapter(projectSectionsPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private void copyToClipboard(String url) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        // Creates a new text clip to put on the clipboard
        ClipData clip = ClipData.newPlainText(mProject.getName(), url);
        clipboard.setPrimaryClip(clip);
        Snackbar.make(mRoot, R.string.copied_to_clipboard, Snackbar.LENGTH_SHORT)
                .show();
    }
}

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
import com.commit451.gitlab.model.api.Branch;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.navigation.Navigator;
import com.commit451.gitlab.util.IntentUtil;

import org.parceler.Parcels;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Callback;
import timber.log.Timber;

public class ProjectActivity extends BaseActivity {

    private static final String EXTRA_PROJECT = "extra_project";
    private static final String EXTRA_PROJECT_ID = "extra_project_id";

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
    String mBranchName;

    private final Callback<Project> mProjectCallback = new EasyCallback<Project>() {
        @Override
        public void success(@NonNull Project response) {
            mProject = response;
            setupTabs();
            loadBranches();
        }

        @Override
        public void failure(Throwable t) {
            Timber.e(t, null);
            mProgress.animate()
                    .alpha(0.0f)
                    .withEndAction(new HideRunnable(mProgress));
            Snackbar.make(mRoot, getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                    .show();
        }
    };

    private final Callback<List<Branch>> mBranchesCallback = new EasyCallback<List<Branch>>() {

        @Override
        public void success(@NonNull List<Branch> response) {
            mProgress.animate()
                    .alpha(0.0f)
                    .withEndAction(new HideRunnable(mProgress));

            for (int i = 0; i < response.size(); i++) {
                if (response.get(i).getName().equals(mProject.getDefaultBranch())) {
                    mBranchName = response.get(i).getName();
                }
            }

            if (response.isEmpty()) {
                broadcastLoad();
            }
        }

        @Override
        public void failure(Throwable t) {
            Timber.e(t, null);
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
                        Navigator.navigateToPickBranchOrTag(ProjectActivity.this, mProject.getId(), REQUEST_BRANCH_OR_TAG);
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
        mProject = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_PROJECT));

        mToolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mToolbar.inflateMenu(R.menu.menu_project);
        mToolbar.setOnMenuItemClickListener(mOnMenuItemClickListener);

        if (mProject == null) {
            String projectId = getIntent().getStringExtra(EXTRA_PROJECT_ID);
            loadProject(projectId);
        } else {
            setupTabs();
            loadBranches();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_BRANCH_OR_TAG:
                if (resultCode == RESULT_OK) {
                    mBranchName = data.getStringExtra(PickBranchOrTagActivity.EXTRA_REF);
                    broadcastLoad();
                }
                break;
        }
    }

    private void loadProject(String projectId) {
        mProgress.setAlpha(0.0f);
        mProgress.setVisibility(View.VISIBLE);
        mProgress.animate().alpha(1.0f);
        App.instance().getGitLab().getProject(projectId).enqueue(mProjectCallback);
    }

    private void loadBranches() {
        mProgress.setAlpha(0.0f);
        mProgress.setVisibility(View.VISIBLE);
        mProgress.animate().alpha(1.0f);
        App.instance().getGitLab().getBranches(mProject.getId()).enqueue(mBranchesCallback);
    }

    private void broadcastLoad() {
        App.bus().post(new ProjectReloadEvent(mProject, mBranchName));
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

    public String getBranchName() {
        return mBranchName;
    }

    public Project getProject() {
        return mProject;
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

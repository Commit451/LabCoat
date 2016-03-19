package com.commit451.gitlab.activity;

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
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.commit451.gitlab.LabCoatApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.SectionsPagerAdapter;
import com.commit451.gitlab.adapter.ThemedArrayAdapter;
import com.commit451.gitlab.animation.HideRunnable;
import com.commit451.gitlab.api.EasyCallback;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.event.ProjectReloadEvent;
import com.commit451.gitlab.fragment.BaseFragment;
import com.commit451.gitlab.model.api.Branch;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.util.IntentUtil;

import org.parceler.Parcels;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Callback;
import timber.log.Timber;

public class ProjectActivity extends BaseActivity {

    private static final String EXTRA_PROJECT = "extra_project";
    private static final String EXTRA_PROJECT_ID = "extra_project_id";

    public static Intent newInstance(Context context, Project project) {
        Intent intent = new Intent(context, ProjectActivity.class);
        intent.putExtra(EXTRA_PROJECT, Parcels.wrap(project));
        return intent;
    }

    public static Intent newInstance(Context context, long projectId) {
        Intent intent = new Intent(context, ProjectActivity.class);
        intent.putExtra(EXTRA_PROJECT_ID, projectId);
        return intent;
    }

    @Bind(R.id.root)
    ViewGroup mRoot;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.tabs)
    TabLayout mTabLayout;
    @Bind(R.id.branch_spinner)
    Spinner mBranchSpinner;
    @Bind(R.id.progress)
    View mProgress;
    @Bind(R.id.pager)
    ViewPager mViewPager;

    private final AdapterView.OnItemSelectedListener mSpinnerItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (!(view instanceof TextView)) {
                return;
            }

            mBranchName = ((TextView) view).getText().toString();
            broadcastLoad();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    Project mProject;
    String mBranchName;

    private final Callback<Project> mProjectCallback = new EasyCallback<Project>() {
        @Override
        public void onResponse(@NonNull Project response) {
            mProject = response;
            setupTabs();
            loadBranches();
        }

        @Override
        public void onAllFailure(Throwable t) {
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
        public void onResponse(@NonNull List<Branch> response) {
            mProgress.animate()
                    .alpha(0.0f)
                    .withEndAction(new HideRunnable(mProgress));

            if (response.isEmpty()) {
                mBranchSpinner.setVisibility(View.GONE);
            } else {
                mBranchSpinner.setVisibility(View.VISIBLE);
                mBranchSpinner.setAlpha(0.0f);
                mBranchSpinner.animate().alpha(1.0f);
                // Set up the dropdown list navigation in the action bar.
                mBranchSpinner.setAdapter(new ThemedArrayAdapter<>(ProjectActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, response));
            }
            for (int i = 0; i < response.size(); i++) {
                if (response.get(i).getName().equals(mProject.getDefaultBranch())) {
                    mBranchSpinner.setSelection(i);
                }
            }

            mBranchSpinner.setOnItemSelectedListener(mSpinnerItemSelectedListener);

            if (response.isEmpty()) {
                broadcastLoad();
            }
        }

        @Override
        public void onAllFailure(Throwable t) {
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
                case R.id.action_share:
                    IntentUtil.share(mRoot, mProject.getWebUrl());
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
        mToolbar.inflateMenu(R.menu.menu_repository);
        mToolbar.setOnMenuItemClickListener(mOnMenuItemClickListener);

        if (mProject == null) {
            long projectId = getIntent().getLongExtra(EXTRA_PROJECT_ID, -1);
            loadProject(projectId);
        } else {
            setupTabs();
            loadBranches();
        }
    }

    private void loadProject(long projectId) {
        mProgress.setAlpha(0.0f);
        mProgress.setVisibility(View.VISIBLE);
        mProgress.animate().alpha(1.0f);
        GitLabClient.instance().getProject(projectId).enqueue(mProjectCallback);
    }

    private void loadBranches() {
        mProgress.setAlpha(0.0f);
        mProgress.setVisibility(View.VISIBLE);
        mProgress.animate().alpha(1.0f);
        GitLabClient.instance().getBranches(mProject.getId()).enqueue(mBranchesCallback);
    }

    private void broadcastLoad() {
        LabCoatApp.bus().post(new ProjectReloadEvent(mProject, mBranchName));
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
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());

        mViewPager.setAdapter(sectionsPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }
}

package com.commit451.gitlab.widget;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;

import com.commit451.gitlab.R;
import com.commit451.gitlab.activity.BaseActivity;
import com.commit451.gitlab.adapter.ProjectsPagerAdapter;
import com.commit451.gitlab.api.GitLab;
import com.commit451.gitlab.api.GitLabFactory;
import com.commit451.gitlab.api.OkHttpClientFactory;
import com.commit451.gitlab.fragment.ProjectsFragment;
import com.commit451.gitlab.model.Account;
import com.commit451.gitlab.model.api.Project;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * You chose your account, now choose your project!
 */
public class ProjectFeedWidgetConfigureProjectActivity extends BaseActivity implements ProjectsFragment.Listener {

    public static final String EXTRA_PROJECT = "project";
    private static final String EXTRA_ACCOUNT = "account";

    public static Intent newIntent(Context context, Account account) {
        Intent intent = new Intent(context, ProjectFeedWidgetConfigureProjectActivity.class);
        intent.putExtra(EXTRA_ACCOUNT, Parcels.wrap(account));
        return intent;
    }

    @BindView(R.id.tabs)
    TabLayout mTabLayout;
    @BindView(R.id.pager)
    ViewPager mViewPager;

    GitLab mGitLab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_feed_widget_configure);
        ButterKnife.bind(this);

        Account account = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_ACCOUNT));
        mGitLab = GitLabFactory.create(account, OkHttpClientFactory.create(account, false).build());

        mViewPager.setAdapter(new ProjectsPagerAdapter(this, getSupportFragmentManager()));
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public GitLab getGitLab() {
        return mGitLab;
    }

    @Override
    public void onProjectClicked(Project project) {
        Intent data = new Intent();
        data.putExtra(EXTRA_PROJECT, Parcels.wrap(project));
        setResult(RESULT_OK, data);
        finish();
    }
}

package com.commit451.gitlab.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.commit451.gitlab.LabCoatApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.ProjectsPagerAdapter;
import com.commit451.gitlab.event.CloseDrawerEvent;
import com.commit451.gitlab.navigation.NavigationManager;
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Shows the projects
 */
public class ProjectsActivity extends BaseActivity implements ATEActivityThemeCustomizer {

    @Override
    public int getActivityTheme() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", true) ?
                R.style.Activity_Projects : R.style.ActivityLight_Projects;
    }

    public static Intent newInstance(Context context) {
        Intent intent = new Intent(context, ProjectsActivity.class);
        return intent;
    }

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.tabs) TabLayout mTabLayout;
    @Bind(R.id.pager) ViewPager mViewPager;
    @Bind(R.id.navigation_view) NavigationView mNavigationView;
    @Bind(R.id.drawer_layout) DrawerLayout mDrawerLayout;

    EventReceiver mEventReceiver;

    private final Toolbar.OnMenuItemClickListener mOnMenuItemClickListener = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_search:
                    NavigationManager.navigateToSearch(ProjectsActivity.this);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);
        ButterKnife.bind(this);
        mEventReceiver = new EventReceiver();
        LabCoatApp.bus().register(mEventReceiver);

        mToolbar.setTitle(R.string.projects);
        mToolbar.setNavigationIcon(R.drawable.ic_menu_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });
        mToolbar.inflateMenu(R.menu.menu_search);
        mToolbar.setOnMenuItemClickListener(mOnMenuItemClickListener);
        mViewPager.setAdapter(new ProjectsPagerAdapter(this, getSupportFragmentManager()));
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LabCoatApp.bus().unregister(mEventReceiver);
    }

    private class EventReceiver {

        @Subscribe
        public void onCloseDrawerEvent(CloseDrawerEvent event) {
            mDrawerLayout.closeDrawers();
        }
    }
}

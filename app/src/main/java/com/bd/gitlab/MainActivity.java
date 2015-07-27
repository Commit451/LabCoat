package com.bd.gitlab;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import com.bd.gitlab.adapter.DrawerAdapter;
import com.bd.gitlab.fragments.CommitsFragment;
import com.bd.gitlab.fragments.FilesFragment;
import com.bd.gitlab.fragments.IssuesFragment;
import com.bd.gitlab.fragments.UsersFragment;
import com.bd.gitlab.model.Branch;
import com.bd.gitlab.model.Group;
import com.bd.gitlab.model.Project;
import com.bd.gitlab.model.User;
import com.bd.gitlab.tools.Repository;
import com.bd.gitlab.tools.RetrofitHelper;
import com.bd.gitlab.views.DrawableClickListener;
import com.bd.gitlab.views.FilterEditText;

import net.danlew.android.joda.JodaTimeAndroid;
import net.danlew.android.joda.ResourceZoneInfoProvider;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends FragmentActivity implements ActionBar.OnNavigationListener, OnItemClickListener {
	
	@Bind(R.id.drawer_layout) DrawerLayout drawerLayout;
	@Bind(R.id.left_drawer) LinearLayout drawerLeft;
	@Bind(R.id.left_drawer_list) ListView drawerList;
	@Bind(R.id.pager) ViewPager viewPager;
	@Bind(R.id.filter_project) FilterEditText filterProjectEdit;

	private ActionBar actionBar;
	private ActionBarDrawerToggle drawerToggle;

    private boolean rotationLocked = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);
		
		actionBar = getActionBar();
		actionBar.setIcon(getResources().getDrawable(R.drawable.ic_actionbar));
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		
		drawerList.setOnItemClickListener(this);
		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.setDrawerListener(drawerToggle);

        filterProjectEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                Repository.drawerAdapter.getFilter().filter(filterProjectEdit.getText().toString());
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        filterProjectEdit.setDrawableClickListener(new DrawableClickListener() {
            @Override
            public void onClick(DrawablePosition target) {
                filterProjectEdit.setText("");
            }
        });
		
		// Workaround that forces the overflow menu
        try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			if(menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		
		Repository.init(this);
		
		if(!Repository.isLoggedIn())
			startActivity(new Intent(this, LoginActivity.class));
		else
			connect();

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
		Repository.displayWidth = size.x;
		
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		
		// Set up the ViewPager with the sections adapter.
		viewPager.setAdapter(sectionsPagerAdapter);
		viewPager.setOffscreenPageLimit(3);
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		drawerToggle.syncState();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Crouton.cancelAllCroutons();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case android.R.id.home:
				if(drawerLayout.isDrawerOpen(drawerLeft))
					drawerLayout.closeDrawer(drawerLeft);
				else
					drawerLayout.openDrawer(drawerLeft);
				return true;
			case R.id.action_logout:
				Repository.setLoggedIn(false);
				startActivity(new Intent(this, LoginActivity.class));
				return true;
            case R.id.action_lock_orientation:
                item.setChecked(!item.isChecked());
                rotationLocked = item.isChecked();

                if(rotationLocked)
                    setRequestedOrientation(Repository.getScreenOrientation(this));
                else
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		Repository.selectedBranch = Repository.branches.get(itemPosition);
        Repository.setLastBranch(Repository.selectedBranch.getName());
		loadData();
		return true;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if(Repository.selectedProject == null || !Repository.selectedProject.equals(Repository.projects.get(position))) {
			Repository.selectedProject = Repository.projects.get(position);
			Repository.setLastProject(Repository.selectedProject.toString());
			Repository.issueAdapter = null;
			Repository.userAdapter = null;
			Repository.drawerAdapter.notifyDataSetChanged();
			
			Repository.getService().getBranches(Repository.selectedProject.getId(), branchesCallback);
		}
		
		if(drawerLayout.isDrawerOpen(drawerLeft)) {
			drawerLayout.closeDrawer(drawerLeft);
		}
	}

	public void hideSoftKeyboard() {
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
	}
	
	@Override
	public void onBackPressed() {
		boolean handled = false;
		
		switch(viewPager.getCurrentItem()) {
			case 0:
				CommitsFragment commitsFragment = (CommitsFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":0");
				handled = commitsFragment.onBackPressed();
				break;
			case 1:
				IssuesFragment issuesFragment = (IssuesFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":1");
				handled = issuesFragment.onBackPressed();
				break;
			case 2:
				FilesFragment filesFragment = (FilesFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":2");
				handled = filesFragment.onBackPressed();
				break;
			case 3:
				UsersFragment usersFragment = (UsersFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":3");
				handled = usersFragment.onBackPressed();
				break;
		}
		
		if(!handled)
			finish();
	}
	
	private void loadData() {
		if(Repository.selectedProject == null)
			return;
		
		CommitsFragment commitsFragment = (CommitsFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":0");
		commitsFragment.loadData();
		
		IssuesFragment issuesFragment = (IssuesFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":1");
		issuesFragment.loadData();
		
		FilesFragment filesFragment = (FilesFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":2");
		filesFragment.loadData();
		
		UsersFragment usersFragment = (UsersFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":3");
		usersFragment.loadData();
	}
	
	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {
		
		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}
		
		@Override
		public Fragment getItem(int position) {
			Fragment fragment = null;
			
			switch(position) {
				case 0:
					fragment = new CommitsFragment();
					break;
				case 1:
					fragment = new IssuesFragment();
					break;
				case 2:
					fragment = new FilesFragment();
					break;
				case 3:
					fragment = new UsersFragment();
					break;
			}

			return fragment;
		}
		
		@Override
		public int getCount() {
			return 4;
		}
		
		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch(position) {
				case 0:
					return getString(R.string.title_section1).toUpperCase(l);
				case 1:
					return getString(R.string.title_section2).toUpperCase(l);
				case 2:
					return getString(R.string.title_section3).toUpperCase(l);
				case 3:
					return getString(R.string.title_section4).toUpperCase(l);
			}
			return null;
		}
	}
	
	/* --- CONNECT --- */
	
	private ProgressDialog pd;
	
	private void connect() {
		pd = ProgressDialog.show(MainActivity.this, "", getResources().getString(R.string.main_progress_dialog), true);
		Repository.getService().getGroups(groupsCallback);
	}
	
	private Callback<List<Group>> groupsCallback = new Callback<List<Group>>() {
		
		@Override
		public void success(List<Group> groups, Response resp) {
			Repository.groups = new ArrayList<Group>(groups);
			
			Repository.getService().getUsers(usersCallback);
		}
		
		@Override
		public void failure(RetrofitError e) {
			RetrofitHelper.printDebugInfo(MainActivity.this, e);
			
			Repository.getService().getUsers(usersCallback);
		}
	};
	
	private Callback<List<User>> usersCallback = new Callback<List<User>>() {
		
		@Override
		public void success(List<User> users, Response resp) {
			Repository.users = new ArrayList<User>(users);
			
			Repository.getService().getProjects(projectsCallback);
		}
		
		@Override
		public void failure(RetrofitError e) {
			RetrofitHelper.printDebugInfo(MainActivity.this, e);
			
			if(pd != null && pd.isShowing())
				pd.cancel();
			
			Crouton.makeText(MainActivity.this, R.string.connection_error, Style.ALERT).show();
		}
	};
	
	private Callback<List<Project>> projectsCallback = new Callback<List<Project>>() {
		
		@Override
		public void success(List<Project> projects, Response resp) {
			Repository.projects = new ArrayList<Project>(projects);

			if(Repository.projects.size() != 0) {
				if(Repository.getLastProject().length() == 0)
					Repository.selectedProject = Repository.projects.get(0);
				else if(Repository.projects.size() > 0) {
					String lastProject = Repository.getLastProject();

					for(Project p : Repository.projects) {
						if(p.toString().equals(lastProject))
							Repository.selectedProject = p;
					}

					if(Repository.selectedProject == null)
						Repository.selectedProject = Repository.projects.get(0);
				}
			}
			
			Repository.drawerAdapter = new DrawerAdapter(MainActivity.this, Repository.projects);
			drawerList.setAdapter(Repository.drawerAdapter);
			
			if(Repository.selectedProject != null)
				Repository.getService().getBranches(Repository.selectedProject.getId(), branchesCallback);
            else
                if(pd != null && pd.isShowing())
                    pd.cancel();
		}
		
		@Override
		public void failure(RetrofitError e) {
			RetrofitHelper.printDebugInfo(MainActivity.this, e);

            if(pd != null && pd.isShowing())
                pd.cancel();
			
			Crouton.makeText(MainActivity.this, R.string.connection_error, Style.ALERT).show();
		}
	};
	
	private Callback<List<Branch>> branchesCallback = new Callback<List<Branch>>() {
		
		@Override
		public void success(List<Branch> branches, Response resp) {
			if(pd != null && pd.isShowing())
				pd.cancel();
			
			Repository.branches = new ArrayList<Branch>(branches);
			Branch[] spinnerData = new Branch[Repository.branches.size()];
			int selectedBranchIndex = -1;
			
			for(int i = 0; i < Repository.branches.size(); i++)
			{
				spinnerData[i] = Repository.branches.get(i);

                if(Repository.getLastBranch().equals(spinnerData[i].getName()))
                    selectedBranchIndex = i;
                else if(selectedBranchIndex == -1 && Repository.selectedProject != null && spinnerData[i].getName().equals(Repository.selectedProject.getDefaultBranch()))
                    selectedBranchIndex = i;
			}
			
			actionBar.setDisplayShowTitleEnabled(false);
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

            Context context = actionBar.getThemedContext();
            if(context == null) context = MainActivity.this;
			// Set up the dropdown list navigation in the action bar.
			actionBar.setListNavigationCallbacks(new ArrayAdapter<Branch>(context, android.R.layout.simple_list_item_1, android.R.id.text1, spinnerData), MainActivity.this);
			if(selectedBranchIndex >= 0)
				actionBar.setSelectedNavigationItem(selectedBranchIndex);
			
			if(Repository.branches.size() == 0) {
				Repository.selectedBranch = null;
				loadData();
			}
		}
		
		@Override
		public void failure(RetrofitError e) {
			if(pd != null && pd.isShowing())
				pd.cancel();

            if(e.getResponse().getStatus() == 500) {
                Repository.selectedBranch = null;
                loadData();

                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

                return;
            }

            RetrofitHelper.printDebugInfo(MainActivity.this, e);
			Crouton.makeText(MainActivity.this, R.string.connection_error, Style.ALERT).show();
		}
	};
}

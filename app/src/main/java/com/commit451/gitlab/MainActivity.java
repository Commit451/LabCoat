package com.commit451.gitlab;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

import com.commit451.gitlab.adapter.DrawerAdapter;
import com.commit451.gitlab.fragments.CommitsFragment;
import com.commit451.gitlab.fragments.FilesFragment;
import com.commit451.gitlab.fragments.IssuesFragment;
import com.commit451.gitlab.fragments.UsersFragment;
import com.commit451.gitlab.model.Branch;
import com.commit451.gitlab.model.Group;
import com.commit451.gitlab.model.Project;
import com.commit451.gitlab.model.User;
import com.commit451.gitlab.tools.Repository;
import com.commit451.gitlab.tools.RetrofitHelper;

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

public class MainActivity extends BaseActivity implements ActionBar.OnNavigationListener, OnItemClickListener {

	@Bind(R.id.toolbar) Toolbar toolbar;
	@Bind(R.id.tabs) TabLayout tabs;
	@Bind(R.id.branch_spinner) Spinner branchSpinner;
	@Bind(R.id.drawer_layout) DrawerLayout drawerLayout;
	@Bind(R.id.left_drawer) LinearLayout drawerLeft;
	@Bind(R.id.left_drawer_list) ListView drawerList;
	@Bind(R.id.pager) ViewPager viewPager;

	private final AdapterView.OnItemSelectedListener spinnerItemSelectedListener = new AdapterView.OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			Repository.selectedBranch = Repository.branches.get(position);
			Repository.setLastBranch(Repository.selectedBranch.getName());
			loadData();
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) { }
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);

		toolbar.setNavigationIcon(R.drawable.ic_menu);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				drawerLayout.openDrawer(GravityCompat.START);
			}
		});
		toolbar.inflateMenu(R.menu.main);
		toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch(item.getItemId()) {
					case R.id.action_logout:
						Repository.setLoggedIn(false);
						startActivity(new Intent(MainActivity.this, LoginActivity.class));
						return true;
					default:
						return false;
				}
			}
		});
		
		drawerList.setOnItemClickListener(this);
		
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
		tabs.setupWithViewPager(viewPager);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Crouton.cancelAllCroutons();
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
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
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
			Repository.projects = new ArrayList<>(projects);

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
			
			Repository.branches = new ArrayList<>(branches);
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

			// Set up the dropdown list navigation in the action bar.
			branchSpinner.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, spinnerData));
			if (selectedBranchIndex >= 0) {
				branchSpinner.setSelection(selectedBranchIndex);
			}
			branchSpinner.setOnItemSelectedListener(spinnerItemSelectedListener);
			
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
                return;
            }

            RetrofitHelper.printDebugInfo(MainActivity.this, e);
			Crouton.makeText(MainActivity.this, R.string.connection_error, Style.ALERT).show();
		}
	};
}

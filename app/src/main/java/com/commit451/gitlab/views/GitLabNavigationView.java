package com.commit451.gitlab.views;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.design.widget.NavigationView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.gitlab.GitLabApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.activities.GroupsActivity;
import com.commit451.gitlab.activities.ProjectsActivity;
import com.commit451.gitlab.adapter.UsersAdapter;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.data.Prefs;
import com.commit451.gitlab.events.CloseDrawerEvent;
import com.commit451.gitlab.model.Account;
import com.commit451.gitlab.model.User;
import com.commit451.gitlab.tools.ImageUtil;
import com.commit451.gitlab.tools.NavigationManager;
import com.commit451.gitlab.viewHolders.UserViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

/**
 * Our very own navigation view
 * Created by Jawn on 7/28/2015.
 */
public class GitLabNavigationView extends NavigationView {

    @Bind(R.id.profile_image) ImageView profileImage;
    @Bind(R.id.profile_user) TextView userName;
    @Bind(R.id.profile_email) TextView userEmail;
    @Bind(R.id.drawer_header) FrameLayout header;
    @Bind(R.id.account_list) RecyclerView mAccountList;
    UsersAdapter mAccountAdapter;

    private final OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.nav_projects:
                    if (getContext() instanceof ProjectsActivity) {

                    } else {
                        NavigationManager.navigateToProjects((Activity) getContext());
                        ((Activity) getContext()).finish();
                        ((Activity)getContext()).overridePendingTransition(R.anim.fade_in, R.anim.do_nothing);
                    }
                    GitLabApp.bus().post(new CloseDrawerEvent());
                    return true;
                case R.id.nav_groups:
                    if (getContext() instanceof GroupsActivity) {

                    } else {
                        NavigationManager.navigateToGroups((Activity) getContext());
                        ((Activity) getContext()).finish();
                        ((Activity)getContext()).overridePendingTransition(R.anim.fade_in, R.anim.do_nothing);
                    }
                    GitLabApp.bus().post(new CloseDrawerEvent());
                    return true;
                case R.id.nav_about:
                    GitLabApp.bus().post(new CloseDrawerEvent());
                    NavigationManager.navigateToAbout((Activity) getContext());
                    return true;
            }
            return false;
        }
    };

    @OnClick(R.id.drawer_header)
    void onHeaderClick() {
        mAccountList.setVisibility(View.VISIBLE);
    }

    private final Callback<User> userCallback = new Callback<User>() {

        @Override
        public void onResponse(Response<User> response, Retrofit retrofit) {
            if (!response.isSuccess()) {
                return;
            }
            bindUser(response.body());
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t, null);
        }
    };

    public GitLabNavigationView(Context context) {
        super(context);
        init();
    }

    public GitLabNavigationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GitLabNavigationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        inflateMenu(R.menu.navigation);
        View header = inflateHeaderView(R.layout.header_nav_drawer);
        ButterKnife.bind(this, header);
        mAccountList.setLayoutManager(new LinearLayoutManager(getContext()));
        mAccountAdapter = new UsersAdapter(new UsersAdapter.Listener() {
            @Override
            public void onUserClicked(User user, UserViewHolder userViewHolder) {

            }
        });
        mAccountList.setAdapter(mAccountAdapter);
        setSelectedNavigationItem();
        setAccounts();
        loadCurrentUser();
    }

    private void setSelectedNavigationItem() {
        for (int i=0; i<getMenu().size(); i++) {
            MenuItem menuItem = getMenu().getItem(i);
            if (getContext() instanceof ProjectsActivity && menuItem.getItemId() == R.id.nav_projects) {
                menuItem.setChecked(true);
                return;
            }
            if (getContext() instanceof GroupsActivity && menuItem.getItemId() == R.id.nav_groups) {
                menuItem.setChecked(true);
                return;
            }
        }
        throw new IllegalStateException("You need to set a selected nav item for this activity");
    }

    private void setAccounts() {
        List<Account> accounts = Prefs.getAccounts(getContext());
        ArrayList<User> users = new ArrayList<>();
        for (Account account : accounts) {
            users.add(account.getUser());
        }
        //TODO make this actually an account adapter
        mAccountAdapter.setData(users);
    }

    private void loadCurrentUser() {
        GitLabClient.instance().getUser().enqueue(userCallback);
    }

    private void bindUser(User user) {
        if (getContext() == null) {
            return;
        }
        if (user.getUsername() != null) {
            userName.setText(user.getUsername());
        }
        if (user.getEmail() != null) {
            userEmail.setText(user.getEmail());
        }
        Uri url = ImageUtil.getAvatarUrl(user, getResources().getDimensionPixelSize(R.dimen.larger_image_size));
        GitLabClient.getPicasso()
                .load(url)
                .into(profileImage);
    }
}

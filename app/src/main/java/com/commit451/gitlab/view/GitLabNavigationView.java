package com.commit451.gitlab.view;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.easel.Easel;
import com.commit451.gitlab.LabCoatApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.activity.GroupsActivity;
import com.commit451.gitlab.activity.ProjectsActivity;
import com.commit451.gitlab.adapter.AccountsAdapter;
import com.commit451.gitlab.api.EasyCallback;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.data.Prefs;
import com.commit451.gitlab.event.CloseDrawerEvent;
import com.commit451.gitlab.event.LoginEvent;
import com.commit451.gitlab.event.ReloadDataEvent;
import com.commit451.gitlab.model.Account;
import com.commit451.gitlab.model.api.UserFull;
import com.commit451.gitlab.transformation.CircleTransformation;
import com.commit451.gitlab.util.ImageUtil;
import com.commit451.gitlab.util.NavigationManager;
import com.squareup.otto.Subscribe;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import timber.log.Timber;

/**
 * Our very own navigation view
 * Created by Jawn on 7/28/2015.
 */
public class GitLabNavigationView extends NavigationView {

    @Bind(R.id.profile_image) ImageView mProfileImage;
    @Bind(R.id.profile_user) TextView mUserName;
    @Bind(R.id.profile_email) TextView mUserEmail;
    @Bind(R.id.arrow) View mArrow;

    RecyclerView mAccountList;
    AccountsAdapter mAccountAdapter;
    EventReceiver mEventReceiver;

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
                    LabCoatApp.bus().post(new CloseDrawerEvent());
                    return true;
                case R.id.nav_groups:
                    if (getContext() instanceof GroupsActivity) {

                    } else {
                        NavigationManager.navigateToGroups((Activity) getContext());
                        ((Activity) getContext()).finish();
                        ((Activity)getContext()).overridePendingTransition(R.anim.fade_in, R.anim.do_nothing);
                    }
                    LabCoatApp.bus().post(new CloseDrawerEvent());
                    return true;
                case R.id.nav_settings:
                    LabCoatApp.bus().post(new CloseDrawerEvent());
                    NavigationManager.navigateToSettings((Activity) getContext());
                    return true;
                case R.id.nav_about:
                    LabCoatApp.bus().post(new CloseDrawerEvent());
                    NavigationManager.navigateToAbout((Activity) getContext());
                    return true;
            }
            return false;
        }
    };

    private final AccountsAdapter.Listener mAccountsAdapterListener = new AccountsAdapter.Listener() {
        @Override
        public void onAccountClicked(Account account) {
            switchToAccount(account);
        }

        @Override
        public void onAddAccountClicked() {
            NavigationManager.navigateToLogin((Activity) getContext(), true);
            toggleAccounts();
            LabCoatApp.bus().post(new CloseDrawerEvent());
        }

        @Override
        public void onAccountLogoutClicked(Account account) {
            Prefs.removeAccount(getContext(), account);
            List<Account> accounts = Account.getAccounts(getContext());

            if (accounts.isEmpty()) {
                NavigationManager.navigateToLogin((Activity) getContext());
                ((Activity) getContext()).finish();
            } else {
                if (account.equals(GitLabClient.getAccount())) {
                    switchToAccount(accounts.get(0));
                }
            }
        }
    };

    private final Callback<UserFull> mUserCallback = new EasyCallback<UserFull>() {

        @Override
        public void onResponse(@NonNull UserFull response) {
            //Store the newly retrieved user to the account so that it stays up to date
            // in local storage
            Account account = GitLabClient.getAccount();
            account.setUser(response);
            Prefs.updateAccount(getContext(), account);
            bindUser(response);
        }

        @Override
        public void onAllFailure(Throwable t) {
            Timber.e(t, null);
        }
    };

    @OnClick(R.id.profile_image)
    public void onUserImageClick(ImageView imageView) {
        NavigationManager.navigateToUser((Activity) getContext(), imageView, GitLabClient.getAccount().getUser());
    }

    @OnClick(R.id.drawer_header)
    public void onHeaderClick() {
        toggleAccounts();
    }

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
        mEventReceiver = new EventReceiver();
        LabCoatApp.bus().register(mEventReceiver);
        int colorPrimary = Easel.getThemeAttrColor(getContext(), R.attr.colorPrimary);

        setNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        inflateMenu(R.menu.navigation);
        setBackgroundColor(colorPrimary);
        View header = inflateHeaderView(R.layout.header_nav_drawer);
        ButterKnife.bind(this, header);

        mAccountList = new RecyclerView(getContext());
        mAccountList.setLayoutManager(new LinearLayoutManager(getContext()));
        addView(mAccountList);
        LayoutParams params = (FrameLayout.LayoutParams) mAccountList.getLayoutParams();
        params.setMargins(0, getResources().getDimensionPixelSize(R.dimen.account_header_height), 0, 0);
        mAccountList.setBackgroundColor(colorPrimary);
        mAccountList.setVisibility(View.GONE);
        mAccountAdapter = new AccountsAdapter(getContext(), mAccountsAdapterListener);
        mAccountList.setAdapter(mAccountAdapter);
        setSelectedNavigationItem();
        setAccounts();
        loadCurrentUser();
    }

    @Override
    protected void onDetachedFromWindow() {
        LabCoatApp.bus().unregister(mEventReceiver);
        super.onDetachedFromWindow();
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
        Timber.d("Got %s accounts", accounts.size());
        Collections.sort(accounts);
        Collections.reverse(accounts);
        mAccountAdapter.setAccounts(accounts);
    }

    private void loadCurrentUser() {
        GitLabClient.instance().getThisUser().enqueue(mUserCallback);
    }

    private void bindUser(UserFull user) {
        if (getContext() == null) {
            return;
        }
        if (user.getUsername() != null) {
            mUserName.setText(user.getUsername());
        }
        if (user.getEmail() != null) {
            mUserEmail.setText(user.getEmail());
        }
        Uri url = ImageUtil.getAvatarUrl(user, getResources().getDimensionPixelSize(R.dimen.larger_image_size));
        GitLabClient.getPicasso()
                .load(url)
                .transform(new CircleTransformation())
                .into(mProfileImage);
    }

    /**
     * Toggle the visibility of accounts. Meaning hide it if it is showing, show it if it is hidden
     */
    private void toggleAccounts() {
        if (mAccountList.getVisibility() == View.GONE) {
            mAccountList.setVisibility(View.VISIBLE);
            mAccountList.setAlpha(0.0f);
            mAccountList.animate().alpha(1.0f);
            mArrow.animate().rotation(180.0f);
        } else {
            mAccountList.animate().alpha(0.0f).withEndAction(new Runnable() {
                @Override
                public void run() {
                    if (mAccountList != null) {
                        mAccountList.setVisibility(View.GONE);
                    }
                }
            });
            mArrow.animate().rotation(0.0f);
        }
    }

    private void switchToAccount(Account account) {
        Timber.d("Switching to account: %s", account);
        account.setLastUsed(new Date());
        GitLabClient.setAccount(account);
        Prefs.updateAccount(getContext(), account);
        bindUser(account.getUser());
        toggleAccounts();
        LabCoatApp.bus().post(new ReloadDataEvent());
        LabCoatApp.bus().post(new CloseDrawerEvent());
        // Trigger a reload in the adapter so that we will place the accounts
        // in the correct order from most recently used
        mAccountAdapter.notifyDataSetChanged();
        loadCurrentUser();
    }

    private class EventReceiver {

        @Subscribe
        public void onUserLoggedIn(LoginEvent event) {
            if (mAccountAdapter != null) {
                mAccountAdapter.addAccount(event.account);
                mAccountAdapter.notifyDataSetChanged();
                loadCurrentUser();
            }
        }
    }
}

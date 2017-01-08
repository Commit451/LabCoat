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
import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.activity.ActivityActivity;
import com.commit451.gitlab.activity.GroupsActivity;
import com.commit451.gitlab.activity.ProjectsActivity;
import com.commit451.gitlab.activity.TodosActivity;
import com.commit451.gitlab.adapter.AccountAdapter;
import com.commit451.gitlab.event.CloseDrawerEvent;
import com.commit451.gitlab.event.LoginEvent;
import com.commit451.gitlab.event.ReloadDataEvent;
import com.commit451.gitlab.model.Account;
import com.commit451.gitlab.model.api.UserFull;
import com.commit451.gitlab.navigation.Navigator;
import com.commit451.gitlab.rx.CustomResponseSingleObserver;
import com.commit451.gitlab.transformation.CircleTransformation;
import com.commit451.gitlab.util.ImageUtil;

import org.greenrobot.eventbus.Subscribe;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Our very own navigation view
 */
public class LabCoatNavigationView extends NavigationView {

    @BindView(R.id.profile_image)
    ImageView imageProfile;
    @BindView(R.id.profile_user)
    TextView textUserName;
    @BindView(R.id.profile_email)
    TextView textEmail;
    @BindView(R.id.arrow)
    View iconArrow;

    RecyclerView listAccounts;
    AccountAdapter adapterAccounts;

    private final OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.nav_projects:
                    if (!(getContext() instanceof ProjectsActivity)) {
                        Navigator.navigateToProjects((Activity) getContext());
                        ((Activity) getContext()).finish();
                        ((Activity) getContext()).overridePendingTransition(R.anim.fade_in, R.anim.do_nothing);
                    }
                    App.bus().post(new CloseDrawerEvent());
                    return true;
                case R.id.nav_groups:
                    if (!(getContext() instanceof GroupsActivity)) {
                        Navigator.navigateToGroups((Activity) getContext());
                        ((Activity) getContext()).finish();
                        ((Activity) getContext()).overridePendingTransition(R.anim.fade_in, R.anim.do_nothing);
                    }
                    App.bus().post(new CloseDrawerEvent());
                    return true;
                case R.id.nav_activity:
                    if (!(getContext() instanceof ActivityActivity)) {
                        Navigator.navigateToActivity((Activity) getContext());
                        ((Activity) getContext()).finish();
                        ((Activity) getContext()).overridePendingTransition(R.anim.fade_in, R.anim.do_nothing);
                    }
                    App.bus().post(new CloseDrawerEvent());
                    return true;
                case R.id.nav_todos:
                    if (!(getContext() instanceof TodosActivity)) {
                        Navigator.navigateToTodos((Activity) getContext());
                        ((Activity) getContext()).finish();
                        ((Activity) getContext()).overridePendingTransition(R.anim.fade_in, R.anim.do_nothing);
                    }
                    App.bus().post(new CloseDrawerEvent());
                    return true;
                case R.id.nav_about:
                    App.bus().post(new CloseDrawerEvent());
                    Navigator.navigateToAbout((Activity) getContext());
                    return true;
                case R.id.nav_settings:
                    App.bus().post(new CloseDrawerEvent());
                    Navigator.navigateToSettings((Activity) getContext());
                    return true;
            }
            return false;
        }
    };

    private final AccountAdapter.Listener mAccountsAdapterListener = new AccountAdapter.Listener() {
        @Override
        public void onAccountClicked(Account account) {
            switchToAccount(account);
        }

        @Override
        public void onAddAccountClicked() {
            Navigator.navigateToLogin((Activity) getContext(), true);
            toggleAccounts();
            App.bus().post(new CloseDrawerEvent());
        }

        @Override
        public void onAccountLogoutClicked(Account account) {
            App.get().getPrefs().removeAccount(account);
            List<Account> accounts = Account.getAccounts();

            if (accounts.isEmpty()) {
                Navigator.navigateToLogin((Activity) getContext());
                ((Activity) getContext()).finish();
            } else {
                if (account.equals(App.get().getAccount())) {
                    switchToAccount(accounts.get(0));
                }
            }
        }
    };

    @OnClick(R.id.profile_image)
    public void onUserImageClick(ImageView imageView) {
        Navigator.navigateToUser((Activity) getContext(), imageView, App.get().getAccount().getUser());
    }

    @OnClick(R.id.drawer_header)
    public void onHeaderClick() {
        toggleAccounts();
    }

    public LabCoatNavigationView(Context context) {
        super(context);
        init();
    }

    public LabCoatNavigationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LabCoatNavigationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        App.bus().register(this);
        int colorPrimary = Easel.getThemeAttrColor(getContext(), R.attr.colorPrimary);

        setNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        inflateMenu(R.menu.navigation);
        setBackgroundColor(colorPrimary);
        View header = inflateHeaderView(R.layout.header_nav_drawer);
        ButterKnife.bind(this, header);

        listAccounts = new RecyclerView(getContext());
        listAccounts.setLayoutManager(new LinearLayoutManager(getContext()));
        addView(listAccounts);
        LayoutParams params = (FrameLayout.LayoutParams) listAccounts.getLayoutParams();
        params.setMargins(0, getResources().getDimensionPixelSize(R.dimen.account_header_height), 0, 0);
        listAccounts.setBackgroundColor(colorPrimary);
        listAccounts.setVisibility(View.GONE);
        adapterAccounts = new AccountAdapter(getContext(), mAccountsAdapterListener);
        listAccounts.setAdapter(adapterAccounts);
        setSelectedNavigationItem();
        setAccounts();
        loadCurrentUser();
    }

    @Override
    protected void onDetachedFromWindow() {
        App.bus().unregister(this);
        super.onDetachedFromWindow();
    }

    private void setSelectedNavigationItem() {
        for (int i = 0; i < getMenu().size(); i++) {
            MenuItem menuItem = getMenu().getItem(i);
            if (getContext() instanceof ProjectsActivity && menuItem.getItemId() == R.id.nav_projects) {
                menuItem.setChecked(true);
                return;
            }
            if (getContext() instanceof GroupsActivity && menuItem.getItemId() == R.id.nav_groups) {
                menuItem.setChecked(true);
                return;
            }
            if (getContext() instanceof ActivityActivity && menuItem.getItemId() == R.id.nav_activity) {
                menuItem.setChecked(true);
                return;
            }
            if (getContext() instanceof TodosActivity && menuItem.getItemId() == R.id.nav_todos) {
                menuItem.setChecked(true);
                return;
            }
        }
        throw new IllegalStateException("You need to set a selected nav item for this activity");
    }

    private void setAccounts() {
        List<Account> accounts = App.get().getPrefs().getAccounts();
        Timber.d("Got %s accounts", accounts.size());
        Collections.sort(accounts);
        Collections.reverse(accounts);
        adapterAccounts.setAccounts(accounts);
    }

    private void loadCurrentUser() {
        App.get().getGitLab().getThisUser()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomResponseSingleObserver<UserFull>() {

                    @Override
                    public void error(@NonNull Throwable e) {
                        Timber.e(e);
                    }

                    @Override
                    public void responseSuccess(@NonNull UserFull userFull) {
                        //Store the newly retrieved user to the account so that it stays up to date
                        // in local storage
                        Account account = App.get().getAccount();
                        account.setUser(userFull);
                        App.get().getPrefs().updateAccount(account);
                        bindUser(userFull);
                    }
                });
    }

    private void bindUser(UserFull user) {
        if (getContext() == null) {
            return;
        }
        if (user.getUsername() != null) {
            textUserName.setText(user.getUsername());
        }
        if (user.getEmail() != null) {
            textEmail.setText(user.getEmail());
        }
        Uri url = ImageUtil.getAvatarUrl(user, getResources().getDimensionPixelSize(R.dimen.larger_image_size));
        App.get().getPicasso()
                .load(url)
                .transform(new CircleTransformation())
                .into(imageProfile);
    }

    /**
     * Toggle the visibility of accounts. Meaning hide it if it is showing, show it if it is hidden
     */
    private void toggleAccounts() {
        if (listAccounts.getVisibility() == View.GONE) {
            listAccounts.setVisibility(View.VISIBLE);
            listAccounts.setAlpha(0.0f);
            listAccounts.animate().alpha(1.0f);
            iconArrow.animate().rotation(180.0f);
        } else {
            listAccounts.animate().alpha(0.0f).withEndAction(new Runnable() {
                @Override
                public void run() {
                    if (listAccounts != null) {
                        listAccounts.setVisibility(View.GONE);
                    }
                }
            });
            iconArrow.animate().rotation(0.0f);
        }
    }

    private void switchToAccount(Account account) {
        Timber.d("Switching to account: %s", account);
        account.setLastUsed(new Date());
        App.get().setAccount(account);
        App.get().getPrefs().updateAccount(account);
        bindUser(account.getUser());
        toggleAccounts();
        App.bus().post(new ReloadDataEvent());
        App.bus().post(new CloseDrawerEvent());
        // Trigger a reload in the adapter so that we will place the accounts
        // in the correct order from most recently used
        adapterAccounts.notifyDataSetChanged();
        loadCurrentUser();
    }

        @Subscribe
        public void onUserLoggedIn(LoginEvent event) {
            if (adapterAccounts != null) {
                adapterAccounts.addAccount(event.account);
                adapterAccounts.notifyDataSetChanged();
                loadCurrentUser();
            }
        }
}

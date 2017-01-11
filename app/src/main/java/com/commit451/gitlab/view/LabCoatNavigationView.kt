package com.commit451.gitlab.view

import android.app.Activity
import android.content.Context
import android.support.design.widget.NavigationView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.commit451.alakazam.HideRunnable
import com.commit451.easel.Easel
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.activity.ActivityActivity
import com.commit451.gitlab.activity.GroupsActivity
import com.commit451.gitlab.activity.ProjectsActivity
import com.commit451.gitlab.activity.TodosActivity
import com.commit451.gitlab.adapter.AccountAdapter
import com.commit451.gitlab.data.Prefs
import com.commit451.gitlab.event.CloseDrawerEvent
import com.commit451.gitlab.event.LoginEvent
import com.commit451.gitlab.event.ReloadDataEvent
import com.commit451.gitlab.model.Account
import com.commit451.gitlab.model.api.UserFull
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.rx.CustomResponseSingleObserver
import com.commit451.gitlab.transformation.CircleTransformation
import com.commit451.gitlab.util.ImageUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber
import java.util.*

/**
 * Our very own navigation view
 */
class LabCoatNavigationView : NavigationView {

    @BindView(R.id.profile_image)
    lateinit var imageProfile: ImageView
    @BindView(R.id.profile_user)
    lateinit var textUserName: TextView
    @BindView(R.id.profile_email)
    lateinit var textEmail: TextView
    @BindView(R.id.arrow)
    lateinit var iconArrow: View

    lateinit var listAccounts: RecyclerView
    lateinit var adapterAccounts: AccountAdapter

    val mOnNavigationItemSelectedListener = OnNavigationItemSelectedListener { menuItem ->
        when (menuItem.itemId) {
            R.id.nav_projects -> {
                if (context !is ProjectsActivity) {
                    Navigator.navigateToProjects(context as Activity)
                    (context as Activity).finish()
                    (context as Activity).overridePendingTransition(R.anim.fade_in, R.anim.do_nothing)
                }
                App.bus().post(CloseDrawerEvent())
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_groups -> {
                if (context !is GroupsActivity) {
                    Navigator.navigateToGroups(context as Activity)
                    (context as Activity).finish()
                    (context as Activity).overridePendingTransition(R.anim.fade_in, R.anim.do_nothing)
                }
                App.bus().post(CloseDrawerEvent())
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_activity -> {
                if (context !is ActivityActivity) {
                    Navigator.navigateToActivity(context as Activity)
                    (context as Activity).finish()
                    (context as Activity).overridePendingTransition(R.anim.fade_in, R.anim.do_nothing)
                }
                App.bus().post(CloseDrawerEvent())
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_todos -> {
                if (context !is TodosActivity) {
                    Navigator.navigateToTodos(context as Activity)
                    (context as Activity).finish()
                    (context as Activity).overridePendingTransition(R.anim.fade_in, R.anim.do_nothing)
                }
                App.bus().post(CloseDrawerEvent())
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_about -> {
                App.bus().post(CloseDrawerEvent())
                Navigator.navigateToAbout(context as Activity)
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_settings -> {
                App.bus().post(CloseDrawerEvent())
                Navigator.navigateToSettings(context as Activity)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private val mAccountsAdapterListener = object : AccountAdapter.Listener {
        override fun onAccountClicked(account: Account) {
            switchToAccount(account)
        }

        override fun onAddAccountClicked() {
            Navigator.navigateToLogin(context as Activity, true)
            toggleAccounts()
            App.bus().post(CloseDrawerEvent())
        }

        override fun onAccountLogoutClicked(account: Account) {
            Prefs.removeAccount(account)
            val accounts = Account.getAccounts()

            if (accounts.isEmpty()) {
                Navigator.navigateToLogin(context as Activity)
                (context as Activity).finish()
            } else {
                if (account == App.get().getAccount()) {
                    switchToAccount(accounts[0])
                }
            }
        }
    }

    @OnClick(R.id.profile_image)
    fun onUserImageClick(imageView: ImageView) {
        Navigator.navigateToUser(context as Activity, imageView, App.get().getAccount().user)
    }

    @OnClick(R.id.drawer_header)
    fun onHeaderClick() {
        toggleAccounts()
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    fun init() {
        App.bus().register(this)
        val colorPrimary = Easel.getThemeAttrColor(context, R.attr.colorPrimary)

        setNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        inflateMenu(R.menu.navigation)
        setBackgroundColor(colorPrimary)
        val header = inflateHeaderView(R.layout.header_nav_drawer)
        ButterKnife.bind(this, header)

        listAccounts = RecyclerView(context)
        listAccounts.layoutManager = LinearLayoutManager(context)
        addView(listAccounts)
        val params = listAccounts.layoutParams as FrameLayout.LayoutParams
        params.setMargins(0, resources.getDimensionPixelSize(R.dimen.account_header_height), 0, 0)
        listAccounts.setBackgroundColor(colorPrimary)
        listAccounts.visibility = View.GONE
        adapterAccounts = AccountAdapter(context, mAccountsAdapterListener)
        listAccounts.adapter = adapterAccounts
        setSelectedNavigationItem()
        setAccounts()
        loadCurrentUser()
    }

    override fun onDetachedFromWindow() {
        App.bus().unregister(this)
        super.onDetachedFromWindow()
    }

    fun setSelectedNavigationItem() {
        if (context is ProjectsActivity) {
            setCheckedItem(R.id.nav_projects)
        } else if (context is GroupsActivity) {
            setCheckedItem(R.id.nav_groups)
        } else if (context is ActivityActivity) {
            setCheckedItem(R.id.nav_activity)
        } else if (context is TodosActivity) {
            setCheckedItem(R.id.nav_todos)
        } else {
            throw IllegalStateException("You need to defined a menu item for this activity")
        }
    }

    fun setAccounts() {
        val accounts = Prefs.getAccounts()
        Timber.d("Got %s accounts", accounts.size)
        Collections.sort(accounts)
        Collections.reverse(accounts)
        adapterAccounts.setAccounts(accounts)
    }

    fun loadCurrentUser() {
        App.get().gitLab.getThisUser()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : CustomResponseSingleObserver<UserFull>() {

                    override fun error(e: Throwable) {
                        Timber.e(e)
                    }

                    override fun responseSuccess(userFull: UserFull) {
                        //Store the newly retrieved user to the account so that it stays up to date
                        // in local storage
                        val account = App.get().getAccount()
                        account.user = userFull
                        Prefs.updateAccount(account)
                        bindUser(userFull)
                    }
                })
    }

    fun bindUser(user: UserFull) {
        if (context == null) {
            return
        }
        if (user.username != null) {
            textUserName.text = user.username
        }
        if (user.email != null) {
            textEmail.text = user.email
        }
        val url = ImageUtil.getAvatarUrl(user, resources.getDimensionPixelSize(R.dimen.larger_image_size))
        App.get().picasso
                .load(url)
                .transform(CircleTransformation())
                .into(imageProfile)
    }

    /**
     * Toggle the visibility of accounts. Meaning hide it if it is showing, show it if it is hidden
     */
    fun toggleAccounts() {
        if (listAccounts.visibility == View.GONE) {
            listAccounts.visibility = View.VISIBLE
            listAccounts.alpha = 0.0f
            listAccounts.animate().alpha(1.0f)
            iconArrow.animate().rotation(180.0f)
        } else {
            listAccounts.animate().alpha(0.0f).withEndAction(HideRunnable(listAccounts))
            iconArrow.animate().rotation(0.0f)
        }
    }

    fun switchToAccount(account: Account) {
        Timber.d("Switching to account: %s", account)
        account.lastUsed = Date()
        App.get().setAccount(account)
        Prefs.updateAccount(account)
        bindUser(account.user)
        toggleAccounts()
        App.bus().post(ReloadDataEvent())
        App.bus().post(CloseDrawerEvent())
        // Trigger a reload in the adapter so that we will place the accounts
        // in the correct order from most recently used
        adapterAccounts.notifyDataSetChanged()
        loadCurrentUser()
    }

    @Subscribe
    fun onUserLoggedIn(event: LoginEvent) {
        adapterAccounts.addAccount(event.account)
        adapterAccounts.notifyDataSetChanged()
        loadCurrentUser()
    }
}

package com.commit451.gitlab.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.gitlab.R
import com.commit451.gitlab.activity.BaseActivity
import com.commit451.gitlab.data.Prefs
import com.commit451.gitlab.model.Account
import timber.log.Timber
import java.util.*

/**
 * The configuration screen for the ExampleAppWidgetProvider widget sample.
 */
class UserFeedWidgetConfigureActivity : BaseActivity() {

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar
    @BindView(R.id.message_text)
    lateinit var textMessage: TextView
    @BindView(R.id.list)
    lateinit var list: RecyclerView

    lateinit var adapterAccounts: AccountsAdapter

    var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setResult(Activity.RESULT_CANCELED)
        setContentView(R.layout.activity_feed_widget_configure)
        ButterKnife.bind(this)

        // Find the widget id from the intent.
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        }
        // If they gave us an intent without the widget id, just bail.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
        }

        toolbar.setTitle(R.string.widget_choose_account)

        adapterAccounts = AccountsAdapter()
        adapterAccounts.setOnItemClickListener { adapter, _, position ->
            saveWidgetConfig(adapter.get(position))
        }
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapterAccounts

        loadAccounts()
    }

    fun loadAccounts() {
        val accounts = Prefs.getAccounts()
        Timber.d("Got %s accounts", accounts.size)
        Collections.sort(accounts)
        Collections.reverse(accounts)
        if (accounts.isEmpty()) {
            textMessage.visibility = View.VISIBLE
        } else {
            textMessage.visibility = View.GONE
            adapterAccounts.clearAndFill(accounts)
        }
    }

    fun saveWidgetConfig(account: Account) {
        UserFeedWidgetPrefs.setAccount(this@UserFeedWidgetConfigureActivity, appWidgetId, account)
        val appWidgetManager = AppWidgetManager.getInstance(this@UserFeedWidgetConfigureActivity)

        // Make sure we pass back the original appWidgetId
        val data = Intent()
        val extras = Bundle()
        extras.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        data.putExtras(extras)
        setResult(Activity.RESULT_OK, data)
        appWidgetManager.updateAppWidgetOptions(appWidgetId, extras)
        finish()
        //Manually have to trigger on update here, it seems
        WidgetUtil.triggerWidgetUpdate(this, UserFeedWidgetProvider::class.java, appWidgetId)
    }

}

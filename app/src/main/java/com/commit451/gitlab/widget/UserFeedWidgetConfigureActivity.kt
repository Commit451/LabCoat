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
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.activity.BaseActivity
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
        adapterAccounts.setOnItemClickListener { adapter, view, position -> saveWidgetConfig(adapter.get(position)) }
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapterAccounts

        loadAccounts()
    }

    fun loadAccounts() {
        val accounts = App.get().prefs.getAccounts()
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
        // Push widget update to surface with newly set prefix
        val appWidgetManager = AppWidgetManager.getInstance(this@UserFeedWidgetConfigureActivity)
        //        ExampleAppWidgetProvider.updateAppWidget(context, appWidgetManager,
        //                appWidgetId, titlePrefix);
        // Make sure we pass back the original appWidgetId
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }

}

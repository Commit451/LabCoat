package com.commit451.gitlab.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.commit451.gitlab.R
import com.commit451.gitlab.activity.BaseActivity
import com.commit451.gitlab.data.Prefs
import com.commit451.gitlab.extension.feedUrl
import com.commit451.gitlab.model.Account
import com.commit451.gitlab.model.api.Project
import kotlinx.android.synthetic.main.activity_feed_widget_configure.*
import timber.log.Timber

/**
 * The configuration screen for the ExampleAppWidgetProvider widget sample.
 */
class ProjectFeedWidgetConfigureActivity : BaseActivity() {

    companion object {
        private const val REQUEST_PROJECT = 1
    }

    private lateinit var adapterAccounts: AccountsAdapter

    private var widgetAccount: Account? = null
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setResult(Activity.RESULT_CANCELED)
        setContentView(R.layout.activity_feed_widget_configure)

        // Find the widget id from the intent.
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        }
        // If they gave us an intent without the widget id, just bail.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Timber.e("We did not get a widget id. Bail out")
            finish()
        }

        toolbar.setTitle(R.string.widget_choose_account)

        adapterAccounts = AccountsAdapter(object : AccountsAdapter.Listener {
            override fun onAccountClicked(account: Account) {
                widgetAccount = account
                moveAlongToChooseProject(account)
            }
        })
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapterAccounts

        loadAccounts()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_PROJECT -> {
                if (resultCode == Activity.RESULT_OK) {
                    val project = data?.getParcelableExtra<Project>(ProjectFeedWidgetConfigureProjectActivity.EXTRA_PROJECT)!!
                    saveWidgetConfig(widgetAccount!!, project)
                }
            }
        }
    }

    private fun loadAccounts() {
        val accounts = Prefs.getAccounts()
        Timber.d("Got %s accounts", accounts.size)
        accounts.sort()
        accounts.reverse()
        if (accounts.isEmpty()) {
            textMessage.visibility = View.VISIBLE
        } else {
            textMessage.visibility = View.GONE
            adapterAccounts.clearAndFill(accounts)
        }
    }

    private fun moveAlongToChooseProject(account: Account) {
        val intent = ProjectFeedWidgetConfigureProjectActivity.newIntent(this, account)
        startActivityForResult(intent, REQUEST_PROJECT)
    }

    private fun saveWidgetConfig(account: Account, project: Project) {
        ProjectFeedWidgetPrefs.setAccount(this, appWidgetId, account)
        ProjectFeedWidgetPrefs.setFeedUrl(this, appWidgetId, project.feedUrl)

        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(Activity.RESULT_OK, resultValue)
        finish()

        //Manually have to trigger on update here, it seems
        WidgetUtil.triggerWidgetUpdate(this, ProjectFeedWidgetProvider::class.java, appWidgetId)
    }
}

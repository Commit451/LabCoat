package com.commit451.gitlab.activity

import androidx.appcompat.app.AppCompatActivity
import com.commit451.gitlab.App
import com.commit451.gitlab.api.GitLab
import com.commit451.gitlab.model.Account
import com.novoda.simplechromecustomtabs.SimpleChromeCustomTabs
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider

/**
 * Base activity for others to derive from
 */
abstract class BaseActivity : AppCompatActivity() {

    val scopeProvider: AndroidLifecycleScopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }

    val account: Account
        get() = App.get().gitLab.account

    val gitLab: GitLab
        get() = App.get().gitLab

    open fun hasBrowsableLinks(): Boolean {
        return false
    }

    override fun onResume() {
        super.onResume()
        if (hasBrowsableLinks()) {
            SimpleChromeCustomTabs.getInstance().connectTo(this)
        }
    }

    override fun onPause() {
        if (hasBrowsableLinks() && SimpleChromeCustomTabs.getInstance().isConnected) {
            SimpleChromeCustomTabs.getInstance().disconnectFrom(this)
        }
        super.onPause()
    }
}

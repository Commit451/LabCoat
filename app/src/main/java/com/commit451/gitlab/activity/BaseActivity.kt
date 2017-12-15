package com.commit451.gitlab.activity

import android.support.v7.app.AppCompatActivity
import com.novoda.simplechromecustomtabs.SimpleChromeCustomTabs
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider

/**
 * Base activity for others to derive from
 */
abstract class BaseActivity : AppCompatActivity() {

    val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }

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

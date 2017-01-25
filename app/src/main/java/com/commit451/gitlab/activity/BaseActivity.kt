package com.commit451.gitlab.activity

import com.novoda.simplechromecustomtabs.SimpleChromeCustomTabs
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity

/**
 * Base activity for others to derive from
 */
open class BaseActivity : RxAppCompatActivity() {

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

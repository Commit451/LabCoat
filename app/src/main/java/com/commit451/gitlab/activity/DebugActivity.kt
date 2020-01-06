package com.commit451.gitlab.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.commit451.gitlab.R
import kotlinx.android.synthetic.main.activity_debug.*

/**
 * Allows some debugging
 */
class DebugActivity : BaseActivity() {

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, DebugActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)

        toolbar.title = "Here Be Dragons"
        toolbar.setNavigationIcon(R.drawable.ic_back_24dp)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }
}

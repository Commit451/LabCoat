package com.commit451.gitlab.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import butterknife.ButterKnife
import com.commit451.gitlab.R
import com.commit451.gitlab.data.Prefs
import com.readystatesoftware.chuck.Chuck
import kotlinx.android.synthetic.main.activity_settings.*

/**
 * Settings
 */
class SettingsActivity : BaseActivity() {

    companion object {

        fun newIntent(context: Context): Intent {
            val intent = Intent(context, SettingsActivity::class.java)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        ButterKnife.bind(this)
        toolbar.setTitle(R.string.settings)
        toolbar.setNavigationIcon(R.drawable.ic_back_24dp)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        if (Build.VERSION.SDK_INT < 21) {
            //lollipop+ only!
            rootRequireDeviceAuth.visibility = View.GONE
        }

        bindPrefs()
        switchRequireAuth.setOnCheckedChangeListener { _, b ->
            Prefs.isRequiredDeviceAuth = b
        }
        rootRequireDeviceAuth.setOnClickListener { switchRequireAuth.toggle() }
        rootNetwork.setOnClickListener {
            val intent = Chuck.getLaunchIntent(this)
            startActivity(intent)
        }
    }

    fun bindPrefs() {
        switchRequireAuth.isChecked = Prefs.isRequiredDeviceAuth
    }
}

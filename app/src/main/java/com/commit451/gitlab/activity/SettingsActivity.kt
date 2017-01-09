package com.commit451.gitlab.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.widget.SwitchCompat
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.ViewGroup
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.commit451.gitlab.App
import com.commit451.gitlab.R

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

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar
    @BindView(R.id.root_require_device_auth)
    lateinit var rootRequireDeviceAuth: ViewGroup
    @BindView(R.id.switch_require_auth)
    lateinit var switchRequireAuth: SwitchCompat

    @OnClick(R.id.root_require_device_auth)
    fun onRequireDeviceAuthClicked() {
        switchRequireAuth.toggle()
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
        switchRequireAuth.setOnCheckedChangeListener { compoundButton, b -> App.get().prefs.isRequireDeviceAuth = b }
    }

    fun bindPrefs() {
        switchRequireAuth.isChecked = App.get().prefs.isRequireDeviceAuth
    }
}

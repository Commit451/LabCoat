package com.commit451.gitlab.activity

import android.annotation.TargetApi
import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.gitlab.R
import com.commit451.gitlab.data.Prefs
import com.commit451.gitlab.model.Account
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.ssl.CustomKeyManager

/**
 * This activity acts as switching platform for the application directing the user to the appropriate
 * activity based on their logged in state
 */
class LaunchActivity : BaseActivity() {

    companion object {
        private val REQUEST_DEVICE_AUTH = 123
    }

    @BindView(R.id.root) lateinit var root: ViewGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)
        ButterKnife.bind(this)
        figureOutWhatToDo()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_DEVICE_AUTH -> if (resultCode == Activity.RESULT_OK) {
                moveAlong()
            } else {
                finish()
            }
        }
    }

    private fun figureOutWhatToDo() {
        val accounts = Prefs.getAccounts()
        if (accounts.isEmpty()) {
            Navigator.navigateToLogin(this)
            finish()
        } else if (Prefs.isRequiredDeviceAuth) {
            showKeyguard()
        } else {
            moveAlong()
        }
    }

    @TargetApi(21)
    fun showKeyguard() {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val intent = keyguardManager.createConfirmDeviceCredentialIntent(getString(R.string.device_auth_title), getString(R.string.device_auth_message))
        if (intent == null) {
            moveAlong()
        } else {
            startActivityForResult(intent, REQUEST_DEVICE_AUTH)
        }
    }

    private fun moveAlong() {
        Navigator.navigateToStartingActivity(this)
        finish()
    }
}

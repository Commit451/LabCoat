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

        //Figure out how this works, then reenable
        private val PRIVATE_KEY_ENABLED = false

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
            if (PRIVATE_KEY_ENABLED) {
                loadPrivateKey(accounts, 0)
            } else {
                moveAlong()
            }
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

    private fun loadPrivateKey(accounts: List<Account>, i: Int) {
        if (i >= accounts.size) {
            runOnUiThread { }
            return
        }

        val alias = accounts[i].privateKeyAlias
        if (alias != null && !CustomKeyManager.isCached(alias)) {
            CustomKeyManager.cache(this, alias, object : CustomKeyManager.KeyCallback {
                override fun onSuccess(entry: CustomKeyManager.KeyEntry) {
                    loadPrivateKey(accounts, i + 1)
                }

                override fun onError(e: Exception) {
                    loadPrivateKey(accounts, i + 1)
                }
            })
        } else {
            loadPrivateKey(accounts, i + 1)
        }
    }
}

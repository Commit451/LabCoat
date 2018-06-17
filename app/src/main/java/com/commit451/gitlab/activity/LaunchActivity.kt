package com.commit451.gitlab.activity

import android.annotation.TargetApi
import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.core.widget.toast
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.gitlab.R
import com.commit451.gitlab.data.Prefs
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.migration.Migration261
import com.commit451.gitlab.navigation.Navigator
import timber.log.Timber

/**
 * This activity acts as switching platform for the application directing the user to the appropriate
 * activity based on their logged in state
 */
class LaunchActivity : BaseActivity() {

    companion object {
        private const val REQUEST_DEVICE_AUTH = 123
    }

    @BindView(R.id.root)
    lateinit var root: ViewGroup

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
    private fun showKeyguard() {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val intent = keyguardManager.createConfirmDeviceCredentialIntent(getString(R.string.device_auth_title), getString(R.string.device_auth_message))
        if (intent == null) {
            moveAlong()
        } else {
            startActivityForResult(intent, REQUEST_DEVICE_AUTH)
        }
    }

    private fun moveAlong() {
        if (account.username == null || account.email == null) {
            Migration261.run()
                    .with(this)
                    .subscribe({
                        Navigator.navigateToStartingActivity(this)
                        finish()
                    }, {
                        Timber.e(it)
                        toast("Unable to migrate. Unfortunately, you probably need to re-install the app")
                        finish()
                    })
        } else {
            Navigator.navigateToStartingActivity(this)
            finish()
        }
    }
}

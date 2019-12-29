package com.commit451.gitlab.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import com.commit451.gitlab.R
import com.commit451.gitlab.data.Prefs
import kotlinx.android.synthetic.main.activity_settings.*


/**
 * Settings
 */
class SettingsActivity : BaseActivity() {

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, SettingsActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        toolbar.setTitle(R.string.settings)
        toolbar.setNavigationIcon(R.drawable.ic_back_24dp)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        bindPrefs()
        switchRequireAuth.setOnCheckedChangeListener { _, isChecked ->
            Prefs.isRequiredDeviceAuth = isChecked
        }
        rootRequireDeviceAuth.setOnClickListener { switchRequireAuth.toggle() }
        rootAppearance.setOnClickListener {
            appearance()
        }
    }

    private fun bindPrefs() {
        switchRequireAuth.isChecked = Prefs.isRequiredDeviceAuth
    }

    private fun appearance() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle(R.string.setting_appearance)
        val items = arrayOf(
                getString(R.string.device_theme),
                getString(R.string.light_theme),
                getString(R.string.dark_theme)
        )
        val checkedItem = mapToChoice(Prefs.theme)
        alertDialog.setSingleChoiceItems(items, checkedItem) { _, which ->
            val mode = when (which) {
                0 -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                1 -> AppCompatDelegate.MODE_NIGHT_NO
                2 -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            Prefs.theme = mode
            AppCompatDelegate.setDefaultNightMode(mode)
        }
        alertDialog.show()
    }

    private fun mapToChoice(themePref: Int): Int {
        return when (themePref) {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> 0
            AppCompatDelegate.MODE_NIGHT_NO -> 1
            AppCompatDelegate.MODE_NIGHT_YES -> 2
            else -> 0
        }
    }
}

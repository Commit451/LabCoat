package com.commit451.gitlab.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.commit451.addendum.design.snackbar
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.BuildPagerAdapter
import com.commit451.gitlab.event.BuildChangedEvent
import com.commit451.gitlab.extension.getDownloadBuildUrl
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.Build
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.util.DownloadUtil
import kotlinx.android.synthetic.main.activity_build.*
import kotlinx.android.synthetic.main.progress_fullscreen.*
import timber.log.Timber

/**
 * Shows the details of a build
 */
class BuildActivity : BaseActivity() {

    companion object {

        private const val REQUEST_PERMISSION_WRITE_STORAGE = 1337

        private const val KEY_PROJECT = "key_project"
        private const val KEY_BUILD = "key_merge_request"

        fun newIntent(context: Context, project: Project, build: Build): Intent {
            val intent = Intent(context, BuildActivity::class.java)
            intent.putExtra(KEY_PROJECT, project)
            intent.putExtra(KEY_BUILD, build)
            return intent
        }
    }

    private lateinit var menuItemDownload: MenuItem

    private lateinit var project: Project
    private lateinit var build: Build

    private val onMenuItemClickListener = Toolbar.OnMenuItemClickListener { item ->
        when (item.itemId) {
            R.id.action_retry -> {
                fullscreenProgress.visibility = View.VISIBLE
                App.get().gitLab.retryBuild(project.id, build.id)
                        .with(this)
                        .subscribe({
                            fullscreenProgress.visibility = View.GONE
                            root.snackbar(R.string.build_started)
                            App.bus().post(BuildChangedEvent(build))
                        }, {
                            Timber.e(it)
                            fullscreenProgress.visibility = View.GONE
                            root.snackbar(R.string.unable_to_retry_build)
                        })
                return@OnMenuItemClickListener true
            }
            R.id.action_erase -> {
                fullscreenProgress.visibility = View.VISIBLE
                App.get().gitLab.eraseBuild(project.id, build.id)
                        .with(this)
                        .subscribe({
                            fullscreenProgress.visibility = View.GONE
                            root.snackbar(R.string.build_erased)
                            App.bus().post(BuildChangedEvent(it))
                        }, {
                            Timber.e(it)
                            fullscreenProgress.visibility = View.GONE
                            root.snackbar(R.string.unable_to_erase_build)
                        })
                return@OnMenuItemClickListener true
            }
            R.id.action_cancel -> {
                fullscreenProgress.visibility = View.VISIBLE
                App.get().gitLab.cancelBuild(project.id, build.id)
                        .with(this)
                        .subscribe({
                            fullscreenProgress.visibility = View.GONE
                            root.snackbar(R.string.build_canceled)
                            App.bus().post(BuildChangedEvent(it))
                        }, {
                            Timber.e(it)
                            fullscreenProgress.visibility = View.GONE
                            root.snackbar(R.string.unable_to_cancel_build)
                        })
                return@OnMenuItemClickListener true
            }
            R.id.action_download -> {
                checkDownloadBuild()
                return@OnMenuItemClickListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_build)

        project = intent.getParcelableExtra(KEY_PROJECT)!!
        build = intent.getParcelableExtra(KEY_BUILD)!!

        toolbar.title = String.format(getString(R.string.build_number), build.id)
        toolbar.setNavigationIcon(R.drawable.ic_back_24dp)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        toolbar.subtitle = project.nameWithNamespace
        toolbar.inflateMenu(R.menu.retry)
        toolbar.inflateMenu(R.menu.erase)
        toolbar.inflateMenu(R.menu.cancel)
        toolbar.inflateMenu(R.menu.download)
        toolbar.setOnMenuItemClickListener(onMenuItemClickListener)
        menuItemDownload = toolbar.menu.findItem(R.id.action_download)
        menuItemDownload.isVisible = build.artifactsFile != null
        setupTabs()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSION_WRITE_STORAGE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    downloadBuild()
                }
            }
        }
    }

    private fun setupTabs() {
        val sectionsPagerAdapter = BuildPagerAdapter(
                this,
                supportFragmentManager,
                project,
                build)

        viewPager.adapter = sectionsPagerAdapter
        tabLayout.setupWithViewPager(viewPager)
    }

    @SuppressLint("NewApi")
    private fun checkDownloadBuild() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            downloadBuild()
        } else {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSION_WRITE_STORAGE)
        }
    }

    private fun downloadBuild() {
        val account = App.get().getAccount()
        val downloadUrl = build.getDownloadBuildUrl(App.get().getAccount().serverUrl!!, project)
        Timber.d("Downloading build: $downloadUrl")
        val artifactsFileName = build.artifactsFile?.fileName
        if (artifactsFileName != null) {
            DownloadUtil.download(this, account, downloadUrl, artifactsFileName)
        }
    }
}

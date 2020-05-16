package com.commit451.gitlab.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.MergeRequestSectionsPagerAdapter
import com.commit451.gitlab.event.MergeRequestChangedEvent
import com.commit451.gitlab.extension.mapResponseSuccess
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.MergeRequest
import com.commit451.gitlab.model.api.Project
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_merge_request.*
import kotlinx.android.synthetic.main.progress_fullscreen.*
import retrofit2.HttpException
import timber.log.Timber

/**
 * Shows the details of a merge request
 */
class MergeRequestActivity : BaseActivity() {

    companion object {

        private const val KEY_PROJECT = "key_project"
        private const val KEY_MERGE_REQUEST = "key_merge_request"

        fun newIntent(context: Context, project: Project, mergeRequest: MergeRequest): Intent {
            val intent = Intent(context, MergeRequestActivity::class.java)
            intent.putExtra(KEY_PROJECT, project)
            intent.putExtra(KEY_MERGE_REQUEST, mergeRequest)
            return intent
        }
    }

    private lateinit var project: Project
    private lateinit var mergeRequest: MergeRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_merge_request)

        project = intent.getParcelableExtra(KEY_PROJECT)!!
        mergeRequest = intent.getParcelableExtra(KEY_MERGE_REQUEST)!!

        toolbar.title = getString(R.string.merge_request_number) + mergeRequest.iid
        toolbar.setNavigationIcon(R.drawable.ic_back_24dp)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        toolbar.subtitle = project.nameWithNamespace
        if (mergeRequest.state == MergeRequest.STATE_OPENED) {
            toolbar.inflateMenu(R.menu.merge)
        }
        toolbar.setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_merge -> {
                    merge()
                    return@OnMenuItemClickListener true
                }
            }
            false
        })
        val sectionsPagerAdapter = MergeRequestSectionsPagerAdapter(
                this,
                supportFragmentManager,
                project,
                mergeRequest)

        viewPager.adapter = sectionsPagerAdapter
        tabLayout.setupWithViewPager(viewPager)
    }

    fun merge() {
        fullscreenProgress.visibility = View.VISIBLE
        App.get().gitLab.acceptMergeRequest(project.id, mergeRequest.iid)
                .mapResponseSuccess()
                .with(this)
                .subscribe({
                    fullscreenProgress.visibility = View.GONE
                    Snackbar.make(root, R.string.merge_request_accepted, Snackbar.LENGTH_LONG)
                            .show()
                    App.bus().post(MergeRequestChangedEvent(mergeRequest))
                }, {
                    Timber.e(it)
                    fullscreenProgress.visibility = View.GONE
                    var message = getString(R.string.unable_to_merge)
                    if (it is HttpException) {
                        val code = it.response()?.code()
                        if (code == 406) {
                            message = getString(R.string.merge_request_already_merged_or_closed)
                        }
                    }
                    Snackbar.make(root, message, Snackbar.LENGTH_LONG)
                            .show()
                })
    }
}

package com.commit451.gitlab.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.ViewGroup
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.MergeRequestSectionsPagerAdapter
import com.commit451.gitlab.event.MergeRequestChangedEvent
import com.commit451.gitlab.extension.setup
import com.commit451.gitlab.model.api.MergeRequest
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.rx.CustomResponseSingleObserver
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException
import org.parceler.Parcels
import timber.log.Timber

/**
 * Shows the details of a merge request
 */
class MergeRequestActivity : BaseActivity() {

    companion object {

        private val KEY_PROJECT = "key_project"
        private val KEY_MERGE_REQUEST = "key_merge_request"

        fun newIntent(context: Context, project: Project, mergeRequest: MergeRequest): Intent {
            val intent = Intent(context, MergeRequestActivity::class.java)
            intent.putExtra(KEY_PROJECT, Parcels.wrap(project))
            intent.putExtra(KEY_MERGE_REQUEST, Parcels.wrap(mergeRequest))
            return intent
        }
    }

    @BindView(R.id.root) lateinit var root: ViewGroup
    @BindView(R.id.toolbar) lateinit var toolbar: Toolbar
    @BindView(R.id.tabs) lateinit var tabLayout: TabLayout
    @BindView(R.id.pager) lateinit var viewPager: ViewPager
    @BindView(R.id.progress) lateinit var progress: View

    lateinit var project: Project
    lateinit var mergeRequest: MergeRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_merge_request)
        ButterKnife.bind(this)

        project = Parcels.unwrap<Project>(intent.getParcelableExtra<Parcelable>(KEY_PROJECT))
        mergeRequest = Parcels.unwrap<MergeRequest>(intent.getParcelableExtra<Parcelable>(KEY_MERGE_REQUEST))

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
        progress.visibility = View.VISIBLE
        App.get().gitLab.acceptMergeRequest(project.id, mergeRequest.id)
                .setup(bindToLifecycle())
                .subscribe(object : CustomResponseSingleObserver<MergeRequest>() {

                    override fun error(e: Throwable) {
                        Timber.e(e)
                        progress.visibility = View.GONE
                        var message = getString(R.string.unable_to_merge)
                        if (e is HttpException) {
                            val code = e.response().code()
                            if (code == 406) {
                                message = getString(R.string.merge_request_already_merged_or_closed)
                            }
                        }
                        Snackbar.make(root, message, Snackbar.LENGTH_LONG)
                                .show()
                    }

                    override fun responseSuccess(mergeRequest: MergeRequest) {
                        progress.visibility = View.GONE
                        Snackbar.make(root, R.string.merge_request_accepted, Snackbar.LENGTH_LONG)
                                .show()
                        App.bus().post(MergeRequestChangedEvent(mergeRequest))
                    }
                })
    }
}

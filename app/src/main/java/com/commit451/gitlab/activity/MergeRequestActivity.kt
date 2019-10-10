package com.commit451.gitlab.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.MergeRequestSectionsPagerAdapter
import com.commit451.gitlab.event.MergeRequestChangedEvent
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.MergeRequest
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.rx.CustomResponseSingleObserver
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
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

    @BindView(R.id.root)
    lateinit var root: ViewGroup
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar
    @BindView(R.id.tabs)
    lateinit var tabLayout: TabLayout
    @BindView(R.id.pager)
    lateinit var viewPager: ViewPager
    @BindView(R.id.progress)
    lateinit var progress: View

    lateinit var project: Project
    lateinit var mergeRequest: MergeRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_merge_request)
        ButterKnife.bind(this)

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
        progress.visibility = View.VISIBLE
        App.get().gitLab.acceptMergeRequest(project.id, mergeRequest.iid)
                .with(this)
                .subscribe(object : CustomResponseSingleObserver<MergeRequest>() {

                    override fun error(e: Throwable) {
                        Timber.e(e)
                        progress.visibility = View.GONE
                        var message = getString(R.string.unable_to_merge)
                        if (e is HttpException) {
                            val code = e.response()?.code()
                            if (code == 406) {
                                message = getString(R.string.merge_request_already_merged_or_closed)
                            }
                        }
                        Snackbar.make(root, message, Snackbar.LENGTH_LONG)
                                .show()
                    }

                    override fun responseNonNullSuccess(mergeRequest: MergeRequest) {
                        progress.visibility = View.GONE
                        Snackbar.make(root, R.string.merge_request_accepted, Snackbar.LENGTH_LONG)
                                .show()
                        App.bus().post(MergeRequestChangedEvent(mergeRequest))
                    }
                })
    }
}

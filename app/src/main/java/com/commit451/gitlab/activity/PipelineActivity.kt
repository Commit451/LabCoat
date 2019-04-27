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
import com.commit451.gitlab.adapter.PipelinePagerAdapter
import com.commit451.gitlab.event.PipelineChangedEvent
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.Pipeline
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.rx.CustomSingleObserver
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import timber.log.Timber

/**
 * Shows the details of a pipeline
 */
class PipelineActivity : BaseActivity() {

    companion object {

        private const val KEY_PROJECT = "key_project"
        private const val KEY_PIPELINE = "key_merge_request"

        fun newIntent(context: Context, project: Project, pipeline: Pipeline): Intent {
            val intent = Intent(context, PipelineActivity::class.java)
            intent.putExtra(KEY_PROJECT, project)
            intent.putExtra(KEY_PIPELINE, pipeline)
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
    lateinit var pipeline: Pipeline

    private val onMenuItemClickListener = Toolbar.OnMenuItemClickListener { item ->
        when (item.itemId) {
            R.id.action_retry -> {
                progress.visibility = View.VISIBLE
                App.get().gitLab.retryPipeline(project.id, pipeline.id)
                        .with(this)
                        .subscribe(object : CustomSingleObserver<Pipeline>() {

                            override fun error(t: Throwable) {
                                Timber.e(t)
                                progress.visibility = View.GONE
                                Snackbar.make(root, R.string.unable_to_retry_pipeline, Snackbar.LENGTH_LONG)
                                        .show()
                            }

                            override fun success(pipeline: Pipeline) {
                                progress.visibility = View.GONE
                                Snackbar.make(root, R.string.pipeline_started, Snackbar.LENGTH_LONG)
                                        .show()
                                App.bus().post(PipelineChangedEvent(pipeline))
                            }
                        })
                return@OnMenuItemClickListener true
            }
            R.id.action_cancel -> {
                progress.visibility = View.VISIBLE
                App.get().gitLab.cancelPipeline(project.id, pipeline.id)
                        .with(this)
                        .subscribe(object : CustomSingleObserver<Pipeline>() {

                            override fun error(t: Throwable) {
                                Timber.e(t)
                                progress.visibility = View.GONE
                                Snackbar.make(root, R.string.unable_to_cancel_pipeline, Snackbar.LENGTH_LONG)
                                        .show()
                            }

                            override fun success(pipeline: Pipeline) {
                                progress.visibility = View.GONE
                                Snackbar.make(root, R.string.pipeline_canceled, Snackbar.LENGTH_LONG)
                                        .show()
                                App.bus().post(PipelineChangedEvent(pipeline))
                            }
                        })
                return@OnMenuItemClickListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pipeline)
        ButterKnife.bind(this)

        project = intent.getParcelableExtra(KEY_PROJECT)!!
        pipeline = intent.getParcelableExtra(KEY_PIPELINE)!!

        toolbar.title = String.format(getString(R.string.pipeline_number), pipeline.id)
        toolbar.setNavigationIcon(R.drawable.ic_back_24dp)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        toolbar.subtitle = project.nameWithNamespace
        toolbar.inflateMenu(R.menu.retry)
        toolbar.inflateMenu(R.menu.cancel)
        toolbar.setOnMenuItemClickListener(onMenuItemClickListener)
        setupTabs()
    }

    private fun setupTabs() {
        val sectionsPagerAdapter = PipelinePagerAdapter(
                this,
                supportFragmentManager,
                project,
                pipeline)

        viewPager.adapter = sectionsPagerAdapter
        tabLayout.setupWithViewPager(viewPager)
    }
}

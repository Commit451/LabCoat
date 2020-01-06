package com.commit451.gitlab.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.PipelinePagerAdapter
import com.commit451.gitlab.event.PipelineChangedEvent
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.Pipeline
import com.commit451.gitlab.model.api.Project
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_pipeline.*
import kotlinx.android.synthetic.main.progress_fullscreen.*
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

    private lateinit var project: Project
    private lateinit var pipeline: Pipeline

    private val onMenuItemClickListener = Toolbar.OnMenuItemClickListener { item ->
        when (item.itemId) {
            R.id.action_retry -> {
                progress.visibility = View.VISIBLE
                App.get().gitLab.retryPipeline(project.id, pipeline.id)
                        .with(this)
                        .subscribe({
                            progress.visibility = View.GONE
                            Snackbar.make(root, R.string.pipeline_started, Snackbar.LENGTH_LONG)
                                    .show()
                            App.bus().post(PipelineChangedEvent(it))
                        }, {
                            Timber.e(it)
                            progress.visibility = View.GONE
                            Snackbar.make(root, R.string.unable_to_retry_pipeline, Snackbar.LENGTH_LONG)
                                    .show()
                        })
                return@OnMenuItemClickListener true
            }
            R.id.action_cancel -> {
                progress.visibility = View.VISIBLE
                App.get().gitLab.cancelPipeline(project.id, pipeline.id)
                        .with(this)
                        .subscribe({
                            progress.visibility = View.GONE
                            Snackbar.make(root, R.string.pipeline_canceled, Snackbar.LENGTH_LONG)
                                    .show()
                            App.bus().post(PipelineChangedEvent(it))
                        }, {
                            Timber.e(it)
                            progress.visibility = View.GONE
                            Snackbar.make(root, R.string.unable_to_cancel_pipeline, Snackbar.LENGTH_LONG)
                                    .show()
                        })
                return@OnMenuItemClickListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pipeline)

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
                pipeline
        )

        viewPager.adapter = sectionsPagerAdapter
        tabLayout.setupWithViewPager(viewPager)
    }
}

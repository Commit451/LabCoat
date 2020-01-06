package com.commit451.gitlab.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.event.BuildChangedEvent
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.*
import com.commit451.gitlab.util.DateUtil
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_build_description.*
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber
import java.util.*

/**
 * Shows the details of a build
 */
class BuildDescriptionFragment : BaseFragment() {

    companion object {

        private const val KEY_PROJECT = "project"
        private const val KEY_BUILD = "build"

        fun newInstance(project: Project, build: Build): BuildDescriptionFragment {
            val fragment = BuildDescriptionFragment()
            val args = Bundle()
            args.putParcelable(KEY_PROJECT, project)
            args.putParcelable(KEY_BUILD, build)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var project: Project
    private lateinit var build: Build

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = arguments?.getParcelable(KEY_PROJECT)!!
        build = arguments?.getParcelable(KEY_BUILD)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_build_description, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipeRefreshLayout.setOnRefreshListener { load() }
        bindBuild(build)
        App.bus().register(this)
    }

    override fun onDestroyView() {
        App.bus().unregister(this)
        super.onDestroyView()
    }

    fun load() {
        App.get().gitLab.getBuild(project.id, build.id)
                .with(this)
                .subscribe({
                    swipeRefreshLayout.isRefreshing = false
                    build = it
                    bindBuild(build)
                    App.bus().post(BuildChangedEvent(it))
                }, {
                    Timber.e(it)
                    Snackbar.make(root, R.string.unable_to_load_build, Snackbar.LENGTH_LONG)
                            .show()
                })
    }

    private fun bindBuild(build: Build) {
        var finishedTime: Date? = build.finishedAt
        if (finishedTime == null) {
            finishedTime = Date()
        }
        var startedTime: Date? = build.startedAt
        if (startedTime == null) {
            startedTime = Date()
        }
        val name = String.format(getString(R.string.build_name), build.name)
        textName.text = name
        val pipelineText = String.format(getString(R.string.build_pipeline), build.pipeline)
        textPipeline.text = pipelineText
        val stage = String.format(getString(R.string.build_stage), build.stage)
        textStage.text = stage
        val status = String.format(getString(R.string.build_status), build.status)
        textStatus.text = status
        val timeTaken = DateUtil.getTimeTaken(startedTime, finishedTime)
        val duration = String.format(getString(R.string.build_duration), timeTaken)
        textDuration.text = duration
        val created = String.format(getString(R.string.build_created), DateUtil.getRelativeTimeSpanString(baseActivty, build.createdAt))
        textCreated.text = created
        val ref = String.format(getString(R.string.build_ref), build.ref)
        textRef.text = ref
        val finishedAt = build.finishedAt
        if (finishedAt != null) {
            val finished = String.format(getString(R.string.build_finished), DateUtil.getRelativeTimeSpanString(baseActivty, finishedAt))
            textFinished.text = finished
            textFinished.visibility = View.VISIBLE
        } else {
            textFinished.visibility = View.GONE
        }
        val runner = build.runner
        if (runner != null) {
            bindRunner(runner)
        }
        val pipeline = build.pipeline
        if (pipeline != null) {
            bindPipeline(pipeline)
        }
        val commit = build.commit
        if (commit != null) {
            bindCommit(commit)
        }
    }

    private fun bindRunner(runner: Runner) {
        val runnerNum = String.format(getString(R.string.runner_number), runner.id.toString())
        textRunner.text = runnerNum
    }

    private fun bindPipeline(pipeline: Pipeline) {
        val pipelineNum = String.format(getString(R.string.build_pipeline), pipeline.id.toString())
        textPipeline.text = pipelineNum
    }

    private fun bindCommit(commit: RepositoryCommit) {
        val authorText = String.format(getString(R.string.build_commit_author), commit.authorName)
        textAuthor.text = authorText
        val messageText = String.format(getString(R.string.build_commit_message), commit.message)
        textMessage.text = messageText
    }

    @Suppress("unused")
    @Subscribe
    fun onBuildChangedEvent(event: BuildChangedEvent) {
        if (build.id == event.build.id) {
            build = event.build
            bindBuild(build)
        }
    }
}

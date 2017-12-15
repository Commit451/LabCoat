package com.commit451.gitlab.fragment

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import com.commit451.addendum.parceler.getParcelerParcelable
import com.commit451.addendum.parceler.putParcelerParcelable
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.event.BuildChangedEvent
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.*
import com.commit451.gitlab.rx.CustomSingleObserver
import com.commit451.gitlab.util.DateUtil
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber
import java.util.*

/**
 * Shows the details of a build
 */
class BuildDescriptionFragment : ButterKnifeFragment() {

    companion object {

        private val KEY_PROJECT = "project"
        private val KEY_BUILD = "build"

        fun newInstance(project: Project, build: Build): BuildDescriptionFragment {
            val fragment = BuildDescriptionFragment()
            val args = Bundle()
            args.putParcelerParcelable(KEY_PROJECT, project)
            args.putParcelerParcelable(KEY_BUILD, build)
            fragment.arguments = args
            return fragment
        }
    }

    @BindView(R.id.root) lateinit var root: ViewGroup
    @BindView(R.id.swipe_layout) lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.text_name) lateinit var textName: TextView
    @BindView(R.id.text_pipeline) lateinit var textPipeline: TextView
    @BindView(R.id.text_stage) lateinit var textStage: TextView
    @BindView(R.id.text_status) lateinit var textStatus: TextView
    @BindView(R.id.text_duration) lateinit var textDuration: TextView
    @BindView(R.id.text_created) lateinit var textCreated: TextView
    @BindView(R.id.text_finished) lateinit var textFinished: TextView
    @BindView(R.id.text_runner) lateinit var textRunner: TextView
    @BindView(R.id.text_ref) lateinit var textRef: TextView
    @BindView(R.id.text_author) lateinit var textAuthor: TextView
    @BindView(R.id.text_message) lateinit var textMessage: TextView

    lateinit var project: Project
    lateinit var build: Build

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = arguments?.getParcelerParcelable<Project>(KEY_PROJECT)!!
        build = arguments?.getParcelerParcelable<Build>(KEY_BUILD)!!
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
                .subscribe(object : CustomSingleObserver<Build>() {

                    override fun error(t: Throwable) {
                        Timber.e(t)
                        Snackbar.make(root, R.string.unable_to_load_build, Snackbar.LENGTH_LONG)
                                .show()
                    }

                    override fun success(build: Build) {
                        swipeRefreshLayout.isRefreshing = false
                        this@BuildDescriptionFragment.build = build
                        bindBuild(build)
                        App.bus().post(BuildChangedEvent(build))
                    }
                })
    }

    fun bindBuild(build: Build) {
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

    fun bindRunner(runner: Runner) {
        val runnerNum = String.format(getString(R.string.runner_number), runner.id.toString())
        textRunner.text = runnerNum
    }
    fun bindPipeline(pipeline: Pipeline) {
        val pipelineNum = String.format(getString(R.string.build_pipeline), pipeline.id.toString())
        textPipeline.text = pipelineNum
    }

    fun bindCommit(commit: RepositoryCommit) {
        val authorText = String.format(getString(R.string.build_commit_author), commit.authorName)
        textAuthor.text = authorText
        val messageText = String.format(getString(R.string.build_commit_message), commit.message)
        textMessage.text = messageText
    }

    @Subscribe
    fun onBuildChangedEvent(event: BuildChangedEvent) {
        if (build.id == event.build.id) {
            build = event.build
            bindBuild(build)
        }
    }
}

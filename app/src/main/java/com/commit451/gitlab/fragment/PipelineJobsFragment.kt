package com.commit451.gitlab.fragment

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import com.commit451.addendum.parceler.getParcelerParcelable
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.event.PipelineChangedEvent
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.CommitUser
import com.commit451.gitlab.model.api.Pipeline
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.rx.CustomSingleObserver
import com.commit451.gitlab.util.DateUtil
import org.greenrobot.eventbus.Subscribe
import org.parceler.Parcels
import timber.log.Timber
import java.util.*

/**
 * Shows the details of a pipeline
 */
class PipelineJobsFragment : ButterKnifeFragment() {

    companion object {

        private val KEY_PROJECT = "project"
        private val KEY_PIPELINE = "pipeline"

        fun newInstance(project: Project, pipeline: Pipeline): PipelineJobsFragment {
            val fragment = PipelineJobsFragment()
            val args = Bundle()
            args.putParcelable(KEY_PROJECT, Parcels.wrap(project))
            args.putParcelable(KEY_PIPELINE, Parcels.wrap(pipeline))
            fragment.arguments = args
            return fragment
        }
    }

    @BindView(R.id.root)
    lateinit var root: ViewGroup
    @BindView(R.id.swipe_layout)
    lateinit var swipeRefreshLayout: androidx.swiperefreshlayout.widget.SwipeRefreshLayout
    @BindView(R.id.text_number)
    lateinit var textName: TextView
    @BindView(R.id.text_status)
    lateinit var textStatus: TextView
    @BindView(R.id.text_duration)
    lateinit var textDuration: TextView
    @BindView(R.id.text_created)
    lateinit var textCreated: TextView
    @BindView(R.id.text_finished)
    lateinit var textFinished: TextView
    @BindView(R.id.text_ref)
    lateinit var textRef: TextView
    @BindView(R.id.text_sha)
    lateinit var textSha: TextView
    @BindView(R.id.text_author)
    lateinit var textAuthor: TextView
    @BindView(R.id.text_message)
    lateinit var textMessage: TextView

    lateinit var project: Project
    lateinit var pipeline: Pipeline

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = arguments?.getParcelerParcelable(KEY_PROJECT)!!
        pipeline = arguments?.getParcelerParcelable(KEY_PIPELINE)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pipeline_description, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipeRefreshLayout.setOnRefreshListener { load() }
        bindPipeline(pipeline)
        App.bus().register(this)
    }

    override fun onDestroyView() {
        App.bus().unregister(this)
        super.onDestroyView()
    }

    fun load() {
        App.get().gitLab.getPipeline(project.id, pipeline.id)
                .with(this)
                .subscribe(object : CustomSingleObserver<Pipeline>() {

                    override fun error(t: Throwable) {
                        Timber.e(t)
                        Snackbar.make(root, R.string.unable_to_load_pipeline, Snackbar.LENGTH_LONG)
                                .show()
                    }

                    override fun success(pipeline: Pipeline) {
                        swipeRefreshLayout.isRefreshing = false
                        this@PipelineJobsFragment.pipeline = pipeline
                        bindPipeline(pipeline)
                        App.bus().post(PipelineChangedEvent(pipeline))
                    }
                })
    }

    fun bindPipeline(pipeline: Pipeline) {
        var finishedTime: Date? = pipeline.finishedAt
        if (finishedTime == null) {
            finishedTime = Date()
        }
        var startedTime: Date? = pipeline.startedAt
        if (startedTime == null) {
            startedTime = Date()
        }
        val status = String.format(getString(R.string.pipeline_status), pipeline.status)
        textStatus.text = status

        val name = String.format(getString(R.string.pipeline_name), pipeline.id)
        textName.text = name

        val created = String.format(getString(R.string.build_created), DateUtil.getRelativeTimeSpanString(baseActivty, pipeline.createdAt))
        textCreated.text = created

        val finished = String.format(getString(R.string.pipeline_finished), pipeline.finishedAt)
        textFinished.text = finished

        val timeTaken = DateUtil.getTimeTaken(startedTime, finishedTime)
        val duration = String.format(getString(R.string.pipeline_duration), timeTaken)
        textDuration.text = duration

        val ref = String.format(getString(R.string.pipeline_ref), pipeline.ref)
        textRef.text = ref

        val sha = String.format(getString(R.string.pipeline_sha), pipeline.sha)
        textSha.text = sha


        if (pipeline.finishedAt != null) {
            val finished = String.format(getString(R.string.pipeline_finished), DateUtil.getRelativeTimeSpanString(baseActivty, pipeline.finishedAt))
            textFinished.text = finished
            textFinished.visibility = View.VISIBLE
        } else {
            textFinished.visibility = View.GONE
        }
        val user = pipeline.user
        if (user != null) {
            bindUser(user)
        }
    }

    fun bindUser(user: CommitUser) {
        val authorText = String.format(getString(R.string.pipeline_commit_author), user.name)
        textAuthor.text = authorText
    }

    @Suppress("unused")
    @Subscribe
    fun onPipelineChangedEvent(event: PipelineChangedEvent) {
        if (pipeline.id == event.pipeline.id) {
            pipeline = event.pipeline
            bindPipeline(pipeline)
        }
    }
}

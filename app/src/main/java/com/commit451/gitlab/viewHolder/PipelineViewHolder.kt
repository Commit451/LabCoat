package com.commit451.gitlab.viewHolder

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.Pipeline
import com.commit451.gitlab.util.DateUtil
import java.util.*

/**
 * Pipelines, woot
 */
class PipelineViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {

        fun inflate(parent: ViewGroup): PipelineViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_pipeline, parent, false)
            return PipelineViewHolder(view)
        }
    }

    @BindView(R.id.sha) lateinit var textSha: TextView
    @BindView(R.id.number) lateinit var textPipelineNumber: TextView
    @BindView(R.id.status) lateinit var textStatus: TextView
    @BindView(R.id.ref) lateinit var textRef: TextView

    init {
        ButterKnife.bind(this, view)
    }

    fun bind(pipeline: Pipeline) {
        val pipeSha = pipeline.sha.substring(0, 8)
        val pipelineShaText = String.format(itemView.resources.getString(R.string.pipeline_sha), pipeSha)
        textSha.text = pipelineShaText

        val pipelineNumberText = String.format(itemView.resources.getString(R.string.pipeline_number), pipeline.id)
        textPipelineNumber.text = pipelineNumberText

        val statusText = String.format(itemView.resources.getString(R.string.pipeline_status), pipeline.status)
        textStatus.text = statusText

        val refText = String.format(itemView.resources.getString(R.string.pipeline_ref), pipeline.ref)
        textRef.text = refText
    }
}

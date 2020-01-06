package com.commit451.gitlab.viewHolder

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.commit451.addendum.recyclerview.bindView
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.Pipeline

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

    private val textSha: TextView by bindView(R.id.sha)
    private val textPipelineNumber: TextView by bindView(R.id.number)
    private val textStatus: TextView by bindView(R.id.status)
    private val textRef: TextView by bindView(R.id.ref)

    fun bind(pipeline: Pipeline) {
        val pipeSha = pipeline.sha!!.substring(0, 8)
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

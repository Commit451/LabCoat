package com.commit451.gitlab.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import com.commit451.gitlab.model.api.Pipeline
import com.commit451.gitlab.viewHolder.PipelineViewHolder
import com.commit451.gitlab.viewHolder.LoadingFooterViewHolder
import java.util.*

/**
 * Pipelines adapter
 */
class PipelineAdapter(private val listener: PipelineAdapter.Listener) : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    companion object {

        private val FOOTER_COUNT = 1

        private val TYPE_ITEM = 0
        private val TYPE_FOOTER = 1
    }

    private val values: ArrayList<Pipeline> = ArrayList()
    private var loading = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_ITEM -> {
                val holder = PipelineViewHolder.inflate(parent)
                holder.itemView.setOnClickListener {
                    val position = holder.adapterPosition
                    listener.onPipelinesClicked(getValueAt(position))
                }
                return holder
            }
            TYPE_FOOTER -> return LoadingFooterViewHolder.inflate(parent)
        }
        throw IllegalStateException("No holder for view type " + viewType)
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        if (holder is PipelineViewHolder) {
            val pipeline = getValueAt(position)
            holder.bind(pipeline)
        } else if (holder is LoadingFooterViewHolder) {
            holder.bind(loading)
        } else {
            throw IllegalStateException("What is this holder?")
        }
    }

    override fun getItemCount(): Int {
        return values.size + FOOTER_COUNT
    }

    override fun getItemViewType(position: Int): Int {
        if (position == values.size) {
            return TYPE_FOOTER
        } else {
            return TYPE_ITEM
        }
    }

    fun setValues(values: Collection<Pipeline>?) {
        this.values.clear()
        addValues(values)
    }

    fun addValues(values: Collection<Pipeline>?) {
        if (values != null) {
            this.values.addAll(values)
        }
        notifyDataSetChanged()
    }

    fun updatePipeline(pipeline: Pipeline) {
        val indexToModify = values.indices.firstOrNull { values[it].id == pipeline.id }
        if (indexToModify != null) {
            values.removeAt(indexToModify)
            values.add(indexToModify, pipeline)
            notifyItemChanged(indexToModify)
        }
    }

    fun getValueAt(position: Int): Pipeline {
        return values[position]
    }

    fun setLoading(loading: Boolean) {
        this.loading = loading
        notifyItemChanged(values.size)
    }

    interface Listener {
        fun onPipelinesClicked(pipeline: Pipeline)
    }
}

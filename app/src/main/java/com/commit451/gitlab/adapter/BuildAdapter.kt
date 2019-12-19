package com.commit451.gitlab.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import com.commit451.gitlab.model.api.Build
import com.commit451.gitlab.viewHolder.BuildViewHolder
import com.commit451.gitlab.viewHolder.LoadingFooterViewHolder
import java.util.*

/**
 * Builds adapter
 */
class BuildAdapter(private val listener: Listener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {

        private const val FOOTER_COUNT = 1

        private const val TYPE_ITEM = 0
        private const val TYPE_FOOTER = 1
    }

    private val values: ArrayList<Build> = ArrayList()
    private var loading = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_ITEM -> {
                val holder = BuildViewHolder.inflate(parent)
                holder.itemView.setOnClickListener {
                    val position = holder.adapterPosition
                    listener.onBuildClicked(getValueAt(position))
                }
                return holder
            }
            TYPE_FOOTER -> return LoadingFooterViewHolder.inflate(parent)
        }
        throw IllegalStateException("No holder for view type $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is BuildViewHolder -> {
                val build = getValueAt(position)
                holder.bind(build)
            }
            is LoadingFooterViewHolder -> {
                holder.bind(loading)
            }
            else -> {
                throw IllegalStateException("What is this holder?")
            }
        }
    }

    override fun getItemCount(): Int {
        return values.size + FOOTER_COUNT
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == values.size) {
            TYPE_FOOTER
        } else {
            TYPE_ITEM
        }
    }

    fun setValues(values: Collection<Build>?) {
        this.values.clear()
        addValues(values)
    }

    fun addValues(values: Collection<Build>?) {
        if (values != null) {
            this.values.addAll(values)
        }
        notifyDataSetChanged()
    }

    fun updateBuild(build: Build) {
        val indexToModify = values.indices.firstOrNull { values[it].id == build.id }
        if (indexToModify != null) {
            values.removeAt(indexToModify)
            values.add(indexToModify, build)
            notifyItemChanged(indexToModify)
        }
    }

    fun getValueAt(position: Int): Build {
        return values[position]
    }

    fun setLoading(loading: Boolean) {
        this.loading = loading
        notifyItemChanged(values.size)
    }

    interface Listener {
        fun onBuildClicked(build: Build)
    }
}

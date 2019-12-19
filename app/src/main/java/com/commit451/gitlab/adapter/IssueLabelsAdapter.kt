package com.commit451.gitlab.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.commit451.gitlab.R
import com.commit451.gitlab.viewHolder.IssueLabelViewHolder
import java.util.*

/**
 * So many labels
 */
class IssueLabelsAdapter(private val listener: Listener) : RecyclerView.Adapter<IssueLabelViewHolder>() {

    private val values: ArrayList<String> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IssueLabelViewHolder {
        val holder = IssueLabelViewHolder.inflate(parent)
        holder.itemView.setOnClickListener { v ->
            val position = v.getTag(R.id.list_position) as Int
            listener.onLabelClicked(getEntry(position), holder)
        }
        return holder
    }

    override fun onBindViewHolder(holder: IssueLabelViewHolder, position: Int) {
        holder.itemView.setTag(R.id.list_position, position)
        holder.itemView.setTag(R.id.list_view_holder, holder)
        holder.bind(getEntry(position))
    }

    override fun getItemCount(): Int {
        return values.size
    }

    fun setLabels(labels: Collection<String>?) {
        values.clear()
        addLabels(labels)
    }

    fun addLabels(labels: Collection<String>?) {
        if (labels != null) {
            values.addAll(labels)
        }
        notifyDataSetChanged()
    }

    private fun getEntry(position: Int): String {
        return values[position]
    }

    interface Listener {
        fun onLabelClicked(label: String, viewHolder: IssueLabelViewHolder)
    }
}

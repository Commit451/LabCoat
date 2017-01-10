package com.commit451.gitlab.adapter

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.commit451.adapterlayout.AdapterLayout
import com.commit451.gitlab.model.api.Label
import com.commit451.gitlab.viewHolder.AddLabelViewHolder
import java.util.*

/**
 * So many labels
 */
class AddIssueLabelAdapter(private val listener: AddIssueLabelAdapter.Listener) : RecyclerView.Adapter<AddLabelViewHolder>() {

    private val values: ArrayList<Label> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddLabelViewHolder {
        val holder = AddLabelViewHolder.inflate(parent)
        holder.itemView.setOnClickListener {
            val position = AdapterLayout.getAdapterPosition(holder)
            val label = getEntry(position)
            listener.onLabelClicked(label)
        }
        return holder
    }

    override fun onBindViewHolder(holder: AddLabelViewHolder, position: Int) {
        holder.bind(getEntry(position))
    }

    override fun getItemCount(): Int {
        return values.size
    }

    fun setLabels(labels: Collection<Label>) {
        values.clear()
        addLabels(labels)
    }

    fun addLabels(labels: Collection<Label>?) {
        if (labels != null) {
            values.addAll(labels)
        }
        notifyDataSetChanged()
    }

    fun addLabel(label: Label) {
        values.add(label)
        notifyItemInserted(values.size - 1)
    }

    fun removeLabel(label: Label) {
        val indexOf = values.indexOf(label)
        values.removeAt(indexOf)
        notifyItemRemoved(indexOf)
    }

    fun containsLabel(label: Label): Boolean {
        return values.contains(label)
    }

    private fun getEntry(position: Int): Label {
        return values[position]
    }

    //Remove last ","
    fun getCommaSeperatedStringOfLabels(): String? {
        if (values.isEmpty()) {
            return null
        }
        var labels = ""
        for (label in values) {
            labels = labels + label.name + ","
        }
        //Remove last ","
        labels = labels.substring(0, labels.length - 1)
        return labels
    }

    interface Listener {
        fun onLabelClicked(label: Label)
    }
}

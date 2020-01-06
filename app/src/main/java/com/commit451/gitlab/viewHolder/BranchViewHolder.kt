package com.commit451.gitlab.viewHolder

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.commit451.addendum.recyclerview.bindView
import com.commit451.addendum.recyclerview.context
import com.commit451.addendum.themeAttrColor
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.Branch

/**
 * Label
 */
class BranchViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {

        fun inflate(parent: ViewGroup): BranchViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_branch, parent, false)
            return BranchViewHolder(view)
        }
    }

    private val textTitle: TextView by bindView(R.id.title)

    private var colorHighlighted: Int = 0

    init {
        colorHighlighted = context.themeAttrColor(R.attr.colorControlHighlight)
    }

    fun bind(branch: Branch, selected: Boolean) {
        textTitle.text = branch.name
        if (selected) {
            itemView.setBackgroundColor(colorHighlighted)
        } else {
            itemView.background = null
        }
    }
}

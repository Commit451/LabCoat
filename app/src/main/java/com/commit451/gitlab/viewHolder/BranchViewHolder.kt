package com.commit451.gitlab.viewHolder

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.easel.Easel
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

    @BindView(R.id.title) lateinit var textTitle: TextView

    var colorHighlighted: Int = 0

    init {
        ButterKnife.bind(this, view)
        colorHighlighted = Easel.getThemeAttrColor(itemView.context, R.attr.colorControlHighlight)
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
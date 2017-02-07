package com.commit451.gitlab.viewHolder

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.gitlab.R

/**
 * Shows the labels for an issue
 */
class IssueLabelViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {

        fun inflate(parent: ViewGroup): IssueLabelViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_issue_label, parent, false)
            return IssueLabelViewHolder(view)
        }
    }

    @BindView(R.id.title) lateinit var textTitle: TextView

    init {
        ButterKnife.bind(this, view)
    }

    fun bind(label: String) {
        textTitle.text = label
    }
}
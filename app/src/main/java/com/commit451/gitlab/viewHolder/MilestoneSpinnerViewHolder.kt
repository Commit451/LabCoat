package com.commit451.gitlab.viewHolder

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.Milestone

/**
 * Shows milestone in a spinner
 */
class MilestoneSpinnerViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {

        fun inflate(parent: ViewGroup): MilestoneSpinnerViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_spinner_milestone, parent, false)
            return MilestoneSpinnerViewHolder(view)
        }
    }

    @BindView(R.id.title)
    lateinit var textTitle: TextView

    init {
        ButterKnife.bind(this, view)
    }

    fun bind(milestone: Milestone?) {
        if (milestone == null) {
            textTitle.setText(R.string.no_milestone)
        } else {
            textTitle.text = milestone.title
        }
    }
}

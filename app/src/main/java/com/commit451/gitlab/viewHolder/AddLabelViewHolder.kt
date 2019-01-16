package com.commit451.gitlab.viewHolder

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.gitlab.R
import com.commit451.gitlab.extension.getColor
import com.commit451.gitlab.extension.getTitleColor
import com.commit451.gitlab.model.api.Label

/**
 * Shows the label on a screen where you can add labels
 */
class AddLabelViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {

    companion object {

        fun inflate(parent: ViewGroup): AddLabelViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_add_label, parent, false)
            return AddLabelViewHolder(view)
        }
    }

    @BindView(R.id.title)
    lateinit var textTitle: TextView

    init {
        ButterKnife.bind(this, view)
    }

    fun bind(label: Label) {
        textTitle.text = label.name
        textTitle.setTextColor(label.getTitleColor())
        textTitle.setBackgroundColor(label.getColor())
    }
}
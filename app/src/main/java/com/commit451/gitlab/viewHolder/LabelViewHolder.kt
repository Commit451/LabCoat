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
 * Label
 */
class LabelViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {

        fun inflate(parent: ViewGroup): LabelViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_label, parent, false)
            return LabelViewHolder(view)
        }
    }

    @BindView(R.id.title)
    lateinit var textTitle: TextView
    @BindView(R.id.color)
    lateinit var viewColor: View

    init {
        ButterKnife.bind(this, view)
    }

    fun bind(label: Label) {
        textTitle.text = label.name
        textTitle.setTextColor(label.getTitleColor())
        viewColor.setBackgroundColor(label.getColor())
    }
}

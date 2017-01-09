package com.commit451.gitlab.viewHolder

import android.graphics.drawable.ColorDrawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.gitlab.R

/**
 * Shows an access level
 */
class AccessViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {

        fun inflate(parent: ViewGroup): AccessViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_access, parent, false)
            return AccessViewHolder(view)
        }
    }

    @BindView(R.id.access) lateinit var textTitle: TextView

    init {
        ButterKnife.bind(this, view)
    }

    fun bind(access: String, colorSelected: Int, isSelected: Boolean) {
        textTitle.text = access
        (itemView as FrameLayout).foreground = if (isSelected) ColorDrawable(colorSelected) else null
    }
}

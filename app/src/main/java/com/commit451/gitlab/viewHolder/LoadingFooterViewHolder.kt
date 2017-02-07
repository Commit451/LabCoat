package com.commit451.gitlab.viewHolder

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.commit451.gitlab.R

/**
 * Footer to show loading in a RecyclerView
 */
class LoadingFooterViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {

        fun inflate(parent: ViewGroup): LoadingFooterViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.footer_loading, parent, false)
            return LoadingFooterViewHolder(view)
        }
    }

    fun bind(show: Boolean) {
        if (show) {
            itemView.visibility = View.VISIBLE
        } else {
            itemView.visibility = View.GONE
        }
    }
}

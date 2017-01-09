package com.commit451.gitlab.viewHolder

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.gitlab.R

/**
 * Breadcrumb view
 */
class BreadcrumbViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {

        fun inflate(parent: ViewGroup): BreadcrumbViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_breadcrumb, parent, false)
            return BreadcrumbViewHolder(view)
        }
    }

    @BindView(R.id.breadcrumb_text) lateinit var textBreadcrumb: TextView
    @BindView(R.id.breadcrumb_arrow) lateinit var buttonArrow: ImageView

    init {
        ButterKnife.bind(this, view)
    }

    fun bind(breadcrumb: String, showArrow: Boolean) {
        textBreadcrumb.text = breadcrumb
        if (showArrow) {
            buttonArrow.visibility = View.VISIBLE
        } else {
            buttonArrow.visibility = View.GONE
        }
    }
}

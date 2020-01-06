package com.commit451.gitlab.viewHolder

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.commit451.addendum.recyclerview.bindView
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

    private val textBreadcrumb: TextView by bindView(R.id.breadcrumb_text)
    private val buttonArrow: ImageView by bindView(R.id.breadcrumb_arrow)

    fun bind(breadcrumb: String, showArrow: Boolean) {
        textBreadcrumb.text = breadcrumb
        if (showArrow) {
            buttonArrow.visibility = View.VISIBLE
        } else {
            buttonArrow.visibility = View.GONE
        }
    }
}

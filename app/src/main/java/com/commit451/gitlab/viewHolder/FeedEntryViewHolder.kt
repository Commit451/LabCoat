package com.commit451.gitlab.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import coil.transform.CircleCropTransformation
import com.commit451.addendum.recyclerview.bindView
import com.commit451.gitlab.R
import com.commit451.gitlab.extension.formatAsHtml
import com.commit451.gitlab.model.rss.Entry
import com.commit451.gitlab.util.DateUtil

/**
 * Represents the view of an item in the RSS feed
 */
class FeedEntryViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {

        fun inflate(parent: ViewGroup): FeedEntryViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_entry, parent, false)
            return FeedEntryViewHolder(view)
        }
    }

    private val image: ImageView by bindView(R.id.image)
    private val textTitle: TextView by bindView(R.id.title)
    private val textSummary: TextView by bindView(R.id.description)
    private val textUpdated: TextView by bindView(R.id.updated)

    fun bind(entry: Entry) {
        image.load(entry.thumbnail.url) {
            transformations(CircleCropTransformation())
        }

        textTitle.text = entry.title.formatAsHtml()
        textSummary.text = entry.summary.formatAsHtml()
        val updatedRelative = DateUtil.getRelativeTimeSpanString(itemView.context, entry.updated)

        textUpdated.text = updatedRelative
    }
}

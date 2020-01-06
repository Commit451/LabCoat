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
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.extension.setMarkdownText
import com.commit451.gitlab.model.api.Note
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.util.DateUtil
import com.commit451.gitlab.util.ImageUtil
import com.commit451.gitlab.util.InternalLinkMovementMethod

/**
 * Notes, aka comments
 */
class NoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {

        fun inflate(parent: ViewGroup): NoteViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_note, parent, false)
            return NoteViewHolder(view)
        }
    }

    private val textTitle: TextView by bindView(R.id.title)
    private val textSummary: TextView by bindView(R.id.summary)
    private val textCreationDate: TextView by bindView(R.id.creation_date)
    private val imageAvatar: ImageView by bindView(R.id.icon)

    fun bind(note: Note, project: Project) {
        if (note.createdAt != null) {
            textCreationDate.text = DateUtil.getRelativeTimeSpanString(itemView.context, note.createdAt)
        }

        val author = note.author
        if (author != null) {
            textTitle.text = author.username
        }

        val summary = note.body ?: ""

        textSummary.setMarkdownText(summary, project)
        textSummary.movementMethod = InternalLinkMovementMethod(App.get().getAccount().serverUrl!!)
        imageAvatar.load(ImageUtil.getAvatarUrl(note.author, itemView.resources.getDimensionPixelSize(R.dimen.image_size))) {
            transformations(CircleCropTransformation())
        }
    }
}

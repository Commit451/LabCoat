package com.commit451.gitlab.viewHolder

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.extension.setMarkdownText
import com.commit451.gitlab.model.api.Note
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.transformation.CircleTransformation
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

    @BindView(R.id.title) lateinit var textTitle: TextView
    @BindView(R.id.summary) lateinit var textSummary: TextView
    @BindView(R.id.creation_date) lateinit var textCreationDate: TextView
    @BindView(R.id.icon) lateinit var imageAvatar: ImageView

    init {
        ButterKnife.bind(this, view)
    }

    fun bind(note: Note, project: Project) {
        if (note.createdAt != null) {
            textCreationDate.text = DateUtil.getRelativeTimeSpanString(itemView.context, note.createdAt)
        }

        if (note.author != null) {
            textTitle.text = note.author.username
        }

        var summary = ""
        if (note.body != null) {
            summary = note.body
        }

        textSummary.setMarkdownText(summary, project)
        textSummary.movementMethod = InternalLinkMovementMethod(App.get().getAccount().serverUrl)

        App.get().picasso
                .load(ImageUtil.getAvatarUrl(note.author, itemView.resources.getDimensionPixelSize(R.dimen.image_size)))
                .transform(CircleTransformation())
                .into(imageAvatar)
    }
}

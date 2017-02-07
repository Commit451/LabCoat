package com.commit451.gitlab.viewHolder

import android.graphics.Color
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.transformation.CircleTransformation
import com.github.ivbaranov.mli.MaterialLetterIcon
import com.squareup.picasso.Picasso

/**
 * Projects, yay!
 */
class ProjectViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {

        fun inflate(parent: ViewGroup): ProjectViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_project, parent, false)
            return ProjectViewHolder(view)
        }
    }

    @BindView(R.id.project_image) lateinit var image: ImageView
    @BindView(R.id.project_letter) lateinit var iconLetter: MaterialLetterIcon
    @BindView(R.id.project_title) lateinit var textTitle: TextView
    @BindView(R.id.project_description) lateinit var textDescription: TextView
    @BindView(R.id.project_visibility) lateinit var iconVisibility: ImageView

    init {
        ButterKnife.bind(this, view)
    }

    fun bind(project: Project, color: Int) {
        if (project.avatarUrl != null && project.avatarUrl != Uri.EMPTY) {
            iconLetter.visibility = View.GONE

            image.visibility = View.VISIBLE
            Picasso.with(itemView.context)
                    .load(project.avatarUrl)
                    .transform(CircleTransformation())
                    .into(image)
        } else {
            image.visibility = View.GONE

            iconLetter.visibility = View.VISIBLE
            iconLetter.letter = project.name.substring(0, 1)
            iconLetter.letterColor = Color.WHITE
            iconLetter.shapeColor = color
        }

        textTitle.text = project.nameWithNamespace
        if (!TextUtils.isEmpty(project.description)) {
            textDescription.visibility = View.VISIBLE
            textDescription.text = project.description
        } else {
            textDescription.visibility = View.GONE
            textDescription.text = ""
        }

        if (project.isPublic) {
            iconVisibility.setImageResource(R.drawable.ic_public_24dp)
        } else {
            iconVisibility.setImageResource(R.drawable.ic_private_24dp)
        }
    }
}

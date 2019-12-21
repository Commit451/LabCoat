package com.commit451.gitlab.viewHolder

import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.model.api.VISIBILITY_INTERNAL
import com.commit451.gitlab.model.api.VISIBILITY_PUBLIC
import com.commit451.gitlab.transformation.CircleTransformation
import com.github.ivbaranov.mli.MaterialLetterIcon

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

    @BindView(R.id.project_image)
    lateinit var image: ImageView
    @BindView(R.id.project_letter)
    lateinit var iconLetter: MaterialLetterIcon
    @BindView(R.id.project_title)
    lateinit var textTitle: TextView
    @BindView(R.id.project_description)
    lateinit var textDescription: TextView
    @BindView(R.id.project_visibility)
    lateinit var iconVisibility: ImageView

    init {
        ButterKnife.bind(this, view)
    }

    fun bind(project: Project, color: Int) {
        // There is no longer a way that we can load images for private
        // repos, unfortunately. Passing the access token as a param
        // no longer works. We just have to show the letter for private
        // repos
        if (project.avatarUrl.isNullOrBlank() || project.visibility != VISIBILITY_PUBLIC) {
            image.visibility = View.GONE

            iconLetter.visibility = View.VISIBLE
            iconLetter.letter = project.name!!.substring(0, 1)
            iconLetter.letterColor = Color.WHITE
            iconLetter.shapeColor = color
        } else {
            iconLetter.visibility = View.GONE

            image.visibility = View.VISIBLE
            App.get().picasso
                    .load(project.avatarUrl)
                    .transform(CircleTransformation())
                    .into(image)
        }

        textTitle.text = project.nameWithNamespace
        if (!project.description.isNullOrEmpty()) {
            textDescription.visibility = View.VISIBLE
            textDescription.text = project.description
        } else {
            textDescription.visibility = View.GONE
            textDescription.text = ""
        }

        val visibilityResource = when (project.visibility) {
            VISIBILITY_PUBLIC -> R.drawable.ic_public_24dp
            VISIBILITY_INTERNAL -> R.drawable.ic_lock_open_24dp
            else -> R.drawable.ic_private_24dp
        }
        iconVisibility.setImageResource(visibilityResource)
    }
}

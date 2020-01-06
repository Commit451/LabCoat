package com.commit451.gitlab.viewHolder

import android.graphics.Color
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
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.model.api.VISIBILITY_INTERNAL
import com.commit451.gitlab.model.api.VISIBILITY_PUBLIC
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

    private val image: ImageView by bindView(R.id.project_image)
    private val iconLetter: MaterialLetterIcon by bindView(R.id.project_letter)
    private val textTitle: TextView by bindView(R.id.project_title)
    private val textDescription: TextView by bindView(R.id.project_description)
    private val iconVisibility: ImageView by bindView(R.id.project_visibility)

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
            image.load(project.avatarUrl) {
                transformations(CircleCropTransformation())
            }
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

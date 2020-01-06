package com.commit451.gitlab.viewHolder

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.commit451.addendum.recyclerview.bindView
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.ProjectNamespace

/**
 * Shows a button to take you to a group
 */
class ProjectMemberFooterViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {

        fun inflate(parent: ViewGroup): ProjectMemberFooterViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.footer_project_member, parent, false)
            return ProjectMemberFooterViewHolder(view)
        }
    }

    private val button: Button by bindView(R.id.button)

    fun bind(namespace: ProjectNamespace) {
        button.text = String.format(itemView.resources.getString(R.string.group_members), namespace.name)
    }
}

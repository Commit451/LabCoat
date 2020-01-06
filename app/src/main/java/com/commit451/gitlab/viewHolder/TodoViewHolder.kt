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
import com.commit451.gitlab.model.api.Todo
import com.commit451.gitlab.util.DateUtil
import com.commit451.gitlab.util.ImageUtil

/**
 * issues, yay!
 */
class TodoViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {

        fun inflate(parent: ViewGroup): TodoViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_todo, parent, false)
            return TodoViewHolder(view)
        }
    }

    private val textProject: TextView by bindView(R.id.text_project)
    private val image: ImageView by bindView(R.id.issue_image)
    private val textMessage: TextView by bindView(R.id.issue_message)
    private val textCreator: TextView by bindView(R.id.issue_creator)

    fun bind(todo: Todo) {
        textProject.text = todo.project!!.nameWithNamespace
        if (todo.author != null) {
            image.load(ImageUtil.getAvatarUrl(todo.author, itemView.resources.getDimensionPixelSize(R.dimen.image_size))) {
                transformations(CircleCropTransformation())
            }
        } else {
            image.setImageBitmap(null)
        }

        textMessage.text = todo.body

        var time = ""
        if (todo.createdAt != null) {
            time += DateUtil.getRelativeTimeSpanString(itemView.context, todo.createdAt)
        }

        textCreator.text = time
    }
}

package com.commit451.gitlab.viewHolder

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
import com.commit451.gitlab.model.api.Todo
import com.commit451.gitlab.transformation.CircleTransformation
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

    @BindView(R.id.text_project)
    lateinit var textProject: TextView
    @BindView(R.id.issue_image)
    internal lateinit var image: ImageView
    @BindView(R.id.issue_message)
    internal lateinit var textMessage: TextView
    @BindView(R.id.issue_creator)
    internal lateinit var textCreator: TextView

    init {
        ButterKnife.bind(this, view)
    }

    fun bind(todo: Todo) {
        textProject.text = todo.project!!.nameWithNamespace
        if (todo.author != null) {
            App.get().picasso
                    .load(ImageUtil.getAvatarUrl(todo.author, itemView.resources.getDimensionPixelSize(R.dimen.image_size)))
                    .transform(CircleTransformation())
                    .into(image)
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

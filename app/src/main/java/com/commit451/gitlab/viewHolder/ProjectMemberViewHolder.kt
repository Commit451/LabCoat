package com.commit451.gitlab.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.commit451.addendum.recyclerview.bindView
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.User
import com.commit451.gitlab.util.ImageUtil

/**
 * Shows a project member
 */
class ProjectMemberViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {

        fun inflate(parent: ViewGroup): ProjectMemberViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_member_project, parent, false)
            return ProjectMemberViewHolder(view)
        }
    }

    private val buttonOverflow: View by bindView(R.id.overflow)
    private val textUsername: TextView by bindView(R.id.name)
    private val textAccess: TextView by bindView(R.id.access)
    val image: ImageView by bindView(R.id.image)

    val popupMenu: PopupMenu

    init {
        popupMenu = PopupMenu(itemView.context, buttonOverflow)
        popupMenu.menuInflater.inflate(R.menu.item_menu_project_member, popupMenu.menu)

        buttonOverflow.setOnClickListener { popupMenu.show() }
    }

    fun bind(member: User) {
        textUsername.text = member.username
        textAccess.text = User.getAccessLevel(member.accessLevel)
        image.load(ImageUtil.getAvatarUrl(member, itemView.resources.getDimensionPixelSize(R.dimen.user_header_image_size)))
    }
}

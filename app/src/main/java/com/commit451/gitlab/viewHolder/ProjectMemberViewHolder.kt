package com.commit451.gitlab.viewHolder

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.gitlab.App
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

    @BindView(R.id.overflow)
    lateinit var buttonOverflow: View
    @BindView(R.id.name)
    lateinit var textUsername: TextView
    @BindView(R.id.access)
    lateinit var textAccess: TextView
    @BindView(R.id.image)
    lateinit var image: ImageView

    val popupMenu: PopupMenu

    init {
        ButterKnife.bind(this, view)

        popupMenu = PopupMenu(itemView.context, buttonOverflow)
        popupMenu.menuInflater.inflate(R.menu.item_menu_project_member, popupMenu.menu)

        buttonOverflow.setOnClickListener { popupMenu.show() }
    }

    fun bind(member: User) {
        textUsername.text = member.username
        textAccess.text = User.getAccessLevel(member.accessLevel)

        App.get().picasso
                .load(ImageUtil.getAvatarUrl(member, itemView.resources.getDimensionPixelSize(R.dimen.user_header_image_size)))
                .into(image)
    }
}

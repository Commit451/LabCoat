package com.commit451.gitlab.viewHolder

import android.support.v7.widget.RecyclerView
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
import com.commit451.gitlab.model.Account
import com.commit451.gitlab.transformation.CircleTransformation
import com.commit451.gitlab.util.ImageUtil

/**
 * A signed in account
 */
class AccountViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {

        fun inflate(parent: ViewGroup): AccountViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_account, parent, false)
            return AccountViewHolder(view)
        }
    }

    @BindView(R.id.account_image) lateinit var image: ImageView
    @BindView(R.id.account_username) lateinit var textUsername: TextView
    @BindView(R.id.account_server) lateinit var textServer: TextView
    @BindView(R.id.account_more) lateinit var buttonMore: View

    val popupMenu: PopupMenu

    init {
        ButterKnife.bind(this, view)

        popupMenu = PopupMenu(itemView.context, buttonMore)
        popupMenu.menuInflater.inflate(R.menu.item_menu_account, popupMenu.menu)

        buttonMore.setOnClickListener { popupMenu.show() }
    }

    fun bind(account: Account, isActive: Boolean, colorSelected: Int) {
        textServer.text = account.serverUrl.toString()
        textUsername.text = account.user.username

        if (isActive) {
            itemView.setBackgroundColor(colorSelected)
        } else {
            itemView.background = null
        }

        App.get().picasso
                .load(ImageUtil.getAvatarUrl(account.user, itemView.resources.getDimensionPixelSize(R.dimen.user_list_image_size)))
                .transform(CircleTransformation())
                .into(image)
    }
}

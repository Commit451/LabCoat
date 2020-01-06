package com.commit451.gitlab.viewHolder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import coil.transform.CircleCropTransformation
import com.commit451.addendum.recyclerview.bindView
import com.commit451.gitlab.R
import com.commit451.gitlab.model.Account
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

    private val image: ImageView by bindView(R.id.account_image)
    private val textUsername: TextView by bindView(R.id.account_username)
    private val textServer: TextView by bindView(R.id.account_server)
    private val buttonMore: View by bindView(R.id.account_more)

    val popupMenu: PopupMenu

    init {
        popupMenu = PopupMenu(itemView.context, buttonMore)
        popupMenu.menuInflater.inflate(R.menu.logout, popupMenu.menu)

        buttonMore.setOnClickListener { popupMenu.show() }
    }

    fun bind(account: Account, isActive: Boolean, colorSelected: Int) {
        textServer.text = account.serverUrl.toString()
        textUsername.text = account.email

        if (isActive) {
            itemView.setBackgroundColor(colorSelected)
        } else {
            itemView.background = null
        }

        image.load(ImageUtil.getAvatarUrl(account.email, itemView.resources.getDimensionPixelSize(R.dimen.user_list_image_size))) {
            transformations(CircleCropTransformation())
        }
    }
}

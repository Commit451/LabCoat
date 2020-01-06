package com.commit451.gitlab.widget

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
import com.commit451.gitlab.model.Account
import com.commit451.gitlab.util.ImageUtil

/**
 * A signed in account
 */
class AccountViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {

        fun inflate(parent: ViewGroup): AccountViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.widget_item_account, parent, false)
            return AccountViewHolder(view)
        }
    }

    private val image: ImageView by bindView(R.id.image)
    private val textUsername: TextView by bindView(R.id.textUsername)
    private val textServer: TextView by bindView(R.id.textServer)

    fun bind(item: Account) {
        textServer.text = item.serverUrl.toString()
        textUsername.text = item.email

        image.load(ImageUtil.getAvatarUrl(item.email, itemView.resources.getDimensionPixelSize(R.dimen.user_list_image_size))) {
            transformations(CircleCropTransformation())
        }
    }
}

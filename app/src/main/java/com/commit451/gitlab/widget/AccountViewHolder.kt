package com.commit451.gitlab.widget

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.alexgwyn.recyclerviewsquire.TypedViewHolder
import com.commit451.gitlab.R
import com.commit451.gitlab.model.Account
import com.commit451.gitlab.transformation.CircleTransformation
import com.commit451.gitlab.util.ImageUtil
import com.squareup.picasso.Picasso

/**
 * A signed in account
 */
class AccountViewHolder(view: View) : TypedViewHolder<Account>(view) {

    companion object {

        fun inflate(parent: ViewGroup): AccountViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.widget_item_account, parent, false)
            return AccountViewHolder(view)
        }
    }

    @BindView(R.id.account_image) lateinit var image: ImageView
    @BindView(R.id.account_username) lateinit var textUsername: TextView
    @BindView(R.id.account_server) lateinit var textServer: TextView

    init {
        ButterKnife.bind(this, view)
    }

    override fun bind(position: Int, item: Account) {
        textServer.text = item.serverUrl.toString()
        textUsername.text = item.user.username

        Picasso.with(context)
                .load(ImageUtil.getAvatarUrl(item.user, itemView.resources.getDimensionPixelSize(R.dimen.user_list_image_size)))
                .transform(CircleTransformation())
                .into(image)
    }
}

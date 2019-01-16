package com.commit451.gitlab.widget

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.addendum.recyclerview.context
import com.commit451.gitlab.R
import com.commit451.gitlab.model.Account
import com.commit451.gitlab.transformation.CircleTransformation
import com.commit451.gitlab.util.ImageUtil
import com.squareup.picasso.Picasso

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

    @BindView(R.id.account_image)
    lateinit var image: ImageView
    @BindView(R.id.account_username)
    lateinit var textUsername: TextView
    @BindView(R.id.account_server)
    lateinit var textServer: TextView

    init {
        ButterKnife.bind(this, view)
    }

    fun bind(item: Account) {
        textServer.text = item.serverUrl.toString()
        textUsername.text = item.email

        Picasso.with(context)
                .load(ImageUtil.getAvatarUrl(item.email, itemView.resources.getDimensionPixelSize(R.dimen.user_list_image_size)))
                .transform(CircleTransformation())
                .into(image)
    }
}

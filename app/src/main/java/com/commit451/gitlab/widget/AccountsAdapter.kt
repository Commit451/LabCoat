package com.commit451.gitlab.widget

import android.view.ViewGroup

import com.alexgwyn.recyclerviewsquire.ClickableArrayAdapter
import com.commit451.gitlab.model.Account

/**
 * Adapter to show all the accounts
 */
class AccountsAdapter : ClickableArrayAdapter<Account, AccountViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        return AccountViewHolder.inflate(parent)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.bind(position, get(position))
    }
}

package com.commit451.gitlab.widget

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.commit451.gitlab.model.Account

/**
 * Adapter to show all the accounts
 */
class AccountsAdapter(private val listener: Listener) : RecyclerView.Adapter<AccountViewHolder>() {

    private var accounts = mutableListOf<Account>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val holder = AccountViewHolder.inflate(parent)
        holder.itemView.setOnClickListener {
            listener.onAccountClicked(accounts[holder.adapterPosition])
        }
        return holder
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        holder.bind(accounts[position])
    }

    override fun getItemCount(): Int {
        return accounts.size
    }

    fun clearAndFill(collection: Collection<Account>) {
        this.accounts.clear()
        this.accounts.addAll(collection)
        notifyDataSetChanged()
    }

    interface Listener {
        fun onAccountClicked(account: Account)
    }
}

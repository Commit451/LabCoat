package com.commit451.gitlab.widget;

import android.view.ViewGroup;

import com.alexgwyn.recyclerviewsquire.ClickableArrayAdapter;
import com.commit451.gitlab.model.Account;

/**
 * Adapter to show all the accounts
 */
public class AccountsAdapter extends ClickableArrayAdapter<Account, AccountViewHolder> {

    @Override
    public AccountViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return AccountViewHolder.inflate(parent);
    }

    @Override
    public void onBindViewHolder(AccountViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.bind(position, get(position));
    }
}

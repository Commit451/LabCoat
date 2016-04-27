package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.R;

import butterknife.ButterKnife;

/**
 * Shows the "add account" button
 */
public class AccountFooterViewHolder extends RecyclerView.ViewHolder {

    public static AccountFooterViewHolder inflate(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.footer_account, parent, false);
        return new AccountFooterViewHolder(view);
    }

    public AccountFooterViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }
}

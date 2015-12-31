package com.commit451.gitlab.viewHolder;

import com.commit451.gitlab.R;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;

/**
 * Shows the "add account" button
 * Created by Jawn on 12/6/2015.
 */
public class AccountFooterViewHolder extends RecyclerView.ViewHolder {

    public static AccountFooterViewHolder newInstance(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.footer_account, parent, false);
        return new AccountFooterViewHolder(view);
    }

    public AccountFooterViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }
}

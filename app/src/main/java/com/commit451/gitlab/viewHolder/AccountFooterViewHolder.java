package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.appthemeengine.ATE;
import com.commit451.gitlab.R;
import com.commit451.gitlab.util.AppThemeUtil;

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
        ATE.apply(view, AppThemeUtil.resolveThemeKey(view.getContext()));
    }
}

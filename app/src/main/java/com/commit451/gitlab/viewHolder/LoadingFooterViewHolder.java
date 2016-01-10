package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.R;

/**
 * Footer to show loading in a RecyclerView
 */
public class LoadingFooterViewHolder extends RecyclerView.ViewHolder {

    public static LoadingFooterViewHolder inflate(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.footer_loading, parent, false);
        return new LoadingFooterViewHolder(view);
    }

    public LoadingFooterViewHolder(View view) {
        super(view);
    }

    public void bind(boolean show) {
        if (show) {
            itemView.setVisibility(View.VISIBLE);
        } else {
            itemView.setVisibility(View.GONE);
        }
    }

}

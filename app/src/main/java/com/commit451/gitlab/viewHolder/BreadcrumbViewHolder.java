package com.commit451.gitlab.viewHolder;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.gitlab.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Breadcrumb view
 */
public class BreadcrumbViewHolder extends RecyclerView.ViewHolder {

    public static BreadcrumbViewHolder inflate(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_breadcrumb, parent, false);
        return new BreadcrumbViewHolder(view);
    }

    @Bind(R.id.breadcrumb_text) TextView mTextView;
    @Bind(R.id.breadcrumb_arrow) ImageView mArrowView;

    public BreadcrumbViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(String breadcrumb, boolean showArrow) {
        mTextView.setText(breadcrumb);
        if (showArrow) {
            mTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white_60));
            mArrowView.setVisibility(View.VISIBLE);
        } else {
            mTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
            mArrowView.setVisibility(View.GONE);
        }
    }
}

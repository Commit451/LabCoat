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
 * Created by Jawnnypoo on 11/22/2015.
 */
public class BreadcrumbViewHolder extends RecyclerView.ViewHolder {

    public static BreadcrumbViewHolder newInstance(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_breadcrumb, parent, false);
        return new BreadcrumbViewHolder(view);
    }

    @Bind(R.id.breadcrumb_text) TextView text;
    @Bind(R.id.breadcrumb_arrow) ImageView arrow;

    public BreadcrumbViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(String breadcrumb, boolean showArrow) {
        text.setText(breadcrumb);
        if (showArrow) {
            text.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white_60));
            arrow.setVisibility(View.VISIBLE);
        } else {
            text.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
            arrow.setVisibility(View.GONE);
        }
    }
}

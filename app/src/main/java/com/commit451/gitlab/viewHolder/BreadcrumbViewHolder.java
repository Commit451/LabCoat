package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.gitlab.R;

import butterknife.BindView;
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

    @BindView(R.id.breadcrumb_text)
    TextView textBreadcrumb;
    @BindView(R.id.breadcrumb_arrow)
    ImageView buttonArrow;

    public BreadcrumbViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(String breadcrumb, boolean showArrow) {
        textBreadcrumb.setText(breadcrumb);
        if (showArrow) {
            buttonArrow.setVisibility(View.VISIBLE);
        } else {
            buttonArrow.setVisibility(View.GONE);
        }
    }
}

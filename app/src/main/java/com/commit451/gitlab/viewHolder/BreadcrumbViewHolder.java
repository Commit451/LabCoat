package com.commit451.gitlab.viewHolder;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.util.TintHelper;
import com.commit451.gitlab.R;
import com.commit451.gitlab.util.AppThemeUtil;

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
        ATE.apply(view, AppThemeUtil.resolveThemeKey(view.getContext()));
        // We need to tint arrow based on text color
        TintHelper.setTint(mArrowView, mTextView.getTextColors().getDefaultColor());
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

package com.commit451.gitlab.viewHolder;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.util.Util;
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

    private int mPrimaryTextColor;
    private int mSecondaryTextColor;

    public BreadcrumbViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
        mPrimaryTextColor = Util.isColorLight(Config.primaryColor(view.getContext(),
                AppThemeUtil.resolveThemeKey(view.getContext()))) ? Color.BLACK : Color.WHITE;
        mSecondaryTextColor = Util.adjustAlpha(mPrimaryTextColor, 0.5f);
        // We need to tint arrow based on text color
        mArrowView.setColorFilter(mSecondaryTextColor, PorterDuff.Mode.SRC_IN);
    }

    public void bind(String breadcrumb, boolean showArrow) {
        mTextView.setText(breadcrumb);
        if (showArrow) {
            mTextView.setTextColor(mSecondaryTextColor);
            mArrowView.setVisibility(View.VISIBLE);
        } else {
            mTextView.setTextColor(mPrimaryTextColor);
            mArrowView.setVisibility(View.GONE);
        }
    }
}

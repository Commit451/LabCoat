package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.appthemeengine.ATE;
import com.commit451.gitlab.R;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.api.MergeRequest;
import com.commit451.gitlab.transformation.CircleTransformation;
import com.commit451.gitlab.util.AppThemeUtil;
import com.commit451.gitlab.util.ImageUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Represents a merge request within a list
 */
public class MergeRequestViewHolder extends RecyclerView.ViewHolder {

    public static MergeRequestViewHolder inflate(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_merge_request, parent, false);
        return new MergeRequestViewHolder(view);
    }

    @BindView(R.id.request_image) ImageView mImageView;
    @BindView(R.id.request_title) TextView mTitleView;
    @BindView(R.id.request_author) TextView mAuthorView;

    public MergeRequestViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(MergeRequest item) {
        GitLabClient.getPicasso()
                .load(ImageUtil.getAvatarUrl(item.getAuthor(), itemView.getResources().getDimensionPixelSize(R.dimen.image_size)))
                .transform(new CircleTransformation())
                .into(mImageView);

        if (item.getAuthor() != null) {
            mAuthorView.setText(item.getAuthor().getUsername());
        } else {
            mAuthorView.setText("");
        }

        if (item.getTitle() != null) {
            mTitleView.setText(item.getTitle());
        } else {
            mTitleView.setText("");
        }
    }
}

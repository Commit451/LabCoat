package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
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
import com.commit451.gitlab.util.DateUtils;
import com.commit451.gitlab.util.ImageUtil;
import com.commit451.gitlab.util.PicassoImageGetter;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.uncod.android.bypass.Bypass;

/**
 * Header that gives the details of a merge request
 */
public class MergeRequestHeaderViewHolder extends RecyclerView.ViewHolder {

    public static MergeRequestHeaderViewHolder inflate(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.header_merge_request, parent, false);
        return new MergeRequestHeaderViewHolder(view);
    }

    @Bind(R.id.description) TextView mDescriptionView;
    @Bind(R.id.author_image) ImageView mAuthorImageView;
    @Bind(R.id.author) TextView mAuthorView;

    public MergeRequestHeaderViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
        ATE.apply(view, AppThemeUtil.resolveThemeKey(view.getContext()));
    }

    public void bind(MergeRequest mergeRequest, Bypass bypass) {
        if (TextUtils.isEmpty(mergeRequest.getDescription())) {
            mDescriptionView.setVisibility(View.GONE);
        } else {
            mDescriptionView.setVisibility(View.VISIBLE);
            mDescriptionView.setText(bypass.markdownToSpannable(mergeRequest.getDescription(), new PicassoImageGetter(mDescriptionView, GitLabClient.getPicasso())));
            mDescriptionView.setMovementMethod(LinkMovementMethod.getInstance());
        }

        GitLabClient.getPicasso()
                .load(ImageUtil.getAvatarUrl(mergeRequest.getAuthor(), itemView.getResources().getDimensionPixelSize(R.dimen.image_size)))
                .transform(new CircleTransformation())
                .into(mAuthorImageView);

        String author = "";
        if (mergeRequest.getAuthor() != null) {
            author += mergeRequest.getAuthor().getName() + " ";
        }
        author += itemView.getResources().getString(R.string.created_issue);
        if (mergeRequest.getCreatedAt() != null) {
            author += " " + DateUtils.getRelativeTimeSpanString(itemView.getContext(), mergeRequest.getCreatedAt());
        }
        mAuthorView.setText(author);
    }
}

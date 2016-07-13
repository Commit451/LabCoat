package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.bypasspicassoimagegetter.BypassPicassoImageGetter;
import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.MergeRequest;
import com.commit451.gitlab.transformation.CircleTransformation;
import com.commit451.gitlab.util.DateUtils;
import com.commit451.gitlab.util.ImageUtil;

import butterknife.BindView;
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

    @BindView(R.id.description) TextView mDescriptionView;
    @BindView(R.id.author_image) ImageView mAuthorImageView;
    @BindView(R.id.author) TextView mAuthorView;

    public MergeRequestHeaderViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(MergeRequest mergeRequest, Bypass bypass) {
        if (TextUtils.isEmpty(mergeRequest.getDescription())) {
            mDescriptionView.setVisibility(View.GONE);
        } else {
            mDescriptionView.setVisibility(View.VISIBLE);
            mDescriptionView.setText(bypass.markdownToSpannable(mergeRequest.getDescription(), new BypassPicassoImageGetter(mDescriptionView, App.instance().getPicasso())));
            mDescriptionView.setMovementMethod(LinkMovementMethod.getInstance());
        }

        App.instance().getPicasso()
                .load(ImageUtil.getAvatarUrl(mergeRequest.getAuthor(), itemView.getResources().getDimensionPixelSize(R.dimen.image_size)))
                .transform(new CircleTransformation())
                .into(mAuthorImageView);

        String author = "";
        if (mergeRequest.getAuthor() != null) {
            author += mergeRequest.getAuthor().getName() + " ";
        }
        author += itemView.getResources().getString(R.string.created_merge_request);
        if (mergeRequest.getCreatedAt() != null) {
            author += " " + DateUtils.getRelativeTimeSpanString(itemView.getContext(), mergeRequest.getCreatedAt());
        }
        mAuthorView.setText(author);
    }
}

package com.commit451.gitlab.viewHolders;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.MergeRequest;
import com.commit451.gitlab.tools.ImageUtil;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.uncod.android.bypass.Bypass;

/**
 * Adapter for merge request detail
 * Created by John on 11/16/15.
 */
public class MergeRequestHeaderViewHolder extends RecyclerView.ViewHolder {

    public static IssueHeaderViewHolder newInstance(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.header_issue, parent, false);
        return new IssueHeaderViewHolder(view);
    }

    @Bind(R.id.description)
    TextView description;
    @Bind(R.id.author_image)
    ImageView authorImage;
    @Bind(R.id.author) TextView author;
    Bypass mBypass;

    public MergeRequestHeaderViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
        mBypass = new Bypass(view.getContext());
    }

    public void bind(MergeRequest mergeRequest) {
        if (TextUtils.isEmpty(mergeRequest.getDescription())) {
            description.setVisibility(View.GONE);
        } else {
            description.setVisibility(View.VISIBLE);
            description.setText(mBypass.markdownToSpannable(mergeRequest.getDescription()));
            description.setMovementMethod(LinkMovementMethod.getInstance());
        }

        if (mergeRequest.getAuthor() != null) {
            Picasso.with(itemView.getContext())
                    .load(ImageUtil.getGravatarUrl(mergeRequest.getAuthor(), itemView.getResources().getDimensionPixelSize(R.dimen.image_size)))
                    .into(authorImage);
            author.setText(mergeRequest.getAuthor().getName() + " "
                    + itemView.getResources().getString(R.string.created_issue) + " "
                    + DateUtils.getRelativeTimeSpanString(mergeRequest.getCreatedAt().getTime()));
        }
        if (mergeRequest.getCreatedAt() != null) {
            DateUtils.getRelativeTimeSpanString(mergeRequest.getCreatedAt().getTime());
        }
    }
}

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
import com.commit451.gitlab.model.Issue;
import com.commit451.gitlab.tools.ImageUtil;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.uncod.android.bypass.Bypass;

/**
 * Created by Jawn on 8/6/2015.
 */
public class IssueHeaderViewHolder extends RecyclerView.ViewHolder {

    public static IssueHeaderViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_header_issue, parent, false);
        return new IssueHeaderViewHolder(view);
    }

    @Bind(R.id.description) TextView description;
    @Bind(R.id.author_image) ImageView authorImage;
    @Bind(R.id.author) TextView author;
    Bypass mBypass;

    public IssueHeaderViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
        mBypass = new Bypass(view.getContext());
    }

    public void bind(Issue issue) {
        if (TextUtils.isEmpty(issue.getDescription())) {
            description.setVisibility(View.GONE);
        } else {
            description.setVisibility(View.VISIBLE);
            description.setText(mBypass.markdownToSpannable(issue.getDescription()));
            description.setMovementMethod(LinkMovementMethod.getInstance());
        }

        if (issue.getAuthor() != null) {
            Picasso.with(itemView.getContext())
                    .load(ImageUtil.getGravatarUrl(issue.getAuthor(), itemView.getResources().getDimensionPixelSize(R.dimen.image_size)))
                    .into(authorImage);
            author.setText(issue.getAuthor().getName() + " "
                    + itemView.getResources().getString(R.string.created_issue) + " "
                    + DateUtils.getRelativeTimeSpanString(issue.getCreatedAt().getTime()));
        }
        if (issue.getCreatedAt() != null) {
            DateUtils.getRelativeTimeSpanString(issue.getCreatedAt().getTime());
        }
    }
}
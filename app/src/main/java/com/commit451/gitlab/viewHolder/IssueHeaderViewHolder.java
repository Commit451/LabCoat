package com.commit451.gitlab.viewHolder;

import com.commit451.gitlab.R;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.api.Issue;
import com.commit451.gitlab.util.DateUtils;
import com.commit451.gitlab.util.ImageUtil;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.uncod.android.bypass.Bypass;

/**
 * Created by Jawn on 8/6/2015.
 */
public class IssueHeaderViewHolder extends RecyclerView.ViewHolder {

    public static IssueHeaderViewHolder newInstance(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.header_issue, parent, false);
        return new IssueHeaderViewHolder(view);
    }

    @Bind(R.id.description) TextView mDescriptionView;
    @Bind(R.id.author_image) ImageView mAuthorImageView;
    @Bind(R.id.author) TextView mAuthorView;

    private final Bypass mBypass;

    public IssueHeaderViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
        mBypass = new Bypass(view.getContext());
    }

    public void bind(Issue issue) {
        if (TextUtils.isEmpty(issue.getDescription())) {
            mDescriptionView.setVisibility(View.GONE);
        } else {
            mDescriptionView.setVisibility(View.VISIBLE);
            mDescriptionView.setText(mBypass.markdownToSpannable(issue.getDescription()));
            mDescriptionView.setMovementMethod(LinkMovementMethod.getInstance());
        }

        GitLabClient.getPicasso()
                .load(ImageUtil.getAvatarUrl(issue.getAuthor(), itemView.getResources().getDimensionPixelSize(R.dimen.image_size)))
                .into(mAuthorImageView);

        String author = "";
        if (issue.getAuthor() != null) {
            author = issue.getAuthor().getName() + " ";
        }
        author += itemView.getResources().getString(R.string.created_issue);
        if (issue.getCreatedAt() != null) {
            author += DateUtils.getRelativeTimeSpanString(itemView.getContext(), issue.getCreatedAt());
        }
        mAuthorView.setText(author);
    }
}
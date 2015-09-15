package com.commit451.gitlab.viewHolders;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.Issue;
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

    @Bind(R.id.title) TextView title;
    @Bind(R.id.description) TextView description;
    @Bind(R.id.author_image) ImageView authorImage;
    @Bind(R.id.author) TextView author;
    @Bind(R.id.date_added) TextView dateAdded;

    public IssueHeaderViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(Issue issue) {
        title.setText(issue.getTitle());
        Bypass bypass = new Bypass();
        String desc = issue.getDescription();
        if(desc == null) {
            desc = "";
        }
        description.setText(bypass.markdownToSpannable(desc));
        description.setMovementMethod(LinkMovementMethod.getInstance());
        if (issue.getAuthor() != null) {
            Picasso.with(itemView.getContext())
                    .load(issue.getAuthor().getAvatarUrl())
                    .into(authorImage);
            author.setText(issue.getAuthor().getName());
        }
        if (issue.getCreatedAt() != null) {
            DateUtils.getRelativeTimeSpanString(issue.getCreatedAt().getTime());
        }
    }
}
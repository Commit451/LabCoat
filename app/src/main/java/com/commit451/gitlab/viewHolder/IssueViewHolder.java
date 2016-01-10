package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.api.Issue;
import com.commit451.gitlab.util.DateUtils;
import com.commit451.gitlab.util.ImageUtil;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * issues, yay!
 */
public class IssueViewHolder extends RecyclerView.ViewHolder {

    public static IssueViewHolder inflate(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_issue, parent, false);
        return new IssueViewHolder(view);
    }

    @Bind(R.id.issue_image) ImageView mImageView;
    @Bind(R.id.issue_message) TextView mMessageView;
    @Bind(R.id.issue_creator) TextView mCreatorView;

    public IssueViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(Issue issue) {
        long tempId = issue.getIid();
        if (tempId < 1) {
            tempId = issue.getId();
        }

        GitLabClient.getPicasso()
                .load(ImageUtil.getAvatarUrl(issue.getAssignee(), itemView.getResources().getDimensionPixelSize(R.dimen.image_size)))
                .into(mImageView);

        mMessageView.setText("#" + tempId + ": " + issue.getTitle());

        String time = "";
        if (issue.getCreatedAt() != null) {
            time += DateUtils.getRelativeTimeSpanString(itemView.getContext(), issue.getCreatedAt());
        }
        String author = "";
        if (issue.getAuthor() != null) {
            author += issue.getAuthor().getUsername();
        }

        mCreatorView.setText(String.format(itemView.getContext().getString(R.string.created_time), time, author));
    }
}

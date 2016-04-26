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
import com.commit451.gitlab.model.api.Issue;
import com.commit451.gitlab.transformation.CircleTransformation;
import com.commit451.gitlab.util.AppThemeUtil;
import com.commit451.gitlab.util.DateUtils;
import com.commit451.gitlab.util.ImageUtil;

import butterknife.BindView;
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

    @BindView(R.id.issue_image) ImageView mImageView;
    @BindView(R.id.issue_message) TextView mMessageView;
    @BindView(R.id.issue_creator) TextView mCreatorView;

    public IssueViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(Issue issue) {

        if (issue.getAssignee() != null) {
            GitLabClient.getPicasso()
                    .load(ImageUtil.getAvatarUrl(issue.getAssignee(), itemView.getResources().getDimensionPixelSize(R.dimen.image_size)))
                    .transform(new CircleTransformation())
                    .into(mImageView);
        } else {
            mImageView.setImageBitmap(null);
        }

        mMessageView.setText(issue.getTitle());

        String time = "";
        if (issue.getCreatedAt() != null) {
            time += DateUtils.getRelativeTimeSpanString(itemView.getContext(), issue.getCreatedAt());
        }
        String author = "";
        if (issue.getAuthor() != null) {
            author += issue.getAuthor().getUsername();
        }
        String id = "";
        long issueId = issue.getIid();
        if (issueId < 1) {
            issueId = issue.getId();
        }
        id = "#" + issueId;

        mCreatorView.setText(String.format(itemView.getContext().getString(R.string.opened_time), id, time, author));
    }
}

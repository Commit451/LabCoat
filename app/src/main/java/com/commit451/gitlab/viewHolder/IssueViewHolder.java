package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Issue;
import com.commit451.gitlab.transformation.CircleTransformation;
import com.commit451.gitlab.util.DateUtil;
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

    @BindView(R.id.issue_state)
    TextView textState;
    @BindView(R.id.issue_image)
    ImageView image;
    @BindView(R.id.issue_message)
    TextView textMessage;
    @BindView(R.id.issue_creator)
    TextView textCreator;

    public IssueViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(Issue issue) {

        switch (issue.getState()) {
            case Issue.STATE_OPENED:
                textState.setText(itemView.getResources().getString(R.string.issue_open));
                break;
            case Issue.STATE_CLOSED:
                textState.setText(itemView.getResources().getString(R.string.issue_closed));
                break;
            default:
                textState.setVisibility(View.GONE);
                break;
        }

        if (issue.getAssignee() != null) {
            App.get().getPicasso()
                    .load(ImageUtil.getAvatarUrl(issue.getAssignee(), itemView.getResources().getDimensionPixelSize(R.dimen.image_size)))
                    .transform(new CircleTransformation())
                    .into(image);
        } else {
            image.setImageBitmap(null);
        }

        textMessage.setText(issue.getTitle());

        String time = "";
        if (issue.getCreatedAt() != null) {
            time += DateUtil.getRelativeTimeSpanString(itemView.getContext(), issue.getCreatedAt());
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

        textCreator.setText(String.format(itemView.getContext().getString(R.string.opened_time), id, time, author));
    }
}

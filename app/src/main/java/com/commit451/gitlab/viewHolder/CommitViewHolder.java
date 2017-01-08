package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.RepositoryCommit;
import com.commit451.gitlab.transformation.CircleTransformation;
import com.commit451.gitlab.util.DateUtil;
import com.commit451.gitlab.util.ImageUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Shows a commit
 */
public class CommitViewHolder extends RecyclerView.ViewHolder {

    public static CommitViewHolder inflate(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_commit, parent, false);
        return new CommitViewHolder(view);
    }

    @BindView(R.id.commit_image)
    ImageView image;
    @BindView(R.id.commit_message)
    TextView textMessage;
    @BindView(R.id.commit_author)
    TextView textAuthor;
    @BindView(R.id.commit_time)
    TextView textTime;

    public CommitViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(RepositoryCommit commit) {
        App.get().getPicasso()
                .load(ImageUtil.getAvatarUrl(commit.getAuthorEmail(), itemView.getResources().getDimensionPixelSize(R.dimen.image_size)))
                .transform(new CircleTransformation())
                .into(image);

        textMessage.setText(commit.getTitle());
        textAuthor.setText(commit.getAuthorName());
        if (commit.getCreatedAt() != null) {
            textTime.setText(DateUtil.getRelativeTimeSpanString(itemView.getContext(), commit.getCreatedAt()));
        } else {
            textTime.setText(R.string.unknown);
        }
    }
}

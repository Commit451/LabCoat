package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.api.Note;
import com.commit451.gitlab.transformation.CircleTransformation;
import com.commit451.gitlab.util.DateUtils;
import com.commit451.gitlab.util.ImageUtil;
import com.commit451.gitlab.util.PicassoImageGetter;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.uncod.android.bypass.Bypass;

/**
 * Notes, aka comments
 */
public class NoteViewHolder extends RecyclerView.ViewHolder {

    public static NoteViewHolder inflate(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Bind(R.id.title) TextView mTitleView;
    @Bind(R.id.summary) TextView mSummaryView;
    @Bind(R.id.creation_date) TextView mCreationDateView;
    @Bind(R.id.icon) ImageView mIconView;

    public NoteViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(Note note, Bypass bypass) {
        if (note.getCreatedAt() != null) {
            mCreationDateView.setText(DateUtils.getRelativeTimeSpanString(itemView.getContext(), note.getCreatedAt()));
        }

        if (note.getAuthor() != null) {
            mTitleView.setText(note.getAuthor().getUsername());
        }

        String summary = "";
        if (note.getBody() != null) {
            summary = note.getBody();
        }

        mSummaryView.setText(bypass.markdownToSpannable(summary, new PicassoImageGetter(mSummaryView, GitLabClient.getPicasso())));
        mSummaryView.setMovementMethod(LinkMovementMethod.getInstance());

        GitLabClient.getPicasso()
                .load(ImageUtil.getAvatarUrl(note.getAuthor(), itemView.getResources().getDimensionPixelSize(R.dimen.image_size)))
                .transform(new CircleTransformation())
                .into(mIconView);
    }
}

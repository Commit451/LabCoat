package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Todo;
import com.commit451.gitlab.transformation.CircleTransformation;
import com.commit451.gitlab.util.DateUtil;
import com.commit451.gitlab.util.ImageUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * issues, yay!
 */
public class TodoViewHolder extends RecyclerView.ViewHolder {

    public static TodoViewHolder inflate(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_todo, parent, false);
        return new TodoViewHolder(view);
    }

    @BindView(R.id.text_project)
    TextView mTextProject;
    @BindView(R.id.issue_image)
    ImageView mImageView;
    @BindView(R.id.issue_message)
    TextView mMessageView;
    @BindView(R.id.issue_creator)
    TextView mCreatorView;

    public TodoViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(Todo todo) {

        mTextProject.setText(todo.getProject().getNameWithNamespace());
        if (todo.getAuthor() != null) {
            App.get().getPicasso()
                    .load(ImageUtil.getAvatarUrl(todo.getAuthor(), itemView.getResources().getDimensionPixelSize(R.dimen.image_size)))
                    .transform(new CircleTransformation())
                    .into(mImageView);
        } else {
            mImageView.setImageBitmap(null);
        }

        mMessageView.setText(todo.getBody());

        String time = "";
        if (todo.getCreatedAt() != null) {
            time += DateUtil.getRelativeTimeSpanString(itemView.getContext(), todo.getCreatedAt());
        }

        mCreatorView.setText(time);
    }
}

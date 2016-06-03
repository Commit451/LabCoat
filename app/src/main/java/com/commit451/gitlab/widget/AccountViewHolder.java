package com.commit451.gitlab.widget;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alexgwyn.recyclerviewsquire.TypedViewHolder;
import com.commit451.gitlab.R;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.Account;
import com.commit451.gitlab.transformation.CircleTransformation;
import com.commit451.gitlab.util.ImageUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A signed in account
 */
public class AccountViewHolder extends TypedViewHolder<Account> {

    public static AccountViewHolder inflate(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.widget_item_account, parent, false);
        return new AccountViewHolder(view);
    }

    @BindView(R.id.account_image) ImageView mImageView;
    @BindView(R.id.account_username) TextView mUsernameView;
    @BindView(R.id.account_server) TextView mServerView;

    public AccountViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    @Override
    public void bind(int position, Account item) {
        mServerView.setText(item.getServerUrl().toString());
        mUsernameView.setText(item.getUser().getUsername());

        GitLabClient.getPicasso()
                .load(ImageUtil.getAvatarUrl(item.getUser(), itemView.getResources().getDimensionPixelSize(R.dimen.user_list_image_size)))
                .transform(new CircleTransformation())
                .into(mImageView);
    }
}

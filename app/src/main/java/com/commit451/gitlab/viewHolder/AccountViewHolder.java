package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.Account;
import com.commit451.gitlab.transformation.CircleTransformation;
import com.commit451.gitlab.util.ImageUtil;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A signed in account
 */
public class AccountViewHolder extends RecyclerView.ViewHolder{

    public static AccountViewHolder inflate(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_account, parent, false);
        return new AccountViewHolder(view);
    }

    @Bind(R.id.account_image) ImageView mImageView;
    @Bind(R.id.account_username) TextView mUsernameView;
    @Bind(R.id.account_server) TextView mServerView;
    @Bind(R.id.account_more) View mMoreView;

    public final PopupMenu mPopupMenu;

    public AccountViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);

        mPopupMenu = new PopupMenu(itemView.getContext(), mMoreView);
        mPopupMenu.getMenuInflater().inflate(R.menu.item_menu_account, mPopupMenu.getMenu());

        mMoreView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupMenu.show();
            }
        });
    }

    public void bind(Account account, boolean isActive, int colorSelected) {
        mServerView.setText(account.getServerUrl().toString());
        mUsernameView.setText(account.getUser().getUsername());

        if (isActive) {
            itemView.setBackgroundColor(colorSelected);
        } else {
            itemView.setBackground(null);
        }

        GitLabClient.getPicasso()
                .load(ImageUtil.getAvatarUrl(account.getUser(), itemView.getResources().getDimensionPixelSize(R.dimen.user_list_image_size)))
                .transform(new CircleTransformation())
                .into(mImageView);
    }
}

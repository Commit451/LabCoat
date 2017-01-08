package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.model.Account;
import com.commit451.gitlab.transformation.CircleTransformation;
import com.commit451.gitlab.util.ImageUtil;

import butterknife.BindView;
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

    @BindView(R.id.account_image) ImageView image;
    @BindView(R.id.account_username) TextView textUsername;
    @BindView(R.id.account_server) TextView textServer;
    @BindView(R.id.account_more) View buttonMore;

    public final PopupMenu mPopupMenu;

    public AccountViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);

        mPopupMenu = new PopupMenu(itemView.getContext(), buttonMore);
        mPopupMenu.getMenuInflater().inflate(R.menu.item_menu_account, mPopupMenu.getMenu());

        buttonMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupMenu.show();
            }
        });
    }

    public void bind(Account account, boolean isActive, int colorSelected) {
        textServer.setText(account.getServerUrl().toString());
        textUsername.setText(account.getUser().getUsername());

        if (isActive) {
            itemView.setBackgroundColor(colorSelected);
        } else {
            itemView.setBackground(null);
        }

        App.get().getPicasso()
                .load(ImageUtil.getAvatarUrl(account.getUser(), itemView.getResources().getDimensionPixelSize(R.dimen.user_list_image_size)))
                .transform(new CircleTransformation())
                .into(image);
    }
}

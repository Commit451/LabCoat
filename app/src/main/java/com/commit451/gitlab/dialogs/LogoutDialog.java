package com.commit451.gitlab.dialogs;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatDialog;

import com.commit451.gitlab.GitLabApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.events.LogoutEvent;
import com.commit451.gitlab.tools.NavigationManager;
import com.commit451.gitlab.tools.Prefs;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Log out confirmation
 * Created by Jawn on 8/21/2015.
 */
public class LogoutDialog extends AppCompatDialog {

    @OnClick(R.id.logout_button)
    void onLogoutClick() {
        Prefs.setLoggedIn(getContext(), false);
        Prefs.setPrivateToken(getContext(), null);
        NavigationManager.navigateToLogin((Activity) getContext());
        GitLabApp.bus().post(new LogoutEvent());
    }

    @OnClick(R.id.cancel_button)
    void onCancelClick() {
        dismiss();
    }

    public LogoutDialog(Context context) {
        super(context);
        setContentView(R.layout.dialog_logout);
        ButterKnife.bind(this);
    }
}

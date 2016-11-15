package com.commit451.gitlab.widget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.alexgwyn.recyclerviewsquire.ClickableArrayAdapter;
import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.activity.BaseActivity;
import com.commit451.gitlab.model.Account;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * The configuration screen for the ExampleAppWidgetProvider widget sample.
 */
public class UserFeedWidgetConfigureActivity extends BaseActivity {

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.message_text)
    TextView mTextMessage;
    @BindView(R.id.list)
    RecyclerView mList;
    AccountsAdapter mAccountAdapter;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.activity_feed_widget_configure);
        ButterKnife.bind(this);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        // If they gave us an intent without the widget id, just bail.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        mToolbar.setTitle(R.string.widget_choose_account);

        mAccountAdapter = new AccountsAdapter();
        mAccountAdapter.setOnItemClickListener(new ClickableArrayAdapter.OnItemClickListener<Account>() {
            @Override
            public void onItemClicked(ClickableArrayAdapter<Account, ?> adapter, View view, int position) {
                saveWidgetConfig(adapter.get(position));
            }
        });
        mList.setLayoutManager(new LinearLayoutManager(this));
        mList.setAdapter(mAccountAdapter);

        loadAccounts();
    }

    private void loadAccounts() {
        List<Account> accounts = App.get().getPrefs().getAccounts();
        Timber.d("Got %s accounts", accounts.size());
        Collections.sort(accounts);
        Collections.reverse(accounts);
        if (accounts.isEmpty()) {
            mTextMessage.setVisibility(View.VISIBLE);
        } else {
            mTextMessage.setVisibility(View.GONE);
            mAccountAdapter.clearAndFill(accounts);
        }
    }

    private void saveWidgetConfig(Account account) {
        UserFeedWidgetPrefs.setAccount(UserFeedWidgetConfigureActivity.this, mAppWidgetId, account);
        // Push widget update to surface with newly set prefix
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(UserFeedWidgetConfigureActivity.this);
//        ExampleAppWidgetProvider.updateAppWidget(context, appWidgetManager,
//                mAppWidgetId, titlePrefix);
        // Make sure we pass back the original appWidgetId
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

}

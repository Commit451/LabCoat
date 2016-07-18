package com.commit451.gitlab.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.easycallback.EasyCallback;
import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.LabelAdapter;
import com.commit451.gitlab.model.api.Label;
import com.commit451.gitlab.navigation.Navigator;
import com.commit451.gitlab.viewHolder.LabelViewHolder;

import org.parceler.Parcels;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Add labels!
 */
public class AddLabelActivity extends BaseActivity {

    private static final String KEY_PROJECT_ID = "project_id";
    private static final int REQUEST_NEW_LABEL = 1;

    public static final String KEY_LABEL = "label";

    public static Intent newIntent(Context context, long projectId) {
        Intent intent = new Intent(context, AddLabelActivity.class);
        intent.putExtra(KEY_PROJECT_ID, projectId);
        return intent;
    }

    @BindView(R.id.root)
    ViewGroup mRoot;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.list)
    RecyclerView mList;
    LabelAdapter mLabelAdapter;
    @BindView(R.id.message_text)
    TextView mTextMessage;

    long mProjectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_label);
        ButterKnife.bind(this);

        mProjectId = getIntent().getLongExtra(KEY_PROJECT_ID, -1);
        mToolbar.setTitle(R.string.labels);
        mToolbar.inflateMenu(R.menu.menu_add_label);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_add_label:
                        Navigator.navigateToAddNewLabel(AddLabelActivity.this, mProjectId, REQUEST_NEW_LABEL);
                        return true;
                }
                return false;
            }
        });
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                load();
            }
        });
        mLabelAdapter = new LabelAdapter(new LabelAdapter.Listener() {
            @Override
            public void onLabelClicked(Label label, LabelViewHolder viewHolder) {
                Intent data = new Intent();
                data.putExtra(KEY_LABEL, Parcels.wrap(label));
                setResult(RESULT_OK, data);
                finish();
            }
        });
        mList.setAdapter(mLabelAdapter);
        mList.setLayoutManager(new LinearLayoutManager(this));

        mToolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        load();
    }

    private void load() {
        mTextMessage.setVisibility(View.GONE);
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            }
        });
        App.instance().getGitLab().getLabels(mProjectId).enqueue(new EasyCallback<List<Label>>() {
            @Override
            public void success(@NonNull List<Label> response) {
                mSwipeRefreshLayout.setRefreshing(false);
                if (response.isEmpty()) {
                    mTextMessage.setVisibility(View.VISIBLE);
                }
                mLabelAdapter.setItems(response);
            }

            @Override
            public void failure(Throwable t) {
                mSwipeRefreshLayout.setRefreshing(false);
                mTextMessage.setVisibility(View.VISIBLE);
                Timber.e(t, null);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_NEW_LABEL:
                if (resultCode == RESULT_OK) {
                    Label newLabel = Parcels.unwrap(data.getParcelableExtra(AddNewLabelActivity.KEY_NEW_LABEL));
                    mLabelAdapter.addLabel(newLabel);
                }
                break;
        }
    }
}

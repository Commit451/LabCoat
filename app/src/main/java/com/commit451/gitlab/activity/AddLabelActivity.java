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

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.LabelAdapter;
import com.commit451.gitlab.model.api.Label;
import com.commit451.gitlab.navigation.Navigator;
import com.commit451.gitlab.rx.CustomSingleObserver;
import com.commit451.gitlab.viewHolder.LabelViewHolder;

import org.parceler.Parcels;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static com.commit451.gitlab.R.string.labels;

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
    ViewGroup root;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.list)
    RecyclerView list;
    LabelAdapter adapterLabel;
    @BindView(R.id.message_text)
    TextView textMessage;

    long projectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_label);
        ButterKnife.bind(this);

        projectId = getIntent().getLongExtra(KEY_PROJECT_ID, -1);
        toolbar.setTitle(labels);
        toolbar.inflateMenu(R.menu.menu_add_label);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_add_label:
                        Navigator.navigateToAddNewLabel(AddLabelActivity.this, projectId, REQUEST_NEW_LABEL);
                        return true;
                }
                return false;
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                load();
            }
        });
        adapterLabel = new LabelAdapter(new LabelAdapter.Listener() {
            @Override
            public void onLabelClicked(Label label, LabelViewHolder viewHolder) {
                Intent data = new Intent();
                data.putExtra(KEY_LABEL, Parcels.wrap(label));
                setResult(RESULT_OK, data);
                finish();
            }
        });
        list.setAdapter(adapterLabel);
        list.setLayoutManager(new LinearLayoutManager(this));

        toolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        load();
    }

    private void load() {
        textMessage.setVisibility(View.GONE);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(true);
                }
            }
        });
        App.get().getGitLab().getLabels(projectId)
                .compose(this.<List<Label>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomSingleObserver<List<Label>>() {

                    @Override
                    public void error(@NonNull Throwable t) {
                        Timber.e(t);
                        swipeRefreshLayout.setRefreshing(false);
                        textMessage.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void success(@NonNull List<Label> labels) {
                        swipeRefreshLayout.setRefreshing(false);
                        if (labels.isEmpty()) {
                            textMessage.setVisibility(View.VISIBLE);
                        }
                        adapterLabel.setItems(labels);
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
                    adapterLabel.addLabel(newLabel);
                }
                break;
        }
    }
}

package com.commit451.gitlab.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.MergeRequestDetailAdapter;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.api.MergeRequest;
import com.commit451.gitlab.model.api.Note;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.util.KeyboardUtil;
import com.commit451.gitlab.util.PaginationUtil;

import org.parceler.Parcels;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

/**
 * Shows the details of a merge request
 */
public class MergeRequestActivity extends BaseActivity {

    private static final String KEY_PROJECT = "key_project";
    private static final String KEY_MERGE_REQUEST = "key_merge_request";

    public static Intent newInstance(Context context, Project project, MergeRequest mergeRequest) {
        Intent intent = new Intent(context, MergeRequestActivity.class);
        intent.putExtra(KEY_PROJECT, Parcels.wrap(project));
        intent.putExtra(KEY_MERGE_REQUEST, Parcels.wrap(mergeRequest));
        return intent;
    }

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.merge_request_title) TextView mMergeRequestTitle;
    @Bind(R.id.swipe_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.list) RecyclerView mNotesRecyclerView;
    @Bind(R.id.new_note_edit) EditText mNewNoteEdit;
    @Bind(R.id.progress) View mProgress;
    @OnClick(R.id.new_note_button)
    public void onNewNoteClick() {
        postNote();
    }

    private MergeRequestDetailAdapter mMergeRequestDetailAdapter;
    private LinearLayoutManager mNotesLinearLayoutManager;

    private Project mProject;
    private MergeRequest mMergeRequest;
    private Uri mNextPageUrl;
    private boolean mLoading;

    private final RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = mNotesLinearLayoutManager.getChildCount();
            int totalItemCount = mNotesLinearLayoutManager.getItemCount();
            int firstVisibleItem = mNotesLinearLayoutManager.findFirstVisibleItemPosition();
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !mLoading && mNextPageUrl != null) {
                loadMoreNotes();
            }
        }
    };

    private Callback<List<Note>> mNotesCallback = new Callback<List<Note>>() {

        @Override
        public void onResponse(Response<List<Note>> response, Retrofit retrofit) {
            mSwipeRefreshLayout.setRefreshing(false);
            mLoading = false;
            if (!response.isSuccess()) {
                Snackbar.make(getWindow().getDecorView(), getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                        .show();
                return;
            }
            mNextPageUrl = PaginationUtil.parse(response).getNext();
            mMergeRequestDetailAdapter.setNotes(response.body());
        }

        @Override
        public void onFailure(Throwable t) {
            mLoading = false;
            Timber.e(t, null);
            mSwipeRefreshLayout.setRefreshing(false);
            Snackbar.make(getWindow().getDecorView(), getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                    .show();
        }
    };

    private Callback<List<Note>> mMoreNotesCallback = new Callback<List<Note>>() {

        @Override
        public void onResponse(Response<List<Note>> response, Retrofit retrofit) {
            mMergeRequestDetailAdapter.setLoading(false);
            mLoading = false;
            if (!response.isSuccess()) {
                return;
            }
            mNextPageUrl = PaginationUtil.parse(response).getNext();
            mMergeRequestDetailAdapter.addNotes(response.body());
        }

        @Override
        public void onFailure(Throwable t) {
            mLoading = false;
            Timber.e(t, null);
            mMergeRequestDetailAdapter.setLoading(false);
            Snackbar.make(getWindow().getDecorView(), getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                    .show();
        }
    };

    private Callback<Note> mPostNoteCallback = new Callback<Note>() {

        @Override
        public void onResponse(Response<Note> response, Retrofit retrofit) {
            mProgress.setVisibility(View.GONE);
            if (!response.isSuccess()) {
                Snackbar.make(getWindow().getDecorView(), getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                        .show();
                return;
            }
            mMergeRequestDetailAdapter.addNote(response.body());
            mNotesRecyclerView.smoothScrollToPosition(mMergeRequestDetailAdapter.getItemCount());
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t, null);
            mProgress.setVisibility(View.GONE);
            Snackbar.make(getWindow().getDecorView(), getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                    .show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merge_request);
        ButterKnife.bind(this);

        mProject = Parcels.unwrap(getIntent().getParcelableExtra(KEY_PROJECT));
        mMergeRequest = Parcels.unwrap(getIntent().getParcelableExtra(KEY_MERGE_REQUEST));

        mToolbar.setTitle(getString(R.string.merge_request_number) + mMergeRequest.getIid());
        mToolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mToolbar.setSubtitle(mProject.getNameWithNamespace());
        mMergeRequestTitle.setText(mMergeRequest.getTitle());

        mMergeRequestDetailAdapter = new MergeRequestDetailAdapter(MergeRequestActivity.this, mMergeRequest);
        mNotesLinearLayoutManager = new LinearLayoutManager(this);
        mNotesRecyclerView.setLayoutManager(mNotesLinearLayoutManager);
        mNotesRecyclerView.setAdapter(mMergeRequestDetailAdapter);
        mNotesRecyclerView.addOnScrollListener(mOnScrollListener);

        mNewNoteEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                postNote();
                return true;
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadNotes();
            }
        });
        loadNotes();
    }

    private void loadNotes() {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            }
        });
        GitLabClient.instance().getMergeRequestNotes(mProject.getId(), mMergeRequest.getId()).enqueue(mNotesCallback);
    }

    private void loadMoreNotes() {
        mMergeRequestDetailAdapter.setLoading(true);
        GitLabClient.instance().getMergeRequestNotes(mNextPageUrl.toString()).enqueue(mMoreNotesCallback);
    }

    private void postNote() {
        String body = mNewNoteEdit.getText().toString();

        if(body.length() < 1) {
            return;
        }

        mProgress.setVisibility(View.VISIBLE);
        mProgress.setAlpha(0.0f);
        mProgress.animate().alpha(1.0f);
        // Clear text & collapse keyboard
        KeyboardUtil.hideKeyboard(this);
        mNewNoteEdit.setText("");

        GitLabClient.instance().addMergeRequestNote(mProject.getId(), mMergeRequest.getId(), body).enqueue(mPostNoteCallback);
    }
}

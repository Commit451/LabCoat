package com.commit451.gitlab.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.NotesAdapter;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.Issue;
import com.commit451.gitlab.model.Note;
import com.commit451.gitlab.model.Project;
import com.commit451.gitlab.model.User;

import org.parceler.Parcels;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.Response;
import timber.log.Timber;

public class IssueActivity extends BaseActivity {

	private static final String EXTRA_PROJECT = "extra_project";
	private static final String EXTRA_SELECTED_ISSUE = "extra_selected_issue";

	public static Intent newInstance(Context context, Project project, Issue issue) {
		Intent intent = new Intent(context, IssueActivity.class);
		intent.putExtra(EXTRA_PROJECT, Parcels.wrap(project));
		intent.putExtra(EXTRA_SELECTED_ISSUE, Parcels.wrap(issue));
		return intent;
	}

	@Bind(R.id.toolbar) Toolbar toolbar;
	@Bind(R.id.swipe_layout) SwipeRefreshLayout swipeRefreshLayout;
	@Bind(R.id.list) RecyclerView listView;
	@Bind(R.id.new_note_edit) EditText newNoteEdit;
	@Bind(R.id.progress) View progress;

	private NotesAdapter notesAdapter;
	Project mProject;
    Issue mIssue;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_issue);
		ButterKnife.bind(this);

		mProject = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_PROJECT));
		mIssue = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_SELECTED_ISSUE));

        long tempId = mIssue.getIid();
        if(tempId < 1) {
            tempId = mIssue.getId();
        }

        toolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
        toolbar.setTitle("Issue #" + tempId);

        notesAdapter = new NotesAdapter(mIssue);
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.setAdapter(notesAdapter);

		newNoteEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                postNote();
                return true;
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                load();
            }
        });
        load();
    }

    private void load() {
		swipeRefreshLayout.setRefreshing(true);
        GitLabClient.instance().getIssueNotes(mProject.getId(), mIssue.getId()).enqueue(notesCallback);
	}

	private void postNote() {
		String body = newNoteEdit.getText().toString();

		if(body.length() < 1) {
			return;
		}

		progress.setVisibility(View.VISIBLE);
		progress.setAlpha(0.0f);
		progress.animate().alpha(1.0f);
		// Clear text & collapse keyboard
		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(newNoteEdit.getWindowToken(), 0);
		newNoteEdit.setText("");

		GitLabClient.instance().postIssueNote(mProject.getId(), mIssue.getId(), body).enqueue(noteCallback);
	}
	
	private Callback<List<Note>> notesCallback = new Callback<List<Note>>() {

		@Override
		public void onResponse(Response<List<Note>> response) {
			if (!response.isSuccess()) {
				return;
			}
			swipeRefreshLayout.setRefreshing(false);
			notesAdapter.addNotes(response.body());
		}

		@Override
		public void onFailure(Throwable t) {
			Timber.e(t.toString());
			swipeRefreshLayout.setRefreshing(false);
			Snackbar.make(getWindow().getDecorView(), getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
					.show();
		}
	};
	
	@OnClick(R.id.new_note_button)
	public void onNewNoteClick() {
		postNote();
	}
	
	private Callback<Note> noteCallback = new Callback<Note>() {

		@Override
		public void onResponse(Response<Note> response) {
			if (!response.isSuccess()) {
				return;
			}
			progress.setVisibility(View.GONE);
			notesAdapter.addNote(response.body());
		}

		@Override
		public void onFailure(Throwable t) {
			Timber.e(t.toString());
			progress.setVisibility(View.GONE);
			Snackbar.make(getWindow().getDecorView(), getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
					.show();
		}
	};
	
	private Callback<List<User>> usersCallback = new Callback<List<User>>() {

		@Override
		public void onResponse(Response<List<User>> response) {
			if (!response.isSuccess()) {
				return;
			}
			swipeRefreshLayout.setRefreshing(false);
			notesAdapter.addUsers(response.body());
		}

		@Override
		public void onFailure(Throwable t) {
			Timber.e(t.toString());
			swipeRefreshLayout.setRefreshing(false);
			Snackbar.make(getWindow().getDecorView(), getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
					.show();
		}
	};
}

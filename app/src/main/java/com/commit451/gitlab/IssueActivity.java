package com.commit451.gitlab;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.commit451.gitlab.adapter.NotesAdapter;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.Issue;
import com.commit451.gitlab.model.Milestone;
import com.commit451.gitlab.model.Note;
import com.commit451.gitlab.model.User;
import com.commit451.gitlab.tools.Repository;

import org.parceler.Parcels;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

public class IssueActivity extends BaseActivity {

	private static final String EXTRA_SELECTED_ISSUE = "extra_selected_issue";

	public static Intent newInstance(Context context, Issue issue) {
		Intent intent = new Intent(context, IssueActivity.class);
		intent.putExtra(EXTRA_SELECTED_ISSUE, Parcels.wrap(issue));
		return intent;
	}

	@Bind(R.id.toolbar) Toolbar toolbar;
	@Bind(R.id.swipe_layout) SwipeRefreshLayout swipeRefreshLayout;
	@Bind(R.id.list) RecyclerView listView;
	@Bind(R.id.new_note_edit) EditText newNoteEdit;
	@Bind(R.id.progress) View progress;

	private NotesAdapter notesAdapter;
    Issue issue;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_issue);
		ButterKnife.bind(this);

		issue = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_SELECTED_ISSUE));

        long tempId = issue.getIid();
        if(tempId < 1) {
            tempId = issue.getId();
        }

        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setTitle("Issue #" + tempId);

        notesAdapter = new NotesAdapter(issue);
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.setAdapter(notesAdapter);

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
        //TODO chain these
        GitLabClient.instance().getIssueNotes(Repository.selectedProject.getId(), issue.getId(), notesCallback);
        GitLabClient.instance().getMilestones(Repository.selectedProject.getId(), milestonesCallback);
        GitLabClient.instance().getUsersFallback(Repository.selectedProject.getId(), usersCallback);
	}

	private void changeStatus() {
//		String selection = stateSpinner.getSelectedItem().toString();
//		String value = "";
//		if(selection.equals("closed") && (Repository.selectedIssue.getState().equals("opened") || Repository.selectedIssue.getState().equals("reopened"))) {
//			value = "close";
//		}
//		if((selection.equals("reopened") || selection.equals("opened")) && Repository.selectedIssue.getState().equals("closed")) {
//			value = "reopen";
//		}
//
//        GitLabClient.instance().editIssue(
//				Repository.selectedProject.getId(),
//				Repository.selectedIssue.getId(),
//				value,
//				"",
//				issueCallback);
	}
	
	private Callback<List<Note>> notesCallback = new Callback<List<Note>>() {
		
		@Override
		public void success(List<Note> notes, Response resp) {
			swipeRefreshLayout.setRefreshing(false);
            notesAdapter.addNotes(notes);
		}
		
		@Override
		public void failure(RetrofitError e) {
            Timber.e(e.toString());
			swipeRefreshLayout.setRefreshing(false);
			Snackbar.make(getWindow().getDecorView(), getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
					.show();
		}
	};
	
	@OnClick(R.id.new_note_button)
	public void onNewNoteClick() {
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

        GitLabClient.instance().postIssueNote(Repository.selectedProject.getId(), issue.getId(), body, "", noteCallback);
	}
	
	private Callback<Note> noteCallback = new Callback<Note>() {
		
		@Override
		public void success(Note note, Response resp) {
			progress.setVisibility(View.GONE);
			notesAdapter.addNote(note);
		}
		
		@Override
		public void failure(RetrofitError e) {
            Timber.e(e.toString());
			progress.setVisibility(View.GONE);
			Snackbar.make(getWindow().getDecorView(), getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
					.show();
		}
	};
	
	private Callback<Issue> issueCallback = new Callback<Issue>() {
		
		@Override
		public void success(Issue issue, Response resp) {
			progress.setVisibility(View.GONE);
			
//			Repository.selectedIssue.setState(stateSpinner.getSelectedItem().toString());
//			Repository.selectedIssue.setAssignee((User) assigneeSpinner.getSelectedItem());
//			Repository.selectedIssue.setMilestone((Milestone) milestoneSpinner.getSelectedItem());
			//TODO notify the main activity when a issue changes so it will update in the list
		}
		
		@Override
		public void failure(RetrofitError e) {
			Timber.e(e.toString());
			progress.setVisibility(View.GONE);
			Snackbar.make(getWindow().getDecorView(), getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
					.show();
		}
	};
	
	private Callback<List<User>> usersCallback = new Callback<List<User>>() {
		
		@Override
		public void success(List<User> users, Response resp) {
			swipeRefreshLayout.setRefreshing(false);
            notesAdapter.addUsers(users);
//			UserAdapter ua = new UserAdapter(IssueActivity.this, users);
//			assigneeSpinner.setAdapter(ua);
//			assigneeSpinner.setSelection(ua.getPosition(Repository.selectedIssue.getAssignee()), true);
		}
		
		@Override
		public void failure(RetrofitError e) {
			Timber.e(e.toString());
			swipeRefreshLayout.setRefreshing(false);
			Snackbar.make(getWindow().getDecorView(), getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
					.show();
		}
	};
	
	private Callback<List<Milestone>> milestonesCallback = new Callback<List<Milestone>>() {
		
		@Override
		public void success(List<Milestone> milestones, Response resp) {
			swipeRefreshLayout.setRefreshing(false);

            notesAdapter.addMilestones(milestones);
//			MilestonesAdapter ma = new MilestonesAdapter(IssueActivity.this, milestones);
//			milestoneSpinner.setAdapter(ma);
//			milestoneSpinner.setSelection(ma.getPosition(Repository.selectedIssue.getMilestone()), true);
//			if (milestones.isEmpty()) {
//				milestoneSpinner.setVisibility(View.GONE);
//			}
		}
		
		@Override
		public void failure(RetrofitError e) {
            Timber.e(e.toString());
			swipeRefreshLayout.setRefreshing(false);
			Snackbar.make(getWindow().getDecorView(), getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
					.show();
		}
	};
}

package com.commit451.gitlab;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.commit451.gitlab.adapter.MilestonesAdapter;
import com.commit451.gitlab.adapter.NoteAdapter;
import com.commit451.gitlab.adapter.UserAdapter;
import com.commit451.gitlab.model.Issue;
import com.commit451.gitlab.model.Milestone;
import com.commit451.gitlab.model.Note;
import com.commit451.gitlab.model.User;
import com.commit451.gitlab.tools.Repository;
import com.commit451.gitlab.tools.RetrofitHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import in.uncod.android.bypass.Bypass;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class IssueActivity extends BaseActivity {

	@Bind(R.id.toolbar) Toolbar toolbar;
	@Bind(R.id.scroll1) ScrollView scroll;
	
	@Bind(R.id.title) TextView title;
	@Bind(R.id.state_spinner) Spinner stateSpinner;
	@Bind(R.id.assignee_spinner) Spinner assigneeSpinner;
	@Bind(R.id.milestone_spinner) Spinner milestoneSpinner;
	@Bind(R.id.description) TextView description;
	@Bind(R.id.note_list) ListView noteList;
	
	@Bind(R.id.progressbar_loading) ProgressBar progressBar;
	@Bind(R.id.new_note_edit) EditText newNoteEdit;
	
	private ProgressDialog pd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_issue);
		ButterKnife.bind(this);
		
		if(Repository.selectedIssue != null) {
			setupUI();
			loadNotes();
		}
		else {
			finish();
		}
	}
	
	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupUI() {
		long tempId = Repository.selectedIssue.getIid();
		if(tempId < 1) {
			tempId = Repository.selectedIssue.getId();
		}

		toolbar.setNavigationIcon(R.drawable.ic_back);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		toolbar.setTitle("Issue #" + tempId);
		toolbar.inflateMenu(R.menu.issue);
		toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch(item.getItemId()) {
					case android.R.id.home:
						finish();
						return true;
					case R.id.action_save:
						save();
						return true;
				}
				return false;
			}
		});
		
		title.setText(Repository.selectedIssue.getTitle());
		
		ArrayList<String> temp3 = new ArrayList<>();
		if(Repository.selectedIssue.getState().equals("opened")) {
			temp3.add("opened");
			temp3.add("closed");
		}
		else {
			temp3.add("closed");
			temp3.add("reopened");
		}
		stateSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, temp3));
		stateSpinner.setSelection(temp3.indexOf(Repository.selectedIssue.getState()));
		
		if(Repository.userAdapter != null) {
			assigneeSpinner.setAdapter(Repository.userAdapter);
			if(Repository.selectedIssue.getAssignee() != null)
				assigneeSpinner.setSelection(Repository.userAdapter.getPosition(Repository.selectedIssue.getAssignee()), true);
		}
		else {
			if(Repository.selectedIssue.getAssignee() != null) {
				ArrayList<User> temp = new ArrayList<User>();
				temp.add(Repository.selectedIssue.getAssignee());
				assigneeSpinner.setAdapter(new UserAdapter(this, temp));
			}
			
			Repository.getService().getUsersFallback(Repository.selectedProject.getId(), usersCallback);
		}
		
		ArrayList<Milestone> temp2 = new ArrayList<Milestone>();
		if(Repository.selectedIssue.getMilestone() != null)
			temp2.add(Repository.selectedIssue.getMilestone());
		milestoneSpinner.setAdapter(new MilestonesAdapter(this, temp2));
		
		Repository.getService().getMilestones(Repository.selectedProject.getId(), milestonesCallback);
		
		Bypass bypass = new Bypass();
		String desc = Repository.selectedIssue.getDescription();
		if(desc == null)
			desc = "";
		description.setText(bypass.markdownToSpannable(desc));
		description.setMovementMethod(LinkMovementMethod.getInstance());
		
		Repository.setListViewSize(noteList);
	}
	
	private void loadNotes() {
		progressBar.setVisibility(View.VISIBLE);
		Repository.getService().getIssueNotes(Repository.selectedProject.getId(), Repository.selectedIssue.getId(), notesCallback);
	}
	
	private Callback<List<Note>> notesCallback = new Callback<List<Note>>() {
		
		@Override
		public void success(List<Note> notes, Response resp) {
			progressBar.setVisibility(View.GONE);
			
			NoteAdapter noteAdapter = new NoteAdapter(IssueActivity.this, notes);
			noteList.setAdapter(noteAdapter);
			
			Repository.setListViewSize(noteList);
		}
		
		@Override
		public void failure(RetrofitError e) {
			RetrofitHelper.printDebugInfo(IssueActivity.this, e);
			
			progressBar.setVisibility(View.GONE);
			Snackbar.make(getWindow().getDecorView(), getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
					.show();
		}
	};
	
	@OnClick(R.id.new_note_button)
	public void onNewNoteClick() {
		String body = newNoteEdit.getText().toString();
		
		if(body.length() < 1)
			return;
		
		pd = ProgressDialog.show(IssueActivity.this, "", getResources().getString(R.string.progress_dialog), true);
		
		// Clear text & collapse keyboard
		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(newNoteEdit.getWindowToken(), 0);
		newNoteEdit.setText("");
		
		Repository.getService().postIssueNote(Repository.selectedProject.getId(), Repository.selectedIssue.getId(), body, "", noteCallback);
	}
	
	private Callback<Note> noteCallback = new Callback<Note>() {
		
		@Override
		public void success(Note note, Response resp) {
			if(pd != null && pd.isShowing())
				pd.cancel();
			
			((NoteAdapter) noteList.getAdapter()).addNote(note);
			Repository.setListViewSize(noteList);
			
			scroll.post(new Runnable() {
				
				@Override
				public void run() {
					// Scroll to bottom of list
					scroll.fullScroll(View.FOCUS_DOWN);
				}
			});
		}
		
		@Override
		public void failure(RetrofitError e) {
			RetrofitHelper.printDebugInfo(IssueActivity.this, e);
			
			if(pd != null && pd.isShowing())
				pd.cancel();

			Snackbar.make(getWindow().getDecorView(), getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
					.show();
		}
	};
	
	private void save() {
		pd = ProgressDialog.show(IssueActivity.this, "", getResources().getString(R.string.progress_dialog), true);
		
		String selection = stateSpinner.getSelectedItem().toString();
		String value = "";
		if(selection.equals("closed") && (Repository.selectedIssue.getState().equals("opened") || Repository.selectedIssue.getState().equals("reopened")))
			value = "close";
		if((selection.equals("reopened") || selection.equals("opened")) && Repository.selectedIssue.getState().equals("closed"))
			value = "reopen";
		
		Repository.getService().editIssue(Repository.selectedProject.getId(), Repository.selectedIssue.getId(), value, assigneeSpinner.getSelectedItemId(), milestoneSpinner.getSelectedItemId(),
				"", issueCallback);
	}
	
	private Callback<Issue> issueCallback = new Callback<Issue>() {
		
		@Override
		public void success(Issue issue, Response resp) {
			if(pd != null && pd.isShowing())
				pd.cancel();
			
			Repository.selectedIssue.setState(stateSpinner.getSelectedItem().toString());
			Repository.selectedIssue.setAssignee((User) assigneeSpinner.getSelectedItem());
			Repository.selectedIssue.setMilestone((Milestone) milestoneSpinner.getSelectedItem());
			
			if(Repository.issueAdapter != null)
				Repository.issueAdapter.notifyDataSetChanged();
		}
		
		@Override
		public void failure(RetrofitError e) {
			RetrofitHelper.printDebugInfo(IssueActivity.this, e);
			
			if(pd != null && pd.isShowing())
				pd.cancel();
			Snackbar.make(getWindow().getDecorView(), getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
					.show();
		}
	};
	
	private Callback<List<User>> usersCallback = new Callback<List<User>>() {
		
		@Override
		public void success(List<User> users, Response resp) {
			progressBar.setVisibility(View.GONE);
			
			UserAdapter ua = new UserAdapter(IssueActivity.this, users);
			assigneeSpinner.setAdapter(ua);
			assigneeSpinner.setSelection(ua.getPosition(Repository.selectedIssue.getAssignee()), true);
		}
		
		@Override
		public void failure(RetrofitError e) {
			RetrofitHelper.printDebugInfo(IssueActivity.this, e);
			
			progressBar.setVisibility(View.GONE);
			Snackbar.make(getWindow().getDecorView(), getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
					.show();
		}
	};
	
	private Callback<List<Milestone>> milestonesCallback = new Callback<List<Milestone>>() {
		
		@Override
		public void success(List<Milestone> milestones, Response resp) {
			progressBar.setVisibility(View.GONE);
			
			MilestonesAdapter ma = new MilestonesAdapter(IssueActivity.this, milestones);
			milestoneSpinner.setAdapter(ma);
			milestoneSpinner.setSelection(ma.getPosition(Repository.selectedIssue.getMilestone()), true);
		}
		
		@Override
		public void failure(RetrofitError e) {
			RetrofitHelper.printDebugInfo(IssueActivity.this, e);
			
			progressBar.setVisibility(View.GONE);
			Snackbar.make(getWindow().getDecorView(), getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
					.show();
		}
	};
}

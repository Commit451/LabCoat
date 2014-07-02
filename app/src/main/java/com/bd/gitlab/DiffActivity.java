package com.bd.gitlab;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.bd.gitlab.model.Diff;
import com.bd.gitlab.tools.Repository;
import com.bd.gitlab.tools.RetrofitHelper;
import com.bd.gitlab.views.DiffView;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class DiffActivity extends Activity {
	
	@InjectView(R.id.diff_container) LinearLayout diffContainer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_diff);
		ButterKnife.inject(this);
		
		init();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		Crouton.cancelAllCroutons();
	}
	
	private void init() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(Repository.selectedCommit.getShortId());
        getActionBar().setIcon(getResources().getDrawable(R.drawable.ic_actionbar));
		
		Repository.getService().getCommitDiff(Repository.selectedProject.getId(), Repository.selectedCommit.getId(), diffCallback);
	}
	
	private Callback<List<Diff>> diffCallback = new Callback<List<Diff>>() {
		
		@Override
		public void success(List<Diff> diffs, Response resp) {
			for(Diff diff : diffs) {
				diffContainer.addView(new DiffView(DiffActivity.this, diff));
			}
		}
		
		@Override
		public void failure(RetrofitError e) {
			RetrofitHelper.printDebugInfo(DiffActivity.this, e);
			
			Crouton.makeText(DiffActivity.this, R.string.connection_error, Style.ALERT);
		}
	};
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
}
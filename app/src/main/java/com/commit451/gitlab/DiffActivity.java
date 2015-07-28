package com.commit451.gitlab;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.commit451.gitlab.model.Diff;
import com.commit451.gitlab.model.DiffLine;
import com.commit451.gitlab.tools.Repository;
import com.commit451.gitlab.tools.RetrofitHelper;
import com.commit451.gitlab.views.DiffView;
import com.commit451.gitlab.views.MessageView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class DiffActivity extends BaseActivity {

	@Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.message_container)	LinearLayout messageContainer;
	@Bind(R.id.diff_container) LinearLayout diffContainer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_diff);
		ButterKnife.bind(this);
		init();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Crouton.cancelAllCroutons();
	}
	
	private void init() {
		toolbar.setNavigationIcon(R.drawable.ic_back);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		toolbar.setTitle(Repository.selectedCommit.getShortId());
		toolbar.inflateMenu(R.menu.diff);
		toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch(item.getItemId()) {
					case android.R.id.home:
						finish();
						return true;
					case R.id.text_wrap_checkbox:
						item.setChecked(!item.isChecked());
						setTextWrap(item.isChecked());
						return true;
				}
				return false;
			}
		});

		Repository.getService().getCommit(Repository.selectedProject.getId(), Repository.selectedCommit.getId(), commitCallback);
		Repository.getService().getCommitDiff(Repository.selectedProject.getId(), Repository.selectedCommit.getId(), diffCallback);
	}

	private Callback<DiffLine> commitCallback = new Callback<DiffLine>() {
		@Override
		public void success(DiffLine diffLine, Response response) {
			messageContainer.addView(new MessageView(DiffActivity.this, diffLine));
		}

		@Override
		public void failure(RetrofitError e) {
			RetrofitHelper.printDebugInfo(DiffActivity.this, e);

            Crouton.makeText(DiffActivity.this, R.string.connection_error, Style.ALERT);
		}
	};
	
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
	
	private void setTextWrap(boolean checked) {
		((MessageView) messageContainer.getChildAt(0)).setWrapped(checked);

		for(int i = 0; i < diffContainer.getChildCount(); ++i) {
			((DiffView) diffContainer.getChildAt(i)).setWrapped(checked);
		}
	}
}
package com.commit451.gitlab;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.webkit.WebView;

import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.tools.Repository;
import com.commit451.gitlab.tools.RetrofitHelper;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class FileActivity extends BaseActivity {
	@Bind(R.id.toolbar) Toolbar toolbar;
	@Bind(R.id.file_blob) WebView fileBlobView;
	
	private byte[] fileBlob;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file);
		ButterKnife.bind(this);
		
		if(Repository.selectedFile != null) {
			setupUI();

			GitLabClient.instance().getBlob(Repository.selectedProject.getId(), Repository.newestCommit.getId(), getIntent().getExtras().getString("path") + Repository.selectedFile.getName(), blobCallback);
		}
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	private void setupUI() {
		toolbar.setNavigationIcon(R.drawable.ic_back);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		toolbar.setTitle(Repository.selectedFile.getName());
		toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch(item.getItemId()) {
					case R.id.action_open:
						openFile();
						return true;
					case R.id.action_save:
						saveBlob();
						return true;
				}
				return false;
			}
		});
		
		fileBlobView.getSettings().setJavaScriptEnabled(true);
	}
	
	private Callback<Response> blobCallback = new Callback<Response>() {
		
		@Override
		public void success(Response response, Response resp) {
			String content = getResources().getString(R.string.file_load_error);
			
			try {
				fileBlob = IOUtils.toByteArray(response.getBody().in());
				content = new String(fileBlob, "UTF-8");
			}
			catch(UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			catch(IOException e) {
				e.printStackTrace();
			}
			
			String temp = "<!DOCTYPE html><html><head><link href=\"github.css\" rel=\"stylesheet\" /></head><body><pre><code>" + StringEscapeUtils.escapeHtml(content) + "</code></pre><script src=\"highlight.pack.js\"></script><script>hljs.initHighlightingOnLoad();</script></body></html>";
			fileBlobView.loadDataWithBaseURL("file:///android_asset/", temp, "text/html", "utf8", null);

			toolbar.inflateMenu(R.menu.file);
		}
		
		@Override
		public void failure(RetrofitError e) {
			RetrofitHelper.printDebugInfo(FileActivity.this, e);
			Snackbar.make(getWindow().getDecorView(), getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
					.show();
		}
	};
	
	private File saveBlob() {
		String state = Environment.getExternalStorageState();
		
		if(Environment.MEDIA_MOUNTED.equals(state) && fileBlob != null) {
			File downloadFolder = new File(Environment.getExternalStorageDirectory(), "Download");
			
			if(!downloadFolder.exists())
				downloadFolder.mkdir();
			
			File newFile = new File(downloadFolder, Repository.selectedFile.getName());
			
			try {
				FileOutputStream f = new FileOutputStream(newFile);
				f.write(fileBlob);
				f.close();

				Snackbar.make(getWindow().getDecorView(), getString(R.string.file_saved), Snackbar.LENGTH_SHORT)
						.show();
				
				return newFile;
			}
			catch(IOException e) {
				Snackbar.make(getWindow().getDecorView(), getString(R.string.save_error), Snackbar.LENGTH_SHORT)
						.show();
			}
		}
		else {
			Snackbar.make(getWindow().getDecorView(), getString(R.string.save_error), Snackbar.LENGTH_SHORT)
					.show();
		}
		
		return null;
	}
	
	private void openFile() {
		File file = saveBlob();
		
		if(file == null) {
			Snackbar.make(getWindow().getDecorView(), getString(R.string.open_error), Snackbar.LENGTH_SHORT)
					.show();
            return;
        }

        MimeTypeMap myMime = MimeTypeMap.getSingleton();
        Intent newIntent = new Intent(Intent.ACTION_VIEW);

        String fileExt = fileExt(file.toString());
        if (fileExt == null) {
			Snackbar.make(getWindow().getDecorView(), getString(R.string.open_error), Snackbar.LENGTH_SHORT)
					.show();
            return;
        }
        String mimeType = myMime.getMimeTypeFromExtension(fileExt.substring(1));
        newIntent.setDataAndType(Uri.fromFile(file), mimeType);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            startActivity(newIntent);
        }
        catch(android.content.ActivityNotFoundException e) {
			Snackbar.make(getWindow().getDecorView(), getString(R.string.open_error), Snackbar.LENGTH_SHORT)
					.show();
        }
	}
	
	private String fileExt(String url) {
		if(url.contains("?")) {
			url = url.substring(0, url.indexOf("?"));
		}
		if(!url.contains(".")) {
			return null;
		}
		else {
			String ext = url.substring(url.lastIndexOf("."));
			if(ext.contains("%")) {
				ext = ext.substring(0, ext.indexOf("%"));
			}
			if(ext.contains("/")) {
				ext = ext.substring(0, ext.indexOf("/"));
			}
			
			return ext.toLowerCase();
		}
	}
}

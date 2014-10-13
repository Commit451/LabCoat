package com.bd.gitlab;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.webkit.WebView;

import com.bd.gitlab.tools.Repository;
import com.bd.gitlab.tools.RetrofitHelper;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class FileActivity extends Activity {
	
	@InjectView(R.id.file_blob) WebView fileBlobView;
	
	private MenuItem openFile;
	private MenuItem saveFile;
	
	private byte[] fileBlob;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file);
		ButterKnife.inject(this);
		
		if(Repository.selectedFile != null) {
			setupUI();
			
			Repository.getService().getBlob(Repository.selectedProject.getId(), Repository.newestCommit.getId(), getIntent().getExtras().getString("path") + Repository.selectedFile.getName(), blobCallback);
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		Crouton.cancelAllCroutons();
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	private void setupUI() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(Repository.selectedFile.getName());
        getActionBar().setIcon(getResources().getDrawable(R.drawable.ic_actionbar));
		
		fileBlobView.getSettings().setJavaScriptEnabled(true);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.file, menu);
		
		openFile = menu.getItem(0);
		saveFile = menu.getItem(1);
		
		return true;
	}
	
	private void enableMenu() {
		if(openFile != null) {
			openFile.setEnabled(true);
			openFile.setIcon(R.drawable.ic_action_open);
		}
		
		if(saveFile != null) {
			saveFile.setEnabled(true);
			saveFile.setIcon(R.drawable.ic_action_save);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			case R.id.action_open:
				openFile();
				return true;
			case R.id.action_save:
				saveBlob();
				return true;
		}
		
		return super.onOptionsItemSelected(item);
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
			
			enableMenu();
		}
		
		@Override
		public void failure(RetrofitError e) {
			RetrofitHelper.printDebugInfo(FileActivity.this, e);
			Crouton.makeText(FileActivity.this, R.string.connection_error, Style.ALERT).show();
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
				
				Crouton.makeText(this, R.string.file_saved, Style.CONFIRM).show();
				
				return newFile;
			}
			catch(FileNotFoundException e) {
				Crouton.makeText(this, R.string.save_error, Style.ALERT).show();
			}
			catch(IOException e) {
				Crouton.makeText(this, R.string.save_error, Style.ALERT).show();
			}
		}
		else
			Crouton.makeText(this, R.string.save_error, Style.ALERT).show();
		
		return null;
	}
	
	private void openFile() {
		File file = saveBlob();
		
		MimeTypeMap myMime = MimeTypeMap.getSingleton();
		Intent newIntent = new Intent(Intent.ACTION_VIEW);

		String fileExt = fileExt(file.toString());
		if (fileExt == null) {
			Crouton.makeText(this, R.string.open_error, Style.ALERT).show();
			return;
		}
		String mimeType = myMime.getMimeTypeFromExtension(fileExt.substring(1));
		newIntent.setDataAndType(Uri.fromFile(file), mimeType);
		newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		try {
			startActivity(newIntent);
		}
		catch(android.content.ActivityNotFoundException e) {
			Crouton.makeText(this, R.string.open_error, Style.ALERT).show();
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

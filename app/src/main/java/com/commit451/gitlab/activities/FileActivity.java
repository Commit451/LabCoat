package com.commit451.gitlab.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.webkit.WebView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.FileResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class FileActivity extends BaseActivity {

    private static final String EXTRA_PROJECT_ID = "extra_project_id";
    private static final String EXTRA_PATH = "extra_path";
    private static final String EXTRA_REF = "extra_ref";

    public static Intent newIntent(Context context, long projectId, String path, String ref) {
        Intent intent = new Intent(context, FileActivity.class);
        intent.putExtra(EXTRA_PROJECT_ID, projectId);
        intent.putExtra(EXTRA_PATH, path);
        intent.putExtra(EXTRA_REF, ref);
        return intent;
    }

    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.file_blob) WebView fileBlobView;
    @Bind(R.id.progress) View progress;

    long mProjectId;
    String mPath;
    String mRef;

    String mFileName;
    byte[] mBlob;

    private final Callback<FileResponse> mFileResponseCallback = new Callback<FileResponse>() {

        @Override
        public void onResponse(Response<FileResponse> response, Retrofit retrofit) {
            if (!response.isSuccess()) {
                return;
            }
            progress.setVisibility(View.GONE);
            String text = getString(R.string.file_load_error);
            // Receiving side
            mFileName = response.body().getFileName();
            mBlob = Base64.decode(response.body().getContent(), Base64.DEFAULT);

            String content;

            String mimeType = null;
            String ext = fileExt(mFileName);
            if (ext != null) {
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.substring(1));
            }

            if (mimeType != null && mimeType.startsWith("image/")) {
                String imageURL = "data:" + mimeType + ";base64," + response.body().getContent();

                content = "<!DOCTYPE html><html><head><link href=\"github.css\" rel=\"stylesheet\" /></head><body><img style=\"width: 100%;\" src=\"" + imageURL + "\"></body></html>";
            }
            else {
                try {
                    text = new String(mBlob, "UTF-8");
                }
                catch (UnsupportedEncodingException e) {
                    Timber.e(e.toString());
                }

                content = "<!DOCTYPE html><html><head><link href=\"github.css\" rel=\"stylesheet\" /></head><body><pre><code>" + Html.escapeHtml(text) + "</code></pre><script src=\"highlight.pack.js\"></script><script>hljs.initHighlightingOnLoad();</script></body></html>";
            }

            fileBlobView.loadDataWithBaseURL("file:///android_asset/", content, "text/html", "utf8", null);
            toolbar.setTitle(mFileName);
            toolbar.inflateMenu(R.menu.file);
        }

        @Override
        public void onFailure(Throwable t) {
            progress.setVisibility(View.GONE);
            Snackbar.make(getWindow().getDecorView(), R.string.file_load_error, Snackbar.LENGTH_SHORT)
                    .show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);
        ButterKnife.bind(this);
        mProjectId = getIntent().getLongExtra(EXTRA_PROJECT_ID, -1);
        mPath = getIntent().getStringExtra(EXTRA_PATH);
        mRef = getIntent().getStringExtra(EXTRA_REF);
        setupUI();
        load();
    }

    private void load() {
        progress.setVisibility(View.VISIBLE);
        GitLabClient.instance().getFile(mProjectId, mPath, mRef).enqueue(mFileResponseCallback);
    }

    private void setupUI() {
        toolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
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

    }

    private File saveBlob() {
        String state = Environment.getExternalStorageState();

        if(Environment.MEDIA_MOUNTED.equals(state) && mBlob != null) {
            File downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            File newFile = new File(downloadFolder, mFileName);

            try {
                FileOutputStream f = new FileOutputStream(newFile);
                f.write(mBlob);
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

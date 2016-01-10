package com.commit451.gitlab.activity;

import android.content.ActivityNotFoundException;
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
import com.commit451.gitlab.model.api.RepositoryFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class FileActivity extends BaseActivity {
    private static final long MAX_FILE_SIZE = 1024 * 1024;
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

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.file_blob) WebView mFileBlobView;
    @Bind(R.id.progress) View mProgressView;

    private long mProjectId;
    private String mPath;
    private String mRef;

    private String mFileName;
    private byte[] mBlob;

    private final Callback<RepositoryFile> mFileResponseCallback = new Callback<RepositoryFile>() {

        @Override
        public void onResponse(Response<RepositoryFile> response, Retrofit retrofit) {
            mProgressView.setVisibility(View.GONE);

            if (!response.isSuccess()) {
                Snackbar.make(getWindow().getDecorView(), R.string.file_load_error, Snackbar.LENGTH_SHORT)
                        .show();
                return;
            }

            if (response.body().getSize() > MAX_FILE_SIZE) {
                Snackbar.make(getWindow().getDecorView(), R.string.file_too_big, Snackbar.LENGTH_SHORT)
                        .show();
                return;
            }

            // Receiving side
            mFileName = response.body().getFileName();
            mBlob = Base64.decode(response.body().getContent(), Base64.DEFAULT);

            String content;

            String mimeType = null;
            String extension = fileExt(mFileName);
            if (extension != null) {
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension).toLowerCase();
            }

            if (mimeType != null && mimeType.startsWith("image/")) {
                String imageURL = "data:" + mimeType + ";base64," + response.body().getContent();

                content = "<!DOCTYPE html><html><head><link href=\"github.css\" rel=\"stylesheet\" /></head><body><img style=\"width: 100%;\" src=\"" + imageURL + "\"></body></html>";
            } else {
                String text = new String(mBlob, Charset.forName("UTF-8"));

                content = "<!DOCTYPE html><html><head><link href=\"github.css\" rel=\"stylesheet\" /></head><body><pre><code>" + Html.escapeHtml(text) + "</code></pre><script src=\"highlight.pack.js\"></script><script>hljs.initHighlightingOnLoad();</script></body></html>";
            }

            mFileBlobView.loadDataWithBaseURL("file:///android_asset/", content, "text/html", "utf8", null);
            mToolbar.setTitle(mFileName);
            mToolbar.inflateMenu(R.menu.file);
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t, null);
            mProgressView.setVisibility(View.GONE);
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

        mToolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
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

        loadData();
    }

    private void loadData() {
        mProgressView.setVisibility(View.VISIBLE);
        GitLabClient.instance().getFile(mProjectId, mPath, mRef).enqueue(mFileResponseCallback);
    }

    private File saveBlob() {
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state) && mBlob != null) {
            File targetFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), mFileName);

            FileOutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(targetFile);
                outputStream.write(mBlob);

                Snackbar.make(getWindow().getDecorView(), getString(R.string.file_saved), Snackbar.LENGTH_SHORT)
                        .show();

                return targetFile;
            } catch (IOException e) {
                Timber.e(e, null);
                Snackbar.make(getWindow().getDecorView(), getString(R.string.save_error), Snackbar.LENGTH_SHORT)
                        .show();
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                    }
                }
            }
        } else {
            Snackbar.make(getWindow().getDecorView(), getString(R.string.save_error), Snackbar.LENGTH_SHORT)
                    .show();
        }

        return null;
    }

    private void openFile() {
        File file = saveBlob();
        if (file == null) {
            Snackbar.make(getWindow().getDecorView(), getString(R.string.open_error), Snackbar.LENGTH_SHORT)
                    .show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.fromFile(file));

        String extension = fileExt(file.getName());
        if (extension != null) {
            intent.setTypeAndNormalize(MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension));
        }

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException | SecurityException e) {
            Timber.e(e, null);
            Snackbar.make(getWindow().getDecorView(), getString(R.string.open_error), Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

    private static String fileExt(String filename) {
        int extStart = filename.lastIndexOf(".") + 1;
        if (extStart < 1) {
            return null;
        }

        return filename.substring(extStart);
    }
}

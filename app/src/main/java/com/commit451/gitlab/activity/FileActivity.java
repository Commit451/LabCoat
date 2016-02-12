package com.commit451.gitlab.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.webkit.WebView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.api.EasyCallback;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.api.RepositoryFile;
import com.commit451.gitlab.observable.DecodeObservableFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.charset.Charset;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class FileActivity extends BaseActivity {

    private static final int PERMISSION_REQUEST_WRITE_STORAGE = 1337;

    private static final long MAX_FILE_SIZE = 1024 * 1024;
    private static final String EXTRA_PROJECT_ID = "extra_project_id";
    private static final String EXTRA_PATH = "extra_path";
    private static final String EXTRA_REF = "extra_ref";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({OPTION_SAVE, OPTION_OPEN})
    public @interface Option {}
    public static final int OPTION_SAVE = 0;
    public static final int OPTION_OPEN = 1;

    public static Intent newIntent(Context context, long projectId, String path, String ref) {
        Intent intent = new Intent(context, FileActivity.class);
        intent.putExtra(EXTRA_PROJECT_ID, projectId);
        intent.putExtra(EXTRA_PATH, path);
        intent.putExtra(EXTRA_REF, ref);
        return intent;
    }

    @Bind(R.id.root) ViewGroup mRoot;
    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.file_blob) WebView mFileBlobView;
    @Bind(R.id.progress) View mProgressView;

    private long mProjectId;
    private String mPath;
    private String mRef;
    private RepositoryFile mRepositoryFile;
    private String mFileName;
    private byte[] mBlob;
    private @Option int mOption;

    private final Callback<RepositoryFile> mRepositoryFileCallback = new EasyCallback<RepositoryFile>() {
        @Override
        public void onResponse(@NonNull RepositoryFile response) {
            mProgressView.setVisibility(View.GONE);
            bindFile(response);
        }

        @Override
        public void onAllFailure(Throwable t) {
            Timber.e(t, null);
            mProgressView.setVisibility(View.GONE);
            Snackbar.make(mRoot, R.string.file_load_error, Snackbar.LENGTH_SHORT)
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
                        mOption = OPTION_OPEN;
                        checkAccountPermission();
                        return true;
                    case R.id.action_save:
                        mOption = OPTION_SAVE;
                        checkAccountPermission();
                        return true;
                }
                return false;
            }
        });

        loadData();
    }

    private void loadData() {
        mProgressView.setVisibility(View.VISIBLE);
        GitLabClient.instance().getFile(mProjectId, mPath, mRef).enqueue(mRepositoryFileCallback);
    }

    private void bindFile(RepositoryFile repositoryFile) {
        mRepositoryFile = repositoryFile;
        mFileName = repositoryFile.getFileName();
        mToolbar.setTitle(mFileName);
        if (repositoryFile.getSize() > MAX_FILE_SIZE) {
            Snackbar.make(mRoot, R.string.file_too_big, Snackbar.LENGTH_SHORT)
                    .show();
        } else {
            loadBlob(repositoryFile);
        }
    }

    private void loadBlob(RepositoryFile repositoryFile) {
        DecodeObservableFactory.newDecode(repositoryFile.getContent())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<byte[]>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        Snackbar.make(mRoot, R.string.failed_to_load, Snackbar.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onNext(byte[] bytes) {
                        bindBlob(bytes);
                    }
                });
    }

    private void bindBlob(byte[] blob) {
        mBlob = blob;
        String content;
        String mimeType = null;
        String extension = fileExt(mFileName);
        if (extension != null) {
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (mimeType != null) {
                mimeType = mimeType.toLowerCase();
            }
        }

        if (mimeType != null && mimeType.startsWith("image/")) {
            String imageURL = "data:" + mimeType + ";base64," + mRepositoryFile.getContent();

            content = "<!DOCTYPE html>" +
                    "<html>" +
                    "<body>" +
                    "<img style=\"width: 100%;\" src=\"" + imageURL + "\">" +
                    "</body>" +
                    "</html>";
        } else {
            String text = new String(mBlob, Charset.forName("UTF-8"));

            content = "<!DOCTYPE html>" +
                    "<html>" +
                    "<head>" +
                    "<link href=\"github.css\" rel=\"stylesheet\" />" +
                    "</head>" +
                    "<body>" +
                    "<pre><code>" +
                    Html.escapeHtml(text) +
                    "</code></pre>" +
                    "<script src=\"highlight.pack.js\"></script>" +
                    "<script>hljs.initHighlightingOnLoad();</script>" +
                    "</body>" +
                    "</html>";
        }

        mFileBlobView.loadDataWithBaseURL("file:///android_asset/", content, "text/html", "utf8", null);
        mToolbar.inflateMenu(R.menu.file);
    }

    @TargetApi(23)
    private void checkAccountPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            if (mOption == OPTION_SAVE) {
                saveBlob();
            } else {
                openFile();
            }
        } else {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_WRITE_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_WRITE_STORAGE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (mOption == OPTION_SAVE) {
                        saveBlob();
                    } else {
                        openFile();
                    }
                }
            }
        }
    }

    private File saveBlob() {
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state) && mBlob != null) {
            File targetFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), mFileName);

            FileOutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(targetFile);
                outputStream.write(mBlob);

                Snackbar.make(mRoot, getString(R.string.file_saved), Snackbar.LENGTH_SHORT)
                        .show();

                return targetFile;
            } catch (IOException e) {
                Timber.e(e, null);
                Snackbar.make(mRoot, getString(R.string.save_error), Snackbar.LENGTH_SHORT)
                        .show();
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        Timber.e(e, null);
                    }
                }
            }
        } else {
            Snackbar.make(mRoot, getString(R.string.save_error), Snackbar.LENGTH_SHORT)
                    .show();
        }

        return null;
    }

    private void openFile() {
        File file = saveBlob();
        if (file == null) {
            Snackbar.make(mRoot, getString(R.string.open_error), Snackbar.LENGTH_SHORT)
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
            Snackbar.make(mRoot, getString(R.string.open_error), Snackbar.LENGTH_SHORT)
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

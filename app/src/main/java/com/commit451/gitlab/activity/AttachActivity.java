package com.commit451.gitlab.activity;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.commit451.easycallback.EasyCallback;
import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.FileUploadResponse;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.util.FileUtil;

import org.parceler.Parcels;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MultipartBody;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import timber.log.Timber;

/**
 * Attaches files
 */
public class AttachActivity extends BaseActivity {

    public static final String KEY_FILE_UPLOAD_RESPONSE = "response";

    private static final String KEY_PROJECT = "project";

    public static Intent newIntent(Context context, Project project) {
        Intent intent = new Intent(context, AttachActivity.class);
        intent.putExtra(KEY_PROJECT, Parcels.wrap(project));
        return intent;
    }

    private Project mProject;

    @OnClick(R.id.button_choose_photo)
    void onChoosePhotoClicked() {
        EasyImage.openGallery(this, 0);
    }

    @OnClick(R.id.button_take_photo)
    void onTakePhotoClicked() {
        EasyImage.openCamera(this, 0);
    }

    @OnClick(R.id.button_choose_file)
    void onChooseFileClicked() {
        EasyImage.openChooserWithDocuments(this, "Choose file", 0);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attach);
        ButterKnife.bind(this);
        mProject = Parcels.unwrap(getIntent().getParcelableExtra(KEY_PROJECT));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                //Some error handling
            }

            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source, int type) {
                //Handle the image
                onPhotoReturned(imageFile);
            }

            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {
                //Cancel handling, you might wanna remove taken photo if it was canceled
                if (source == EasyImage.ImageSource.CAMERA) {
                    File photoFile = EasyImage.lastlyTakenButCanceledPhoto(AttachActivity.this);
                    if (photoFile != null) {
                        photoFile.delete();
                    }
                }
            }
        });
    }

    private void onPhotoReturned(File photo) {
        MultipartBody.Part part = FileUtil.toPart(photo);
        App.instance().getGitLab().uploadFile(mProject.getId(), part).enqueue(new EasyCallback<FileUploadResponse>() {
            @Override
            public void success(@NonNull FileUploadResponse response) {
                Intent data = new Intent();
                data.putExtra(KEY_FILE_UPLOAD_RESPONSE, Parcels.wrap(response));
                setResult(RESULT_OK, data);
                finish();
            }

            @Override
            public void failure(Throwable t) {
                Timber.e(t);
                finish();
            }
        });

    }
}

package com.commit451.gitlab.activity;


import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.FileUploadResponse;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.rx.CustomSingleObserver;
import com.commit451.gitlab.rx.FileObservableFactory;

import org.parceler.Parcels;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.codetail.animation.ViewAnimationUtils;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
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

    @BindView(R.id.root_buttons)
    ViewGroup mRootButtons;
    @BindView(R.id.progress)
    View mProgress;
    @BindView(R.id.attachCard)
    View mCard;

    Project mProject;

    @OnClick(R.id.root)
    void onRootClicked() {
        onBackPressed();
    }

    @OnClick(R.id.button_choose_photo)
    void onChoosePhotoClicked() {
        EasyImage.openGallery(this, 0, false);
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

        //Run the runnable after the view has been measured
        mCard.post(new Runnable() {
            @Override
            public void run() {
                //we need the radius of the animation circle, which is the diagonal of the view
                float finalRadius = (float) Math.hypot(mCard.getWidth(), mCard.getHeight());

                //it's using a 3rd-party ViewAnimationUtils class for compat reasons (up to API 14)
                Animator animator = ViewAnimationUtils
                        .createCircularReveal(mCard, 0, mCard.getHeight(), 0, finalRadius);
                animator.setDuration(500);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.start();
            }
        });

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
            public void onImagesPicked(List<File> imageFiles, EasyImage.ImageSource source, int type) {
                onPhotoReturned(imageFiles.get(0));
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

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.do_nothing, R.anim.fade_out);
    }

    private void onPhotoReturned(File photo) {
        mProgress.setVisibility(View.VISIBLE);
        mRootButtons.setVisibility(View.INVISIBLE);
        FileObservableFactory.toPart(photo)
                .flatMap(new Function<MultipartBody.Part, SingleSource<FileUploadResponse>>() {
                    @Override
                    public SingleSource<FileUploadResponse> apply(MultipartBody.Part part) throws Exception {
                        return App.get().getGitLab().uploadFile(mProject.getId(), part);
                    }
                })
                .compose(this.<FileUploadResponse>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomSingleObserver<FileUploadResponse>() {

                    @Override
                    public void success(FileUploadResponse fileUploadResponse) {
                        Intent data = new Intent();
                        data.putExtra(KEY_FILE_UPLOAD_RESPONSE, Parcels.wrap(fileUploadResponse));
                        setResult(RESULT_OK, data);
                        finish();
                    }

                    @Override
                    public void error(Throwable t) {
                        Timber.e(t);
                        finish();
                    }
                });
    }
}

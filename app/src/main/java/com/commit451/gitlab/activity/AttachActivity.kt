package com.commit451.gitlab.activity


import android.animation.TimeInterpolator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.extension.getParcelerParcelable
import com.commit451.gitlab.extension.putParcelParcelableExtra
import com.commit451.gitlab.extension.setup
import com.commit451.gitlab.extension.toPart
import com.commit451.gitlab.model.api.FileUploadResponse
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.rx.CustomSingleObserver
import io.codetail.animation.ViewAnimationUtils
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import timber.log.Timber
import java.io.File

/**
 * Attaches files
 */
class AttachActivity : BaseActivity() {

    companion object {

        val KEY_FILE_UPLOAD_RESPONSE = "response"

        private val KEY_PROJECT = "project"

        fun newIntent(context: Context, project: Project): Intent {
            val intent = Intent(context, AttachActivity::class.java)
            intent.putParcelParcelableExtra(KEY_PROJECT, project)
            return intent
        }
    }

    @BindView(R.id.root_buttons) lateinit var rootButtons: ViewGroup
    @BindView(R.id.progress) lateinit var progress: View
    @BindView(R.id.attachCard) lateinit var card: View

    var project: Project?= null

    @OnClick(R.id.root)
    fun onRootClicked() {
        onBackPressed()
    }

    @OnClick(R.id.button_choose_photo)
    fun onChoosePhotoClicked() {
        EasyImage.openGallery(this, 0, false)
    }

    @OnClick(R.id.button_take_photo)
    fun onTakePhotoClicked() {
        EasyImage.openCamera(this, 0)
    }

    @OnClick(R.id.button_choose_file)
    fun onChooseFileClicked() {
        EasyImage.openChooserWithDocuments(this, "Choose file", 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attach)
        ButterKnife.bind(this)

        //Run the runnable after the view has been measured
        card.post {
            //we need the radius of the animation circle, which is the diagonal of the view
            val finalRadius = Math.hypot(card.width.toDouble(), card.height.toDouble()).toFloat()

            //it's using a 3rd-party ViewAnimationUtils class for compat reasons (up to API 14)
            val animator = ViewAnimationUtils
                    .createCircularReveal(card, 0, card.height, 0f, finalRadius)
            animator.duration = 500
            animator.interpolator = AccelerateDecelerateInterpolator() as TimeInterpolator?
            animator.start()
        }

        project = intent.getParcelerParcelable<Project>(KEY_PROJECT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        EasyImage.handleActivityResult(requestCode, resultCode, data, this, object : DefaultCallback() {
            override fun onImagePickerError(e: Exception?, source: EasyImage.ImageSource?, type: Int) {
                //Some error handling
            }

            override fun onImagesPicked(imageFiles: List<File>, source: EasyImage.ImageSource, type: Int) {
                onPhotoReturned(imageFiles[0])
            }

            override fun onCanceled(source: EasyImage.ImageSource?, type: Int) {
                //Cancel handling, you might wanna remove taken photo if it was canceled
                if (source == EasyImage.ImageSource.CAMERA) {
                    val photoFile = EasyImage.lastlyTakenButCanceledPhoto(this@AttachActivity)
                    photoFile?.delete()
                }
            }
        })
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.do_nothing, R.anim.fade_out)
    }

    fun onPhotoReturned(photo: File) {
        progress.visibility = View.VISIBLE
        rootButtons.visibility = View.INVISIBLE
        photo.toPart()
                .flatMap { part -> App.get().gitLab.uploadFile(project!!.id, part) }
                .setup(bindToLifecycle())
                .subscribe(object : CustomSingleObserver<FileUploadResponse>() {

                    override fun success(fileUploadResponse: FileUploadResponse) {
                        val data = Intent()
                        data.putParcelParcelableExtra(KEY_FILE_UPLOAD_RESPONSE, fileUploadResponse)
                        setResult(Activity.RESULT_OK, data)
                        finish()
                    }

                    override fun error(t: Throwable) {
                        Timber.e(t)
                        finish()
                    }
                })
    }
}

package com.commit451.gitlab.activity


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateDecelerateInterpolator
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.extension.toPart
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.Project
import kotlinx.android.synthetic.main.activity_attach.*
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import timber.log.Timber
import java.io.File

/**
 * Attaches files
 */
class AttachActivity : BaseActivity() {

    companion object {

        const val KEY_FILE_UPLOAD_RESPONSE = "response"

        private const val KEY_PROJECT = "project"

        fun newIntent(context: Context, project: Project): Intent {
            val intent = Intent(context, AttachActivity::class.java)
            intent.putExtra(KEY_PROJECT, project)
            return intent
        }
    }

    private var project: Project? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attach)

        root.setOnClickListener {
            onBackPressed()
        }
        buttonTakePhoto.setOnClickListener {
            EasyImage.openCameraForImage(this, 0)
        }
        buttonChoosePhoto.setOnClickListener {
            EasyImage.openGallery(this, 0)
        }
        buttonChooseFile.setOnClickListener {
            EasyImage.openChooserWithDocuments(this, "Choose file", 0)
        }
        reveal()

        project = intent.getParcelableExtra(KEY_PROJECT)
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
                if (source == EasyImage.ImageSource.CAMERA_IMAGE) {
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

    private fun reveal() {
        //Run the runnable after the view has been measured
        card.post {
            //we need the radius of the animation circle, which is the diagonal of the view
            val finalRadius = Math.hypot(card.width.toDouble(), card.height.toDouble()).toFloat()

            //it's using a 3rd-party ViewAnimationUtils class for compat reasons (up to API 14)
            val animator = ViewAnimationUtils
                    .createCircularReveal(card, 0, card.height, 0f, finalRadius)
            animator.duration = 500
            animator.interpolator = AccelerateDecelerateInterpolator()
            animator.start()
        }
    }

    fun onPhotoReturned(photo: File) {
        progress.visibility = View.VISIBLE
        rootButtons.visibility = View.INVISIBLE
        photo.toPart()
                .flatMap { part -> App.get().gitLab.uploadFile(project!!.id, part) }
                .with(this)
                .subscribe({
                    val data = Intent()
                    data.putExtra(KEY_FILE_UPLOAD_RESPONSE, it)
                    setResult(Activity.RESULT_OK, data)
                    finish()
                }, {
                    Timber.e(it)
                    finish()
                })
    }
}

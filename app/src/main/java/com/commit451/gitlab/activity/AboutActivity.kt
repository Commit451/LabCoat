package com.commit451.gitlab.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import coil.api.load
import com.commit451.gimbal.Gimbal
import com.commit451.gitlab.App
import com.commit451.gitlab.BuildConfig
import com.commit451.gitlab.R
import com.commit451.gitlab.api.GitLab
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.Account
import com.commit451.gitlab.model.api.Contributor
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.rx.CustomSingleObserver
import com.commit451.gitlab.util.ImageUtil
import com.commit451.gitlab.util.IntentUtil
import com.google.android.material.snackbar.Snackbar
import com.jawnnypoo.physicslayout.Physics
import com.jawnnypoo.physicslayout.PhysicsConfig
import com.wefika.flowlayout.FlowLayout
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.progress.*
import org.jbox2d.common.Vec2
import timber.log.Timber

/**
 * That's what its all about
 */
class AboutActivity : BaseActivity() {

    companion object {
        private const val REPO_ID = "473568"

        fun newIntent(context: Context): Intent {
            return Intent(context, AboutActivity::class.java)
        }
    }

    private lateinit var sensorManager: SensorManager
    private lateinit var gimbal: Gimbal
    private var gravitySensor: Sensor? = null

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_GRAVITY) {
                if (physicsLayout.physics.world != null) {
                    gimbal.normalizeGravityEvent(event)
                    physicsLayout.physics.world.gravity = Vec2(-event.values[0], event.values[1])
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gimbal = Gimbal(this)
        gimbal.lock()
        setContentView(R.layout.activity_about)
        toolbar.setNavigationIcon(R.drawable.ic_back_24dp)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        toolbar.setTitle(R.string.about)
        toolbar.subtitle = BuildConfig.VERSION_NAME
        physicsLayout.physics.enableFling()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        var gitLab = App.get().gitLab
        val gitLabUrl = getString(R.string.url_gitlab)
        if (!gitLab.account.serverUrl.toString().contains(gitLabUrl)) {
            val account = Account()
            account.serverUrl = gitLabUrl
            gitLab = GitLab.Builder(account)
                    .build()
        }
        gitLab.getContributors(REPO_ID)
                .with(this)
                .subscribe(object : CustomSingleObserver<List<Contributor>>() {

                    override fun error(t: Throwable) {
                        Timber.e(t)
                        progress.visibility = View.GONE
                        Snackbar.make(root, R.string.failed_to_load_contributors, Snackbar.LENGTH_SHORT)
                                .show()
                    }

                    override fun success(contributors: List<Contributor>) {
                        progress.visibility = View.GONE
                        addContributors(contributors)
                    }
                })
        progress.visibility = View.VISIBLE
        sauce.setOnClickListener {
            if (getString(R.string.url_gitlab) == App.get().getAccount().serverUrl.toString()) {
                Navigator.navigateToProject(this@AboutActivity, REPO_ID)
            } else {
                IntentUtil.openPage(this@AboutActivity, getString(R.string.source_url))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(sensorEventListener, gravitySensor, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(sensorEventListener)
    }

    override fun hasBrowsableLinks(): Boolean {
        return true
    }

    fun addContributors(contributors: List<Contributor>) {
        val config = PhysicsConfig.create()
        config.shapeType = PhysicsConfig.SHAPE_TYPE_CIRCLE
        val borderSize = resources.getDimensionPixelSize(R.dimen.border_size)
        val imageSize = resources.getDimensionPixelSize(R.dimen.circle_size)
        for (i in contributors.indices) {
            val contributor = contributors[i]
            val imageView = CircleImageView(this)
            val llp = FlowLayout.LayoutParams(
                    imageSize,
                    imageSize)
            imageView.layoutParams = llp
            imageView.borderWidth = borderSize
            imageView.borderColor = Color.BLACK
            Physics.setPhysicsConfig(imageView, config)
            physicsLayout.addView(imageView)

            val url = ImageUtil.getAvatarUrl(contributor.email, imageSize)
            imageView.load(url)
        }
        physicsLayout.requestLayout()
    }
}

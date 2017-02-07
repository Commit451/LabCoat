package com.commit451.gitlab.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.commit451.gimbal.Gimbal
import com.commit451.gitlab.App
import com.commit451.gitlab.BuildConfig
import com.commit451.gitlab.R
import com.commit451.gitlab.extension.setup
import com.commit451.gitlab.model.api.Contributor
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.rx.CustomSingleObserver
import com.commit451.gitlab.util.ImageUtil
import com.commit451.gitlab.util.IntentUtil
import com.commit451.gitlab.view.PhysicsFlowLayout
import com.jawnnypoo.physicslayout.Physics
import com.jawnnypoo.physicslayout.PhysicsConfig
import com.wefika.flowlayout.FlowLayout
import de.hdodenhof.circleimageview.CircleImageView
import org.jbox2d.common.Vec2
import timber.log.Timber

/**
 * Thats what its all about
 */
class AboutActivity : BaseActivity() {

    companion object {
        private val REPO_ID = "473568"

        fun newIntent(context: Context): Intent {
            val intent = Intent(context, AboutActivity::class.java)
            return intent
        }
    }

    @BindView(R.id.root) lateinit var root: ViewGroup
    @BindView(R.id.toolbar) lateinit var toolbar: Toolbar
    @BindView(R.id.contributors) lateinit var textContributors: TextView
    @BindView(R.id.physics_layout) lateinit var physicsLayout: PhysicsFlowLayout
    @BindView(R.id.progress) lateinit var progress: View

    lateinit var sensorManager: SensorManager
    lateinit var gimbal: Gimbal
    var gravitySensor: Sensor? = null

    val sensorEventListener = object : SensorEventListener {
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

    @OnClick(R.id.sauce)
    fun onSauceClick() {
        if (getString(R.string.url_gitlab) == App.get().getAccount().serverUrl.toString()) {
            Navigator.navigateToProject(this@AboutActivity, REPO_ID)
        } else {
            IntentUtil.openPage(this@AboutActivity, getString(R.string.source_url))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gimbal = Gimbal(this)
        gimbal.lock()
        setContentView(R.layout.activity_about)
        ButterKnife.bind(this)
        toolbar.setNavigationIcon(R.drawable.ic_back_24dp)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        toolbar.setTitle(R.string.about)
        toolbar.subtitle = BuildConfig.VERSION_NAME
        physicsLayout.physics.enableFling()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        App.get().gitLab.getContributors(REPO_ID)
                .setup(bindToLifecycle())
                .subscribe(object : CustomSingleObserver<List<Contributor>>() {

                    override fun error(t: Throwable) {
                        Timber.e(t)
                        progress.visibility = View.GONE
                        Snackbar.make(root, R.string.failed_to_load_contributors, Snackbar.LENGTH_SHORT)
                                .show()
                    }

                    override fun success(contributors: List<Contributor>) {
                        progress.visibility = View.GONE
                        addContributors(Contributor.groupContributors(contributors))
                    }
                })
        progress.visibility = View.VISIBLE
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
            App.get().picasso
                    .load(url)
                    .into(imageView)
        }
        physicsLayout.requestLayout()
    }
}

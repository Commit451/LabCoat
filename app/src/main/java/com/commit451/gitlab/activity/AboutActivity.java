package com.commit451.gitlab.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commit451.easycallback.EasyCallback;
import com.commit451.gimbal.Gimbal;
import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.Contributor;
import com.commit451.gitlab.navigation.Navigator;
import com.commit451.gitlab.util.ImageUtil;
import com.commit451.gitlab.util.IntentUtil;
import com.commit451.gitlab.view.PhysicsFlowLayout;
import com.jawnnypoo.physicslayout.Physics;
import com.jawnnypoo.physicslayout.PhysicsConfig;
import com.wefika.flowlayout.FlowLayout;

import org.jbox2d.common.Vec2;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Callback;
import timber.log.Timber;

/**
 * Thats what its all about
 */
public class AboutActivity extends BaseActivity {
    private static final String REPO_ID = "473568";

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, AboutActivity.class);
        return intent;
    }

    @BindView(R.id.root)
    ViewGroup mRoot;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.contributors)
    TextView mContributors;
    @BindView(R.id.physics_layout)
    PhysicsFlowLayout mPhysicsLayout;
    @BindView(R.id.progress)
    View mProgress;

    @OnClick(R.id.sauce)
    void onSauceClick() {
        if (getString(R.string.url_gitlab).equals(App.get().getAccount().getServerUrl().toString())) {
            Navigator.navigateToProject(AboutActivity.this, REPO_ID);
        } else {
            IntentUtil.openPage(AboutActivity.this, getString(R.string.source_url));
        }
    }

    SensorManager sensorManager;
    Sensor gravitySensor;
    Gimbal mGimbal;

    private final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
                if (mPhysicsLayout.getPhysics().getWorld() != null) {
                    mGimbal.normalizeGravityEvent(event);
                    mPhysicsLayout.getPhysics().getWorld().setGravity(new Vec2(-event.values[0], event.values[1]));
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private Callback<List<Contributor>> mContributorResponseCallback = new EasyCallback<List<Contributor>>() {
        @Override
        public void success(@NonNull List<Contributor> response) {
            mProgress.setVisibility(View.GONE);
            addContributors(Contributor.groupContributors(response));
        }

        @Override
        public void failure(Throwable t) {
            Timber.e(t);
            mProgress.setVisibility(View.GONE);
            Snackbar.make(mRoot, R.string.failed_to_load_contributors, Snackbar.LENGTH_SHORT)
                    .show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGimbal = new Gimbal(this);
        mGimbal.lock();
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        mToolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mToolbar.setTitle(R.string.about);
        mPhysicsLayout.getPhysics().enableFling();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        App.get().getGitLab().getContributors(REPO_ID).enqueue(mContributorResponseCallback);
        mProgress.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(sensorEventListener, gravitySensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorEventListener);
    }

    private void addContributors(List<Contributor> contributors) {
        PhysicsConfig config = PhysicsConfig.create();
        config.shapeType = PhysicsConfig.SHAPE_TYPE_CIRCLE;
        int borderSize = getResources().getDimensionPixelSize(R.dimen.border_size);
        int imageSize = getResources().getDimensionPixelSize(R.dimen.circle_size);
        for (int i=0; i<contributors.size(); i++) {
            Contributor contributor = contributors.get(i);
            CircleImageView imageView = new CircleImageView(this);
            FlowLayout.LayoutParams llp = new FlowLayout.LayoutParams(
                    imageSize,
                    imageSize);
            imageView.setLayoutParams(llp);
            imageView.setBorderWidth(borderSize);
            imageView.setBorderColor(Color.BLACK);
            Physics.setPhysicsConfig(imageView, config);
            mPhysicsLayout.addView(imageView);

            Uri url = ImageUtil.getAvatarUrl(contributor.getEmail(), imageSize);
            App.get().getPicasso()
                    .load(url)
                    .into(imageView);
        }
        mPhysicsLayout.requestLayout();
    }
}

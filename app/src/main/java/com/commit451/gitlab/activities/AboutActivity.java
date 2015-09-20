package com.commit451.gitlab.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.Contributor;
import com.commit451.gitlab.tools.ImageUtil;
import com.commit451.gitlab.tools.IntentUtil;
import com.commit451.gitlab.tools.WindowUtil;
import com.jawnnypoo.physicslayout.Physics;
import com.jawnnypoo.physicslayout.PhysicsConfig;
import com.jawnnypoo.physicslayout.PhysicsFrameLayout;
import com.squareup.picasso.Picasso;

import org.jbox2d.common.Vec2;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit.Callback;
import retrofit.Response;
import timber.log.Timber;

/**
 * Thats what its all about
 * Created by Jawn on 8/25/2015.
 */
public class AboutActivity extends BaseActivity {
    private static final long REPO_ID = 473568;

    public static Intent newInstance(Context context) {
        Intent intent = new Intent(context, AboutActivity.class);
        return intent;
    }

    @Bind(R.id.root) View root;
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.toolbar_title) TextView toolbarTitle;
    @Bind(R.id.contributors) TextView contributors;
    @Bind(R.id.physics_layout) PhysicsFrameLayout physicsLayout;
    @OnClick(R.id.sauce)
    void onSauceClick() {
        IntentUtil.openPage(root, getString(R.string.source_url));
    }

    SensorManager sensorManager;
    Sensor gravitySensor;

    private final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
                if (physicsLayout.getPhysics().getWorld() != null) {
                    WindowUtil.normalizeForOrientation(getWindow(), event);
                    physicsLayout.getPhysics().getWorld().setGravity(new Vec2(-event.values[0], event.values[1]));
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) { }
    };

    private final Callback<List<Contributor>> contributorResponseCallback = new Callback<List<Contributor>>() {

        @Override
        public void onResponse(Response<List<Contributor>> response) {
            if (response.isSuccess()) {
                addContributors(response.body());
            }
        }

        @Override
        public void onFailure(Throwable t) {
            Snackbar.make(getWindow().getDecorView(), R.string.failed_to_load_contributors, Snackbar.LENGTH_SHORT)
                    .show();
            Timber.e(t.toString());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowUtil.lockToCurrentOrientation(this);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbarTitle.setText(R.string.about);
        physicsLayout.getPhysics().enableFling();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        GitLabClient.instance().getContributors(REPO_ID).enqueue(contributorResponseCallback);
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
        PhysicsConfig config = new PhysicsConfig.Builder()
                .setShapeType(PhysicsConfig.ShapeType.CIRCLE)
                .setDensity(1.0f)
                .setFriction(0.0f)
                .setRestitution(0.0f)
                .build();
        int borderSize = getResources().getDimensionPixelSize(R.dimen.border_size);
        int x = 0;
        int y = 0;
        int imageSize = getResources().getDimensionPixelSize(R.dimen.circle_size);
        for (int i=0; i<contributors.size(); i++) {
            Contributor contributor = contributors.get(i);
            CircleImageView imageView = new CircleImageView(this);
            FrameLayout.LayoutParams llp = new FrameLayout.LayoutParams(
                    imageSize,
                    imageSize);
            imageView.setLayoutParams(llp);
            imageView.setBorderWidth(borderSize);
            imageView.setBorderColor(Color.BLACK);
            Physics.setPhysicsConfig(imageView, config);
            physicsLayout.addView(imageView);
            imageView.setX(x);
            imageView.setY(y);

            x = (x + imageSize);
            if (x > physicsLayout.getWidth()) {
                x = 0;
                y = (y + imageSize) % physicsLayout.getHeight();
            }
            String url = ImageUtil.getGravatarUrl(contributor.getEmail(), imageSize);
            Picasso.with(this)
                    .load(url)
                    .into(imageView);
        }
        physicsLayout.getPhysics().onLayout(true);
    }
}

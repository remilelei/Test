package com.example.remilelei.sometest.shake;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.remilelei.sometest.R;

public class ShakeActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAcSensor;
    private SoundPool mSoundPool;

    private Vibrator mVibrator;

    ImageView lineTop;
    ImageView lineBottom;
    LinearLayout top;
    LinearLayout bottom;

    private final ShakingHandler shakingHandlerr = new ShakingHandler(this);

    public boolean isShaking;
    private int sound;

    final public int WHAT_SHAKING_1 = 1001;
    final public int WHAT_SHAKING_2 = 1002;
    final public int WHAT_SHAKING_3 = 1003;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shake);

        // init sensor manager
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        mSoundPool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 5);
        sound = mSoundPool.load(this, R.raw.weichat_audio, 1);

        // init views
        initView();

    }

    @Override
    protected void onResume() {
        super.onResume();

        // register sensor listener
        if(mSensorManager != null) {
            mAcSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if(mAcSensor != null) {
                mSensorManager.registerListener(this, mAcSensor, SensorManager.SENSOR_DELAY_UI);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // unregister sensor listener
        if(mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
    }

    private void initView () {
        lineTop = (ImageView) findViewById(R.id.iv_line_top);
        lineBottom = (ImageView) findViewById(R.id.iv_line_bottom);
        top = (LinearLayout) findViewById(R.id.ll_top);
        bottom = (LinearLayout) findViewById(R.id.ll_bottom);

        lineTop.setVisibility(View.GONE);
        lineBottom.setVisibility(View.GONE);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] values = event.values;
        float x = values[0];
        float y = values[1];
        float z = values[2];
        // if device is shaking, handle it and won't handle other shaking.
        if((x >= 17 || y >=17 || z >= 17) && !isShaking) {
            Log.d("test", "you shaked!");
            isShaking = true;
            Runnable job = new Runnable() {
                @Override
                public void run() {
                    // SHAKING1 500ms SHAKING2 500ms SHAKING3
                    try {
                        shakingHandlerr.obtainMessage(WHAT_SHAKING_1).sendToTarget();
                        Thread.currentThread().sleep(500);
                        shakingHandlerr.obtainMessage(WHAT_SHAKING_2).sendToTarget();
                        Thread.currentThread().sleep(500);
                        shakingHandlerr.obtainMessage(WHAT_SHAKING_3).sendToTarget();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            new Thread(job).start();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void playAnimation(boolean shakeBegin) {
        float topToY = 0;
        float bottomToY = 0;
        float topFromY = 0;
        float bottomFromY = 0;
        int i = shakeBegin? 1 : -1;
        int type = Animation.RELATIVE_TO_SELF;
        topFromY = -.25f + .25f * i;
        bottomFromY = .25f - .25f * i;
        topToY = -.25f - .25f * i;
        bottomToY = .25f + .25f * i;
        TranslateAnimation topAnimation = new TranslateAnimation(type, 0, type, 0, type, topFromY, type, topToY);
        topAnimation.setDuration(200);
        topAnimation.setFillAfter(true);
        TranslateAnimation bottomAnimation = new TranslateAnimation(type, 0, type, 0, type, bottomFromY, type, bottomToY);
        bottomAnimation.setDuration(200);
        bottomAnimation.setFillAfter(true);

        if(!shakeBegin) {
            topAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    lineTop.setVisibility(View.GONE);
                    lineBottom.setVisibility(View.GONE);
                }
                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }

        top.startAnimation(topAnimation);
        bottom.startAnimation(bottomAnimation);
    }

    class ShakingHandler extends Handler {

        private ShakeActivity activity;

        public ShakingHandler(ShakeActivity activity) {
            this.activity = activity;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_SHAKING_1 : {
                    // play animation to split top and bottom and vibrator and make sound
                    lineTop.setVisibility(View.VISIBLE);
                    lineBottom.setVisibility(View.VISIBLE);
                    playAnimation(true);
                    mVibrator.vibrate(300);
                    mSoundPool.play(sound, 1, 1, 0, 0, 1);
                }
                break;
                case WHAT_SHAKING_2 : {
                    // vibrator
                    mVibrator.vibrate(300);
                }
                break;
                case WHAT_SHAKING_3 : {
                    // play ending animation, and reset parameter 'isShaking'
                    isShaking = false;
                    playAnimation(false);
                }
                break;
            }
        }
    }
}

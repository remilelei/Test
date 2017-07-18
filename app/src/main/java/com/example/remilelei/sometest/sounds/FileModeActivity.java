package com.example.remilelei.sometest.sounds;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.remilelei.sometest.R;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileModeActivity extends AppCompatActivity implements View.OnTouchListener {

    // UI views
    TextView tv_show;
    TextView tv_btn_speak;

    // android component to record sounds
    private ExecutorService recordService;
    private MediaRecorder recorder;
    private File sounds;

    int width, height;
    boolean isInside;
    long startTime, endTime;

    // this activity need some permissions
    final private String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };
    final private int PERMISSION_WRITE_EXTERNAL_STORAGE_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_mode);

        // init views
        initView();

        // init executor service
        recordService = Executors.newSingleThreadExecutor();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // shut down executor service when this activity is closed
        recordService.shutdownNow();

        // release recorder if recorder is exist
        releaseRecorder();
    }

    /**
     * get instances of views
     */
    private void initView() {
        tv_show = (TextView) findViewById(R.id.tv_show);
        tv_btn_speak = (TextView) findViewById(R.id.tv_btn_speak);
        tv_btn_speak.setOnTouchListener(this);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // TODO start record and change button UI
                // start record
                startRecord();
                // change button UI
                tv_btn_speak.setText(R.string.rcd_pressed);
                tv_btn_speak.setBackgroundResource(R.color.colorPrimary);
                break;
            case MotionEvent.ACTION_UP:
                // TODO stop record and change button UI
                // stop record
                stopRecord();
                // change button UI
                tv_btn_speak.setText(R.string.rcd_unpressed);
                tv_btn_speak.setBackgroundResource(R.color.colorAccent);
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.i("FileModeActivity", "CANCEL");
                break;
            case MotionEvent.ACTION_OUTSIDE:
                Log.i("FileModeActivity", "OUTSIDE");
                break;
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                float y = event.getY();
                width = tv_btn_speak.getWidth();
                height = tv_btn_speak.getHeight();
                if(x < 0 || x > width ||
                        y < 0 || y > height) {
                   if(isInside) {
                       Log.i("FileModeActivity", "turn to OUTSIDE");
                       isInside = false;
                   }
                } else if(!isInside) {
                    Log.i("FileModeActivity", "turn to INSIDE");
                    isInside = true;
                }
                break;
        }
        return true;
    }

    /**
     * TODO do record in service
     */
    private void startRecord() {
        Runnable job = new Runnable() {
            @Override
            /**
             * release MediaRecorder at first, then let's record sounds
             * if catch an exception, alert something and stop record
             */
            public void run() {
                // 1. release MediaRecorder
                releaseRecorder();
                // 2. record sounds
                if(!doRecord()) {
                    recordFail();
                }
            }
        };
        recordService.submit(job);
    }

    /**
     * TODO stop the record job in service
     */
    private void stopRecord() {
        Runnable job = new Runnable() {
            @Override
            /**
             * stop record and save sounds. if failed, alert user.
             * and then release this recorder
             */
            public void run() {
                // 1. stop record and alert when exception happened
                if(!saveSound()) {
                    recordFail();
                }

                // 2. release recorder
                releaseRecorder();
            }
        };
        recordService.submit(job);
    }

    /**
     * TODO record sound into a file
     * 1. create a MediaRecorder instance
     * 2. verify permission and create a file for store sounds
     * 3. config this MediaRecorder instance
     * 4. start record sounds and do a time count
     * and
     * don't call this method in main thread
     * @return is record success
     */
    private boolean doRecord() {
        try {
            // 1. create a MediaRecorder
            recorder = new MediaRecorder();

            // 2. verify permission and create a file for store sounds
            if(verifyPermission()) {
                sounds = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/remile/sound" + System.currentTimeMillis() + ".mp3");
                File parent = sounds.getParentFile();
                if(!parent.exists()) {
                    if(sounds.getParentFile().mkdir()) {
                        Log.i("FileModeActivity", "parent dir create success");
                        if(sounds.createNewFile()) {
                            Log.i("FileModeActivity", "record file create success");
                        } else {
                            Log.e("FileModeActivity", "record file create failed");
                        }
                    } else {
                        Log.e("FileModeActivity", "parent dir create failed");
                    }
                } else {
                    if(sounds.createNewFile()) {
                        Log.i("FileModeActivity", "record file create success");
                    } else {
                        Log.e("FileModeActivity", "record file create failed");
                    }
                }
            } else {
                ActivityCompat.requestPermissions(this, PERMISSIONS,
                        PERMISSION_WRITE_EXTERNAL_STORAGE_CODE);
                throw new IllegalStateException("app have not storage permission!");
            }
            Log.i("FileModeActivity", "the file is built, prepared to record");

            // 3. config this MediaRecorder instance
            // 3.1 we get sounds from mic
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            // 3.2 choose a format to save sounds file
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            // 3.3 set simple rate (44100 is supported by every android device)
            recorder.setAudioSamplingRate(44100);
            // 3.4 set Encoder----AAC
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            // 3.5 Encoding bit rate----96000
            recorder.setAudioEncodingBitRate(96000);
            // 3.6 set target file to store sounds
            recorder.setOutputFile(sounds.getAbsolutePath());

            // 4. tart record sounds and do a time count
            // 4.1 prepare to record
            recorder.prepare();
            // 4.2 start to record
            recorder.start();
            // 4.3 time count
            startTime = System.currentTimeMillis();
            Log.i("FileModeActivity", "record started.");
        } catch (IOException | IllegalStateException e) {
            Log.e("FileModeActivity", "record failed!");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * TODO stop record and save sounds
     * 1. stop record
     * 2. time count and ignore sounds too short
     * 3. change UI, show record result
     * @return is sound save success
     */
    private boolean saveSound() {
        try {
            // 1. stop record
            recorder.stop();
            Log.i("FileModeActivity", "record stoped.");

            // 2. time count and ignore sounds too short
            endTime = System.currentTimeMillis();
            final int sec = (int)(endTime - startTime) / 1000;
            if(sec < 1 || sec > 30) throw new Exception("illegal record time=" + sec);

            // 3. change UI, show record result
            Runnable job = new Runnable() {
                @Override
                public void run() {
                    tv_show.setText("record success, len=" + sec + "s");
                }
            };
            runOnUiThread(job);
        } catch (Exception e) {
            Log.e("FileModeActivity", "save sounds failed!");
            e.printStackTrace();
            return false;
        }
        Log.i("FileModeActivity", "record " + (sounds.exists()? "success":"failed") + ", save path=" + sounds.getAbsolutePath());
        return true;
    }

    /**
     *
     * @return
     */
    private boolean verifyPermission() {
        int permission_write = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission_record = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);
        if(permission_write != PackageManager.PERMISSION_GRANTED ||
                permission_record != PackageManager.PERMISSION_GRANTED)  return false;
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_WRITE_EXTERNAL_STORAGE_CODE) {

        }
    }

    /**
     * TODO call this method when record is failed
     * 1. clear sounds file
     * 2. make a toast to tell user record is failed
     */
    private void recordFail() {
        // 1. clear sounds file
        if(sounds != null && sounds.exists()) {
            sounds.delete();
        }
        sounds = null;

        // 2. make a toast to tell user record is failed
        runOnUiThread(new Runnable() { // make toast in main thread
            @Override
            public void run() {
                Toast.makeText(FileModeActivity.this.getApplicationContext(),
                        getResources().getString(R.string.rcd_fail),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void releaseRecorder() {
        if(recorder != null) {
            recorder.release();
            recorder = null;
        }
    }
}

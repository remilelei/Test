package com.example.remilelei.sometest.sounds;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
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
import java.io.FileOutputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RunnableFuture;

public class StreamModeActivity extends AppCompatActivity implements View.OnTouchListener {

    final public static String TAG = "StreamModeActivity";
    
    // UI views
    TextView tv_show;
    TextView tv_btn_speak;

    // the size of buffer
    final private static int BUFFER_SIZE = 1024;
    final private int PERMISSION_WRITE_EXTERNAL_STORAGE_CODE = 1;

    int width, height;
    boolean isInside;
    long startTime, endTime;
    volatile boolean isRecording = false;
    byte[] buffer;

    File sounds;
    FileOutputStream outputStream;
    ExecutorService executorService;
    AudioRecord recorder;

    // this activity need some permissions
    final private String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_mode);

        // init views
        initView();

        buffer = new byte[BUFFER_SIZE];
        executorService = Executors.newSingleThreadExecutor();

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
                // start record and change button UI
                // start record
                startRecord();
                // change button UI
                tv_btn_speak.setText(R.string.rcd_pressed);
                tv_btn_speak.setBackgroundResource(R.color.colorPrimary);
                break;
            case MotionEvent.ACTION_UP:
                // stop record and change button UI
                // stop record
                stopRecord();
                // change button UI
                tv_btn_speak.setText(R.string.rcd_unpressed);
                tv_btn_speak.setBackgroundResource(R.color.colorAccent);
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.i(TAG, "CANCEL");
                break;
            case MotionEvent.ACTION_OUTSIDE:
                Log.i(TAG, "OUTSIDE");
                break;
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                float y = event.getY();
                width = tv_btn_speak.getWidth();
                height = tv_btn_speak.getHeight();
                if(x < 0 || x > width ||
                        y < 0 || y > height) {
                    if(isInside) {
                        Log.i(TAG, "turn to OUTSIDE");
                        isInside = false;
                    }
                } else if(!isInside) {
                    Log.i(TAG, "turn to INSIDE");
                    isInside = true;
                }
                break;
        }
        return true;
    }

    /**
     * start record in sub thread
     */
    private void startRecord() {
        isRecording = true;
        Runnable job = new Runnable() {
            @Override
            public void run() {
                releaseRecorder();
                if(!doRecord()) {
                    recordFail();
                }
            }
        };
        executorService.submit(job);
    }

    /**
     * call this method when record is failed
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
                Toast.makeText(StreamModeActivity.this.getApplicationContext(),
                        getResources().getString(R.string.rcd_fail),
                        Toast.LENGTH_SHORT).show();
            }
        });

        // 3. reset state to record
        isRecording = false;
    }

    /**
     * record audio
     * 1. verify permission then create record file
     * 2. create output stream
     * 3. config AudioRecord
     * 4. start record and mark time point
     * 5. get data from AudioRecord and write to output stream(by loop)
     * 6. exit loop and stop record and release source
     * @return
     */
    private boolean doRecord() {
        try {
            // 1. verify permission then create record file
            if(verifyPermission()) { // verify permission
                // create record file
                sounds = new File(Environment.getExternalStorageDirectory() 
                        + "/remile/Ssound" + System.currentTimeMillis() + ".mp3");
                File parent = sounds.getParentFile();
                if(!parent.exists()) {
                    if(sounds.getParentFile().mkdir()) {
                        Log.i(TAG, "parent dir create success");
                        if(sounds.createNewFile()) {
                            Log.i(TAG, "record file create success");
                        } else {
                            Log.e(TAG, "record file create failed");
                        }
                    } else {
                        Log.e(TAG, "parent dir create failed");
                    }
                } else {
                    if(sounds.createNewFile()) {
                        Log.i(TAG, "record file create success");
                    } else {
                        Log.e(TAG, "record file create failed");
                    }
                }
            } else {
                ActivityCompat.requestPermissions(this, PERMISSIONS,
                        PERMISSION_WRITE_EXTERNAL_STORAGE_CODE);
                throw new Exception("no permission to create file");
            }
            Log.i(TAG, "the file is built, prepared to record");

            // 2. create output stream
            outputStream = new FileOutputStream(sounds);

            // 3. config AudioRecord
            int audioSource     = MediaRecorder.AudioSource.MIC;
            int sampleRate      = 44100;
            int channelConfig   = AudioFormat.CHANNEL_IN_MONO;
            int audioFormat     = AudioFormat.ENCODING_PCM_16BIT;
            int minBufferSize   = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
            recorder = new AudioRecord(audioSource, sampleRate, channelConfig, audioFormat,
                    Math.max(minBufferSize, BUFFER_SIZE));

            // 4. start record and mark time point
            recorder.startRecording();
            startTime = System.currentTimeMillis();

            // 5. get data from AudioRecord and write to output stream(by loop)
            while(isRecording) {
                int pos = recorder.read(buffer, 0, BUFFER_SIZE);
                if(pos > 0) {
                    outputStream.write(buffer, 0, pos);
                } else {
                    throw new Exception("reader recorder failed");
                }
            }

            // 6. exit loop and stop record and release source
            return finishRecord();
        } catch (Exception e) {
            Log.e(TAG, "record failed!");
            e.printStackTrace();
            return false;
        } finally {
            releaseRecorder(); // release audio recorder
        }
    }

    /**
     * call this method to finish record;
     * 1. stop record, close output stream
     * 2. calculate record time
     * 3. show record result if this sound is not too short
     * @return is record finished successful
     */
    private boolean finishRecord() {
        try {
            // * 1. stop record, close output stream
            recorder.stop();
            outputStream.close();

            // * 2. calculate record time
            endTime = System.currentTimeMillis();

            // * 3. show record result if this sound is not too short
            final int sec = (int) ((endTime - startTime) / 1000);
            if(sec >= 1) {
                Runnable job = new Runnable() {
                    @Override
                    public void run() {
                        tv_show.setText("record success, len=" + sec + "s");
                        Log.i(TAG, "record success, save file:" + sounds.getAbsolutePath());
                    }
                };
                runOnUiThread(job);
            } else {
                throw new Exception("too short");
            }
        } catch (Exception e) {
            Log.e(TAG, "catch exception when finishRecord");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * release AudioRecord source
     */
    private void releaseRecorder() {
        if(recorder != null) {
            recorder.release();
            recorder = null;
        }
    }

    private void stopRecord() {
        isRecording = false;
    }

    /**
     * verify permission to create file
     * @return verify result
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
}

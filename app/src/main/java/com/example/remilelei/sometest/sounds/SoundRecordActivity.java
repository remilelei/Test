package com.example.remilelei.sometest.sounds;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.remilelei.sometest.R;

public class SoundRecordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_record);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_sound_file: {
                startActivity(new Intent(this, FileModeActivity.class));
                break;
            }
            case R.id.btn_sound_stream: {

                break;
            }
        }
    }
}

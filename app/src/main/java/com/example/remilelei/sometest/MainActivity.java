package com.example.remilelei.sometest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.remilelei.sometest.aidl.IRemoteService;
import com.example.remilelei.sometest.minatest.TestMinaActivity;
import com.example.remilelei.sometest.shake.ShakeActivity;
import com.example.remilelei.sometest.sounds.SoundRecordActivity;
import com.example.remilelei.sometest.widget.UpdateWidgetService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, UpdateWidgetService.class));

    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_to_shake: {
                // 跳转到摇一摇界面
                Intent intent = new Intent(this, ShakeActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.btn_to_sound_record: {
                // 跳转到声音录制界面
                Intent intent = new Intent(this, SoundRecordActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.btn_to_test_mina : {
                // 跳转到Mina测试页
                Intent intent = new Intent(this, TestMinaActivity.class);
                startActivity(intent);
                break;
            }
        }
    }
}

package com.example.remilelei.sometest.minatest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.remilelei.sometest.R;

import org.apache.mina.core.session.IoSession;

public class TestMinaActivity extends AppCompatActivity {

    private static final String TAG = "TestMinaActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_mina);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_mina_link_start: {
                Intent intent = new Intent(this, MInaService.class);
                startService(intent);
                break;
            }
            case R.id.btn_mina_send_msg: {
                IoSession session = ConnectionManager.getSession();
                if(session != null) {
                    Log.i(TAG, "send msg to server");
                    session.write("hello!");
                }
                break;
            }
            case R.id.btn_mina_link_close: {
                Intent intent = new Intent(this, MInaService.class);
                stopService(intent);
                break;
            }
        }
    }
}

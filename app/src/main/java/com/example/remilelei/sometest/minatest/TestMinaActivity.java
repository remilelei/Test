package com.example.remilelei.sometest.minatest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.remilelei.sometest.R;

public class TestMinaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_mina);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_mina_link_start: {
                break;
            }
            case R.id.btn_mina_send_msg: {
                break;
            }
            case R.id.btn_mina_link_close: {
                break;
            }
        }
    }
}

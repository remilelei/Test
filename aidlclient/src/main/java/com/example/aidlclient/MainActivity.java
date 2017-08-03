package com.example.aidlclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.remilelei.sometest.aidl.ITestAidl;

/**
 * AIDL学习 这个模块作为客户端，向服务端发送请求，做一个简单的求和
 * 客户端输入两个数字，服务端进行求和
 */
public class MainActivity extends AppCompatActivity {

    EditText etA;
    EditText etB;
    TextView tvSum;
    ITestAidl iTestAidl;

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            iTestAidl = ITestAidl.Stub.asInterface(iBinder);
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            iTestAidl = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindService();

        etA = (EditText) findViewById(R.id.et_a);
        etB = (EditText) findViewById(R.id.et_b);

        tvSum = (TextView) findViewById(R.id.tv_sum);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_get_sum: {
                if(iTestAidl == null) {
                    Toast.makeText(this, "服务未绑定", Toast.LENGTH_SHORT).show();
                    bindService();
                } else {
                    int a = Integer.valueOf(etA.getText().toString());
                    int b = Integer.valueOf(etB.getText().toString());
                    try {
                        int res = iTestAidl.getSum(a, b);
                        tvSum.setText(a + "+" + b + "=" + res);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
        }
    }

    /**
     * 绑定远程服务
     */
    private void bindService() {
        Log.i("MainActivity", "尝试绑定远程服务");
        // 这里这个packegName必须是Manifest.xml中的package属性
        String packegName = "com.example.remilelei.sometest";
        // action是Manifest.xml中成员service的action
        String action = "com.example.remilelei.sometest.aidl.ITestAidl";
        Intent intent = new Intent(action);
        intent.setPackage(packegName);

        bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

}

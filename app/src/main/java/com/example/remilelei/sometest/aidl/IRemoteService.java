package com.example.remilelei.sometest.aidl;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by remilelei on 2017/7/31.
 */

public class IRemoteService extends Service {
    final private String TAG = "IRemoteService";

    /**
     * 当客户端绑定到该服务的时候， 本函数执行
     * @param intent
     * @return
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    private IBinder iBinder = new ITestAidl.Stub() {

        @Override
        public int getSum(int a, int b) throws RemoteException {
            Log.i(TAG, "收到了远程请求 a=" + a + ",b=" + b);
            return a + b;
        }
    };
}

package com.example.remilelei.sometest.minatest;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.HandlerThread;
import android.os.IBinder;

public class MInaService extends Service {
    public MInaService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // RTODO 这里创建一个线程，使用这个线程建立连接
    }

    class ConnThread extends HandlerThread {
        private static final String sName = "ConnThread";
        private Context mContext;

        public ConnThread(Context context) {
            super(sName);
            mContext = context;

            ConnectionConfig config = new ConnectionConfig();
        }
    }
}

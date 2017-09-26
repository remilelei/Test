package com.example.remilelei.sometest.minatest;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

public class MInaService extends Service {
    public static final String TAG = "MInaService";
    private ConnThread thread;
    public MInaService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "service start...");
        if(thread != null) {
            thread.disConnect();
            thread.keepRun = false;
            thread = null;
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "service start...");
        thread = new ConnThread(getApplicationContext());
        thread.start();
    }

    class ConnThread extends HandlerThread {
        private static final String sName = "ConnThread";
        private Context mContext;
        private ConnectionManager connectionManager;
        public boolean isConnected = false;
        public volatile boolean keepRun;

        public ConnThread(Context context) {
            super(sName);
            mContext = context;
            keepRun = true;

            ConnectionConfig config = new ConnectionConfig();

            config.ip = "192.168.23.1";
            config.port = 9527;
            config.mContext = mContext;
            config.bufferSize = 1024 * 10;

            connectionManager = new ConnectionManager(config);
        }

        @Override
        protected void onLooperPrepared() {
            while(keepRun) {
                isConnected = connectionManager.connect();
                if(isConnected) {
                    Log.i(TAG, "connect success");
                    break;
                }
                Log.e(TAG, "connect failed, try again in 3s");
                try {
                    Thread.sleep(3 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void disConnect() {
            isConnected = false;
            connectionManager.disconnect();
        }
    }
}

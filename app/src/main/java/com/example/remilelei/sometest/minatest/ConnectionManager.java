package com.example.remilelei.sometest.minatest;

import android.content.Context;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;

/**
 * Created by remilelei on 2017/9/15.
 *
 * 封装建立连接与断开连接方法
 */

public class ConnectionManager {

    private ConnectionConfig mConfig;
    private NioSocketConnector mConnector;
    private IoSession mSession;
    private InetSocketAddress mAddress;

    public ConnectionManager(ConnectionConfig config) {
        this.mConfig = config;

        init();
    }

    private void init() {
        mAddress = new InetSocketAddress(mConfig.ip, mConfig.port);
        mConnector = new NioSocketConnector();
        mConnector.getSessionConfig().setReadBufferSize(mConfig.bufferSize);
        mConnector.getFilterChain().addLast("Logger", new LoggingFilter());
        mConnector.getFilterChain().addLast("codec",
                new ProtocolCodecFilter(new ObjectSerializationCodecFactory()));
        mConnector.setHandler(new DemoHandler(mConfig.mContext));
    }

    public boolean connect() {
        try {
            ConnectFuture future = mConnector.connect();
            future.awaitUninterruptibly();
            mSession = future.getSession();
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
        return mSession != null;
    }

    public void disconnect() {
        mConnector.dispose();
        mConfig = null;
        mConnector = null;
        mSession = null;
        mAddress = null;
    }

    private class DemoHandler extends IoHandlerAdapter {
        private Context mContext;

        public DemoHandler(Context mContext) {
            this.mContext = mContext;
        }
    }
}

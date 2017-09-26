package com.example;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MyClass {
    public static void main(String[] args) {
        MyClass myClass = new MyClass();
        myClass.testServer();
    }

    public void testServer() {
        IoAcceptor acceptor = new NioSocketAcceptor();

        // 过滤器
        acceptor.getFilterChain().addLast("logger", new LoggingFilter());
        acceptor.getFilterChain().addLast("codec",
                new ProtocolCodecFilter(new ObjectSerializationCodecFactory()));

        // 加入handler
        acceptor.setHandler(new DemoServerHandler());

        // 配置acceptor
        IoSessionConfig config = acceptor.getSessionConfig();
        config.setReadBufferSize(2048);
        config.setIdleTime(IdleStatus.BOTH_IDLE, 10);

        // 绑定接口
        try {
            acceptor.bind(new InetSocketAddress(9527));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private class DemoServerHandler extends IoHandlerAdapter {
        @Override
        public void sessionCreated(IoSession session) throws Exception {
            super.sessionCreated(session);
            System.out.println("sessionCreated");
        }

        @Override
        public void sessionOpened(IoSession session) throws Exception {
            super.sessionOpened(session);
            System.out.println("sessionOpened");
        }

        @Override
        public void sessionClosed(IoSession session) throws Exception {
            super.sessionClosed(session);
            System.out.println("sessionClosed");
        }

        @Override
        public void messageReceived(IoSession session, Object message) throws Exception {
            super.messageReceived(session, message);
            System.out.println("messageReceived msg=" + message);
            messageSent(session, "hi~");
        }

        @Override
        public void messageSent(IoSession session, Object message) throws Exception {
            super.messageSent(session, message);
            System.out.println("messageSent");
        }
    }

}

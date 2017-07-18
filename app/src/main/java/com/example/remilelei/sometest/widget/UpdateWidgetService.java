package com.example.remilelei.sometest.widget;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

import com.example.remilelei.sometest.R;
import com.example.remilelei.sometest.widget.MyFirstWidget;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by remilelei on 2017/7/12.
 */

public class UpdateWidgetService extends Service {

    private Timer timer;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // 1.实例化timer
        timer = new Timer();
        // 2.规划timer的任务，更新自定义Widget
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateWidget();
            }
        }, 0, 1000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    /**
     * 更新自定义控件
     */
    private void updateWidget() {
        // 1.获取格式化好的时间文本
        String time = sdf.format(new Date());
        // 2.把这个文本传给我们的控件
        RemoteViews rv = new RemoteViews(getPackageName(), R.layout.my_widget);
        rv.setTextViewText(R.id.tv_widget, time);
        // 3.更新自定义控件
        AppWidgetManager awm = AppWidgetManager.getInstance(getApplicationContext());
        ComponentName cn = new ComponentName(getApplicationContext(), MyFirstWidget.class);
        awm.updateAppWidget(cn, rv);
    }
}

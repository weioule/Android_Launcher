package com.e.android_launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.e.android_launcher.utils.SettingUtils;

/**
 * Created by weioule
 * on 2020/11/21
 */
public class TimerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String formType = intent.getStringExtra(PriorityTask.FORM_TYPE);
        if ("TimerService".equals(formType)) {
            //定时器
            Intent i = new Intent(context, TimerService.class);
            context.startService(i);
        } else if ("PriorityTask".equals(formType)) {
            //延时任务
            int type = intent.getIntExtra(PriorityTask.TYPE, 0);
            String info = intent.getStringExtra(PriorityTask.INFO);

            SettingUtils.update(type, info);
        }
    }
}
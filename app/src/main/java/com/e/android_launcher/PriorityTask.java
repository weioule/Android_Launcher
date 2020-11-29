package com.e.android_launcher;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import com.e.android_launcher.utils.SettingUtils;

import java.util.Hashtable;

/**
 * Created by weioule
 * on 2020/11/21
 */
//优先级任务
public class PriorityTask implements Runnable, Comparable {

    public final static String TYPE = "type";
    public final static String INFO = "info";
    public final static String APP_NAME = "app_name";
    public final static String FORM_TYPE = "form_type";
    private int type;
    private int priority;
    private int isNowUpdate;
    private long updateTime;
    private TaskRuningCallBackListener listener;
    private String info;//下载链接、目录path、包名等共用字段
    public static Hashtable<Integer, String> runingTaskMap = new Hashtable<>();

    public PriorityTask(TaskRuningCallBackListener listener, int priority, int type, String info, int isNowUpdate, long updateTime) {
        this.type = type;
        this.info = info;
        this.listener = listener;
        this.priority = priority;
        this.updateTime = updateTime;
        this.isNowUpdate = isNowUpdate;
    }

    public int getType() {
        return type;
    }

    public String getInfo() {
        return info;
    }

    @Override
    public void run() {
        runingTaskMap.put(type, info);
        switch (type) {
            case 1:
                //升级
            case 2:
                //降级
            case 3:
                //新包安装
            case 4:
                //卸载
                doUpdate();
                break;
            case 11:
                //获取版本更新信息
                listener.getUpdateInfoList(type);
                break;
            case 12:
                //上传文件
                listener.uploadDirectoryFiles(type, info);
                break;
            case 13:
                //上传文件目录
                listener.uploadDirectory(type, info);
                break;
            case 14:
                //重启
                SettingUtils.cq(App.getInstance(), 0, type);
                break;
            case 15:
                //关机
                SettingUtils.gj(App.getInstance(), false, type);
                break;
        }
    }

    private void doUpdate() {
        if (isNowUpdate == 1) {
            //立即执行
            SettingUtils.update(type, info);
        } else if (isNowUpdate == 2) {
            //延时任务
            Intent i = new Intent(App.getInstance(), TimerReceiver.class);
            i.putExtra(TYPE, type);
            i.putExtra(FORM_TYPE, "PriorityTask");
            i.putExtra(INFO, info);
            i.putExtra(APP_NAME, SettingUtils.getAppName(info));

            PendingIntent pi = PendingIntent.getBroadcast(App.getInstance().getApplicationContext(), type, i, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager manager = (AlarmManager) App.getInstance().getSystemService(App.getInstance().ALARM_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, updateTime, pi);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                manager.setExact(AlarmManager.RTC_WAKEUP, updateTime, pi);
            } else {
                manager.set(AlarmManager.RTC_WAKEUP, updateTime, pi);
            }
        }
    }

    @Override
    public int compareTo(Object arg) {
        PriorityTask task = (PriorityTask) arg;
        if (this.priority == task.priority) {
            return 0;
        }
        return this.priority > task.priority ? 1 : -1;
    }

    public interface TaskRuningCallBackListener {
        void getUpdateInfoList(int type);

        void uploadDirectoryFiles(int type, String directoryName);

        void uploadDirectory(int type, String uploadDirectory);

    }
}
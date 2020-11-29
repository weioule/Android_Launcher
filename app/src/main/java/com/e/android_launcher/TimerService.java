package com.e.android_launcher;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.e.android_launcher.utils.SettingUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * Created by weioule
 * on 2020/11/21
 */
public class TimerService extends Service implements PriorityTask.TaskRuningCallBackListener {

    private static TimerService timerService;

    public static TimerService getInstance() {
        return timerService;
    }

    public final static String LOGO_STR = " -@- ";
    //使用单线程池实现任务队列，作为桌面避免过多占用cpu
    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 1, TimeUnit.MILLISECONDS, new PriorityBlockingQueue<Runnable>());

    //默认时间是5分钟
    private long defaultDuration = 5 * 60;
    private long updateDuration;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        timerService = this;
        timingTask(TimerService.this);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void timingTask(final Context context) {
        // todo 定时器请求任务接口：

        //将以下任务添加任务队列 addTask()
        //有更新
        //目录查询
        //文件上传
        //重启
        //关机

        //请求结束后启动定时器 (这样可以由本接口修改定时器间隔时间)
//        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
//        if (updateDuration == 0) updateDuration = defaultDuration;
//        long triggerAtTime = SystemClock.elapsedRealtime() + updateDuration * 1000;
//        Intent i = new Intent(context, TimerReceiver.class);
//        i.putExtra(PriorityTask.FORM_TYPE, "TimerService");
//        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            manager.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            manager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
//        } else {
//            manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
//        }
    }

    @Override
    public void uploadDirectory(final int type, final String uploadDirectory) {
        // todo 上传文件（日志 或者 其他文件）
    }

    @Override
    public void uploadDirectoryFiles(final int type, final String directoryName) {
        // todo getFilesAllName(directoryName)) 提交要查询的目录名列表
    }

    @Override
    public void getUpdateInfoList(final int type) {
        // todo 请求版本更新接口 获取具体更新信息（升级、降级、新包安装、卸载 等任务）
//        addTask(appInfo.getIsUpdate(), appInfo.getPackageName() + LOGO_STR + appInfo.getUpdateUrl(), appInfo.getIsNowUpdate(), appInfo.getUpdateTime());
    }

    //添加任务队列
    private void addTask(int type, String str, int isNowUpdate, long updateTime) {
        Iterator<Map.Entry<Integer, String>> it = PriorityTask.runingTaskMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, String> next = it.next();
            if (next.getKey() == type) {
                //若正在执行，则不再添加队列
                return;
            }
        }

        PriorityBlockingQueue<Runnable> queue = (PriorityBlockingQueue<Runnable>) executor.getQueue();
        Iterator<Runnable> iterator = queue.iterator();

        while (iterator.hasNext()) {
            PriorityTask next = (PriorityTask) iterator.next();
            if (type == next.getType() && str.equals(next.getInfo())) {
                //若队列里存在，则删除使用最新
                iterator.remove();
            }
        }

        String taskName = "";
        String infoName = "";

        switch (type) {
            case 1:
                taskName = "升级";
                infoName = " url = ";
                break;
            case 2:
                taskName = "降级";
                infoName = " url = ";
                break;
            case 3:
                taskName = "安装新包";
                infoName = " url = ";
                break;
            case 4:
                taskName = "卸载";
                infoName = " 包名 = ";
                break;
            case 11:
                taskName = "有版本更新";
                break;
            case 12:
                taskName = "上传文件目录";
                infoName = " 路径 = ";
                break;
            case 13:
                taskName = "上传文件";
                infoName = " 路径 = ";
                break;
        }

        SettingUtils.writeHandleTaskProcessLog("将 " + taskName + " 任务添加到队列" + infoName + str);

        executor.execute(new PriorityTask(TimerService.this, queue.size(), type, str, isNowUpdate, updateTime));
    }

}

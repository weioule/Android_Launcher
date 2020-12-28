package com.e.android_launcher.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.text.format.DateFormat;

import androidx.annotation.RequiresApi;

import com.e.android_launcher.App;
import com.e.android_launcher.MainActivity;
import com.e.android_launcher.PriorityTask;
import com.e.android_launcher.TimerReceiver;
import com.e.android_launcher.TimerService;
import com.e.crashhandler.CrashHandlerJar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by weioule
 * on 2020/11/21
 */
public class SettingUtils {

    public static void installApp(String apkPath, String packageName, String downloadUrl) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            MainActivity.getmSessionCallback().installApp(apkPath, packageName, downloadUrl);
        } else {
            boolean success = SettingUtils.execCommand("pm", "install", "-r", apkPath);
            if (success)
                MainActivity.getInstance().refreshAdapterList(1, packageName);
            SettingUtils.writeHandleTaskProcessLog((success ? "安装成功 " : "安装失败 ") + " url = " + downloadUrl);
        }
    }

    /**
     * 根据包名卸载应用
     *
     * @param type
     * @param packageName
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void uninstall(int type, String packageName) {
        Intent broadcastIntent = new Intent(App.getInstance(), TimerReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(App.getInstance(), 30, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PackageInstaller packageInstaller = App.getInstance().getPackageManager().getPackageInstaller();
        packageInstaller.uninstall(packageName, pendingIntent.getIntentSender());

        MainActivity.getInstance().refreshAdapterList(2 == type ? type : 3, packageName);
    }

    /**
     * 重启系统
     */
    public static void cq(Context context, int t, int type) {
        Intent i = new Intent("android.intent.action.REBOOT");
        // 立即重启：1
        i.putExtra("nowait", 1);
        // 重启次数：1
        i.putExtra("interval", 1);
        // 不出现弹窗：0
        i.putExtra("window", t);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);

        removeTask(type);
    }

    public static void gj(Context context, boolean t, int type) {
        Intent off_intent = new Intent();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            off_intent.setAction("com.android.internal.intent.action.REQUEST_SHUTDOWN");
        }else {
            off_intent.setAction("android.intent.action.ACTION_REQUEST_SHUTDOWN");
        }
        off_intent.putExtra("android.intent.extra.KEY_CONFIRM", t);

        off_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(off_intent);
        
        removeTask(type);
    }

    /*
     * pm命令可以通过adb在shell中执行，同样，我们可以通过代码来执行
     */
    public static boolean execCommand(String... command) {
        Process process = null;
        InputStream errIs = null;
        InputStream inIs = null;
        boolean success;
        try {
            process = new ProcessBuilder().command(command).start();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int read = -1;
            errIs = process.getErrorStream();
            while ((read = errIs.read()) != -1) {
                baos.write(read);
            }

            inIs = process.getInputStream();
            while ((read = inIs.read()) != -1) {
                baos.write(read);
            }

            inIs.close();
            errIs.close();
            process.destroy();
            success = true;
        } catch (IOException e) {
            success = false;
        }
        return success;
    }

    public static void downloadApk(final String packageName, final String downloadUrl, final String fileName) {
        if (TextUtils.isEmpty(downloadUrl)) {
            writeHandleTaskProcessLog("下载安装包 url" + downloadUrl);
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(downloadUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(20000);
                    conn.setReadTimeout(20000);
                    InputStream is = conn.getInputStream();
                    FileOutputStream fileOutputStream = null;
                    File file = null;
                    String mApkFile;
                    if (is != null) {
                        mApkFile = getRootPath().toString() + File.separator + App.DIRECTORY + File.separator + fileName;

                        file = new File(mApkFile);
                        if (!file.getParentFile().exists() || !file.getParentFile().isDirectory()) {
                            file.mkdirs();
                        }

                        fileOutputStream = new FileOutputStream(file);
                        byte[] buf = new byte[4096];
                        int temp = -1;
                        while ((temp = is.read(buf)) != -1) {
                            fileOutputStream.write(buf, 0, temp);
                        }
                    }
                    fileOutputStream.flush();
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    if (null != file && file.exists())
                        installApp(file.getAbsolutePath(), packageName, downloadUrl);

                } catch (MalformedURLException e) {
                    writeHandleTaskCrashLog("下载安装包 url = " + downloadUrl + " 异常：" + e.getMessage());
                    e.printStackTrace();
                } catch (IOException e) {
                    writeHandleTaskCrashLog("下载安装包 url = " + downloadUrl + " 异常：" + e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //处理任务崩溃日志
    public static void writeHandleTaskCrashLog(final String logStr) {
        CrashHandlerJar.writeLog(logStr, "Handle_Task_Crash.txt");
    }

    //处理任务流程日志
    public static void writeHandleTaskProcessLog(final String logStr) {
        CrashHandlerJar.writeLog(logStr, "Handle_Task_Process.txt");
    }

    public static void update(int type, String info) {
        if (2 == type || 4 == type) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                uninstall(type, info.split(TimerService.LOGO_STR)[0]);
            } else {
                boolean b = execCommand("pm", "uninstall", info.split(TimerService.LOGO_STR)[0]);//卸载apk，packageName为包名，如com.example.android.apis
                if (b)
                    MainActivity.getInstance().refreshAdapterList(2 == type ? type : 3, info.split(TimerService.LOGO_STR)[0]);
            }
        }
        if (1 == type || 2 == type || 3 == type)
            downloadApk(info.split(TimerService.LOGO_STR)[0], info.split(TimerService.LOGO_STR)[1], getAppName(info));
        removeTask(type);
    }

    public static String getAppName(String info) {
        String appName = null;
        if (info.contains(TimerService.LOGO_STR)) {
            String[] split = info.split(TimerService.LOGO_STR);
            if (null != split && split.length > 0) {
                String packageName = split[0];
                if (!TextUtils.isEmpty(packageName)) {
                    String[] split2 = packageName.split("\\.");
                    if (null != split2 && split2.length > 0) {
                        appName = split2[split2.length - 1] + ".apk";
                    }
                }
            }
        }

        if (TextUtils.isEmpty(appName)) {
            Date date0 = new Date();
            CharSequence str = DateFormat.format("yyyy-MM-dd", date0.getTime());
            appName = str + ".apk";
        }
        return appName;
    }

    //移除执行完的任务
    public static synchronized void removeTask(int key) {
        Iterator<Map.Entry<Integer, String>> iterator = PriorityTask.runingTaskMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, String> next = iterator.next();
            if (next.getKey() == key) {
                iterator.remove();
                break;
            }
        }
    }

    /**
     * 得到SD卡根目录.
     */
    public static File getRootPath() {
        File path = null;
        if (sdCardIsAvailable()) {
            path = Environment.getExternalStorageDirectory(); // 取得sdcard文件路径
        } else {
            path = Environment.getDataDirectory();
        }
        return path;
    }

    /**
     * SD卡是否可用.
     */
    public static boolean sdCardIsAvailable() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File sd = new File(Environment.getExternalStorageDirectory().getPath());
            return sd.canWrite();
        } else {
            return false;
        }
    }
}

package com.e.android_launcher.utils;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.e.android_launcher.App;
import com.e.android_launcher.MainActivity;
import com.e.android_launcher.TimerReceiver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by weioule
 * on 2020/11/21
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class InstallSessionCallback extends PackageInstaller.SessionCallback {
    private String downloadUrl;
    private HashMap<Integer, String> sessionIdMap = new HashMap<>();

    @Override
    public void onCreated(int sessionId) {
    }

    @Override
    public void onBadgingChanged(int sessionId) {
    }

    @Override
    public void onActiveChanged(int sessionId, boolean active) {
    }

    @Override
    public void onProgressChanged(int sessionId, float progress) {
    }

    @Override
    public void onFinished(int sessionId, boolean success) {
        for (Iterator<Map.Entry<Integer, String>> iterator = sessionIdMap.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<Integer, String> item = iterator.next();

            if (item.getKey() == sessionId) {
                if (success) {
                    MainActivity.getInstance().refreshAdapterList(1, item.getValue());
                    SettingUtils.writeHandleTaskProcessLog("安装成功 url = " + downloadUrl);
                } else {
                    SettingUtils.writeHandleTaskProcessLog("安装失败 url = " + downloadUrl);
                }
                iterator.remove();
            }
        }
    }

    /**
     * 适配android9的安装方法。
     * 全部替换安装
     */
    public void installApp(String apkFilePath, String packageName, String downloadUrl) {
        this.downloadUrl = downloadUrl;
        File apkFile = new File(apkFilePath);
        if (!apkFile.exists()) {
            SettingUtils.writeHandleTaskProcessLog("安装app 文件不存在 url = " + downloadUrl);
        }

        PackageInstaller packageInstaller = App.getInstance().getPackageManager().getPackageInstaller();
        PackageInstaller.SessionParams sessionParams = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        sessionParams.setSize(apkFile.length());
        int sessionId = -1;
        try {
            sessionId = packageInstaller.createSession(sessionParams);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (sessionId != -1) {
            sessionIdMap.put(sessionId, packageName);
            boolean copySuccess = onTransfesApkFile(apkFilePath, sessionId);
            if (copySuccess) {
                execInstallAPP(sessionId);
            }
        }
    }

    /**
     * 通过文件流传输apk
     *
     * @param apkFilePath
     * @param sessionId
     * @return
     */
    private boolean onTransfesApkFile(String apkFilePath, int sessionId) {
        InputStream in = null;
        OutputStream out = null;
        PackageInstaller.Session session = null;
        boolean success = false;
        try {
            File apkFile = new File(apkFilePath);
            session = App.getInstance().getPackageManager().getPackageInstaller().openSession(sessionId);
            out = session.openWrite("base.apk", 0, apkFile.length());
            in = new FileInputStream(apkFile);
            int total = 0, c;
            byte[] buffer = new byte[1024 * 1024];
            while ((c = in.read(buffer)) != -1) {
                total += c;
                out.write(buffer, 0, c);
            }
            session.fsync(out);
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != session) {
                session.close();
            }
            try {
                if (null != out) {
                    out.close();
                }
                if (null != in) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return success;
    }

    /**
     * 执行安装并通知安装结果
     *
     * @param sessionId
     * @return
     */
    private boolean execInstallAPP(int sessionId) {
        PackageInstaller.Session session = null;
        boolean success = false;
        try {
            session = App.getInstance().getPackageManager().getPackageInstaller().openSession(sessionId);
            Intent intent = new Intent(App.getInstance(), TimerReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(App.getInstance(), 31, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            session.commit(pendingIntent.getIntentSender());
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != session) {
                session.close();
            }
        }

        return success;
    }
}
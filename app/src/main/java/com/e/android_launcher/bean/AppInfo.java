package com.e.android_launcher.bean;

import android.content.Intent;
import android.graphics.drawable.Drawable;

import java.io.Serializable;

/**
 * Created by weioule
 * on 2020/11/21
 */
public class AppInfo implements Serializable {

    private String packageName; //包名
    private transient Drawable ico;       //图标
    private String appName;        //应用标签
    private Intent intent;     //启动应用程序的Intent ，一般是Action为Main和Category为Lancher的Activity
    private boolean visibility;
    private String versionName;
    private String versionCode;

    private int isUpdate; //类型: 1=升级  2=降级  3=新包安装  4=卸载
    private int isNowUpdate; //是否立刻更新 1立即更新  2按更新时间更新
    private String updateUrl; //更新包url
    private long updateTime;//任务执行时间
    private String updateType;//更新类型  根据不同的设备更新，预留字段


    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }


    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Drawable getIco() {
        return ico;
    }

    public void setIco(Drawable ico) {
        this.ico = ico;
    }

    public boolean isVisibility() {
        return visibility;
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public int getIsUpdate() {
        return isUpdate;
    }

    public void setIsUpdate(int isUpdate) {
        this.isUpdate = isUpdate;
    }

    public int getIsNowUpdate() {
        return isNowUpdate;
    }

    public void setIsNowUpdate(int isNowUpdate) {
        this.isNowUpdate = isNowUpdate;
    }

    public String getUpdateUrl() {
        return updateUrl;
    }

    public void setUpdateUrl(String updateUrl) {
        this.updateUrl = updateUrl;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public String getUpdateType() {
        return updateType;
    }

    public void setUpdateType(String updateType) {
        this.updateType = updateType;
    }
}
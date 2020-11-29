package com.e.android_launcher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.e.android_launcher.adapter.EditAdapter;
import com.e.android_launcher.bean.AppInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by weioule
 * on 2020/11/21
 */
public class EditActivity extends AppCompatActivity implements View.OnClickListener {
    private RecyclerView recycleryView;
    private List<AppInfo> list;
    private List<String> packageNameList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        recycleryView = findViewById(R.id.recyclerView);
        findViewById(R.id.iv_back).setOnClickListener(this);
        findViewById(R.id.complete).setOnClickListener(this);

        packageNameList = (List<String>) getIntent().getSerializableExtra(MainActivity.VISIBILITY_PACKAGE_NAME_LIST);
        List<AppInfo> appInfos = getAppList(this, packageNameList);
        EditAdapter adapter = new EditAdapter(appInfos);
        recycleryView.setLayoutManager(new GridLayoutManager(this, 4));
        recycleryView.setAdapter(adapter);
        RecyclerViewDivider divider = new RecyclerViewDivider.Builder(this)
                .setStyle(RecyclerViewDivider.Style.BOTH)
                .setColor(getResources().getColor(R.color.transparent))
                .setOrientation(RecyclerViewDivider.GRIDE_VIW)
                .setSize(5)
                .build();
        recycleryView.addItemDecoration(divider);
    }

    public List<AppInfo> getAppList(Context context, List<String> packageNameList) {
        list = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> activities = pm.queryIntentActivities(mainIntent, 0);
        for (ResolveInfo info : activities) {
            String packName = info.activityInfo.packageName;
            if (packName.equals(context.getPackageName()) || "com.android.settings.Settings".equals(info.activityInfo.name)) {
                continue;
            }
            AppInfo mInfo = new AppInfo();
            mInfo.setIco(info.activityInfo.applicationInfo.loadIcon(pm));
            mInfo.setAppName(info.activityInfo.applicationInfo.loadLabel(pm).toString());
            mInfo.setPackageName(packName);
            try {
                PackageInfo packageInfo = pm.getPackageInfo(packName, 0);
                mInfo.setVersionName(packageInfo.versionName);
                mInfo.setVersionCode(packageInfo.versionCode + "");
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            // 为应用程序的启动Activity 准备Intent
            Intent launchIntent = new Intent();
            launchIntent.setComponent(new ComponentName(packName, info.activityInfo.name));
            mInfo.setIntent(launchIntent);

            //回显可见的App
            if (null != packageNameList)
                for (String packageName : packageNameList) {
                    if (packageName.equals(packName)) {
                        mInfo.setVisibility(true);
                        break;
                    }
                }

            //4.4会有一些重复的
            boolean hased = false;
            for (AppInfo appInfo : list) {
                if (appInfo.getPackageName().equals(packName)) {
                    hased = true;
                    break;
                }
            }
            if (!hased)
                list.add(mInfo);
        }

        return list;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.complete:
                List<AppInfo> whiteList = new ArrayList<>();
                for (AppInfo appInfo : this.list) {
                    if (appInfo.isVisibility()) {
                        whiteList.add(appInfo);
                    }
                }

                //检测是否有变动
                int size = 0;
                if (whiteList.size() == packageNameList.size() - 3) {
                    for (AppInfo appInfo : whiteList) {
                        for (String packageName : packageNameList) {
                            if (packageName.equals(appInfo.getPackageName())) {
                                size++;
                                break;
                            }
                        }
                    }

                    if (size != whiteList.size())
                        MainActivity.getInstance().updateWhitelist(whiteList);
                } else {
                    MainActivity.getInstance().updateWhitelist(whiteList);
                }

                finish();
                break;
        }
    }
}

package com.e.android_launcher;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.e.android_launcher.adapter.MainAdapter;
import com.e.android_launcher.adapter.MenuAdapter;
import com.e.android_launcher.bean.AppInfo;
import com.e.android_launcher.utils.InstallSessionCallback;
import com.e.android_launcher.utils.PackageUtils;
import com.e.android_launcher.utils.SharedPreferencesUtil;
import com.e.android_launcher.utils.ToastUtil;
import com.e.android_launcher.utils.Utils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by weioule
 * on 2020/11/21
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String DATA = "data";
    private final static String HAS_CHANGE = "hasChange";
    public final static String VISIBILITY_PACKAGE_NAME_LIST = "visibility_package_name_list";
    private View topView;
    private View rightView;
    private Dialog hintDialog;
    private ImageView rightImg;
    private MainAdapter adapter;
    private ImageView background;
    private boolean checkAppList;
    private MenuAdapter menuAdapter;
    private PopupWindow popupWindow;
    private boolean toSetMainLauncher;
    private RecyclerView recycleryView;
    private WindowManager windowManager;
    private static MainActivity mainActivity;
    private WindowManager.LayoutParams mLayoutParam;
    private ArrayList<String> menuList = new ArrayList<>();
    private static InstallSessionCallback mSessionCallback;
    private Hashtable<Integer, String> indexMap = new Hashtable<>();

    public static MainActivity getInstance() {
        return mainActivity;
    }

    public static InstallSessionCallback getmSessionCallback() {
        return mSessionCallback;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        background = findViewById(R.id.iv_background);
        recycleryView = findViewById(R.id.recyclerView);
        rightImg = findViewById(R.id.right_img);
        rightImg.setOnClickListener(this);

        addTopView();
        setBackground();
        menuList.add("管理者模式");

        mainActivity = this;

        String str = SharedPreferencesUtil.getString(MainActivity.this, VISIBILITY_PACKAGE_NAME_LIST, "");
        if (!TextUtils.isEmpty(str) && !"[]".equals(str)) {
            try {
                List<AppInfo> whiteList = new Gson().fromJson(str, new TypeToken<List<AppInfo>>() {
                }.getType());
                initAppList(whiteList);
            } catch (Exception e) {
                initAppList(null);
            }
        } else initAppList(null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSessionCallback = new InstallSessionCallback();
            //注册处理包管理回调
            getPackageManager().getPackageInstaller().registerSessionCallback(mSessionCallback);
        }

        checkAppList = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //返回系统屏幕时防止影响系统屏幕的使用，topView被隐藏了，再次从系统屏幕返回时将其显示
        if (null != windowManager && null != topView)
            topView.setVisibility(View.VISIBLE);
        //回到桌面，隐藏用于屏蔽事件的view
        if (null != windowManager && null != rightView)
            rightView.setVisibility(View.GONE);

        //检查默认桌面
        if (!isMainLauncher()) {
            toSetMainLauncher = true;
            showHintDialog("请设置 " + PackageUtils.getAppName(this) + " 为始终主屏幕", null, 1);
        } else if (toSetMainLauncher) {
            showHintDialog("请长按home键清除最近打开的所有任务，进入桌面锁定模式", null, 0);
            toSetMainLauncher = false;
        }

        //检查白名单
        if (checkAppList && null != adapter && adapter.getData().size() > 0) {
            HashMap<String, Object> map = getAppList(this, adapter.getData());
            final List<AppInfo> appList = (List<AppInfo>) map.get(DATA);
            final boolean hasChange = (boolean) map.get(HAS_CHANGE);
            if (hasChange) {
                //更新白名单
                adapter.replaceData(appList);
                SharedPreferencesUtil.putString(this, VISIBILITY_PACKAGE_NAME_LIST, new Gson().toJson(appList));
                pushWhiteList(appList);
            }
        }

        if (checkAppList) checkAppList = false;
    }

    //是否是默认桌面
    public boolean isMainLauncher() {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        final ResolveInfo res = getPackageManager().resolveActivity(intent, 0);
        if (res.activityInfo == null) {
            return true;
        } else if (!res.activityInfo.packageName.equals(getPackageName())) {
            return false;
        }
        return true;
    }

    private void setBackground() {
        //使用原始背景
        WallpaperManager manager;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            manager = WallpaperManager.getInstance(MainActivity.this);
            Drawable drawable = manager.getDrawable();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                background.setImageDrawable(drawable);
            }
        }
    }

    private void initAppList(List<AppInfo> list) {
        HashMap<String, Object> map = getAppList(this, list);
        final List<AppInfo> appList = (List<AppInfo>) map.get(DATA);
        final boolean hasChange = (boolean) map.get(HAS_CHANGE);
        if (null == adapter) {
            adapter = new MainAdapter(appList);
            recycleryView.setLayoutManager(new GridLayoutManager(this, 4));
            recycleryView.setAdapter(adapter);
            RecyclerViewDivider divider = new RecyclerViewDivider.Builder(this)
                    .setStyle(RecyclerViewDivider.Style.BOTH)
                    .setColor(getResources().getColor(R.color.transparent))
                    .setOrientation(RecyclerViewDivider.GRIDE_VIW)
                    .setSize(5)
                    .build();
            recycleryView.addItemDecoration(divider);
            adapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                    AppInfo appInfo = (AppInfo) adapter.getData().get(position);
                    switch (appInfo.getAppName()) {
                        case "WIFI设置":
                            addRightView();
                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                            break;
                        case "位置信息":
                            addRightView();
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            break;
                        case "蓝牙设置":
                            addRightView();
                            startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
                            break;
                        default:
                            Intent intent = null;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
                                intent = getPackageManager().getLaunchIntentForPackage(appList.get(position).getPackageName());
                            }
                            if (intent != null) {
                                intent.putExtra("type", "110");
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            } else {
                                ToastUtil.shortMsg("卸载中，请稍后...");
                            }
                            break;
                    }

                    overridePendingTransition(0, 0);
                }
            });

            //todo 启动服务
//            Intent intent = new Intent(this, TimerService.class);
//            startService(intent);
        } else {
            adapter.replaceData(appList);
        }

        //更新本地白名单
        if (hasChange) {
            SharedPreferencesUtil.putString(this, VISIBILITY_PACKAGE_NAME_LIST, new Gson().toJson(appList));
            pushWhiteList(appList);
        }
    }

    //检测本地白名单是否都在（存在被卸载的可能）
    public HashMap<String, Object> getAppList(Context context, List<AppInfo> whiteList) {
        HashMap<String, Object> map = new HashMap<>();
        boolean hasChange = false, hasDefaultApp = false;
        if (whiteList != null && whiteList.size() > 0) {
            PackageManager pm = context.getPackageManager();
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> activities = pm.queryIntentActivities(mainIntent, 0);

            Iterator<AppInfo> iterator = whiteList.iterator();
            while (iterator.hasNext()) {
                AppInfo next = iterator.next();
                boolean hasApp = false;
                for (ResolveInfo info : activities) {
                    String packName = info.activityInfo.packageName;
                    if ("WIFI设置".equals(next.getAppName())) {
                        if (null == next.getIco())
                            next.setIco(getResources().getDrawable(R.drawable.ic_wifi));
                        hasApp = true;
                        break;
                    }
                    if ("位置信息".equals(next.getAppName())) {
                        if (null == next.getIco())
                            next.setIco(getResources().getDrawable(R.drawable.ic_location));
                        hasApp = true;
                        break;
                    }
                    if ("蓝牙设置".equals(next.getAppName())) {
                        if (null == next.getIco())
                            next.setIco(getResources().getDrawable(R.drawable.ic_bluetooth));
                        hasApp = true;
                        break;
                    }
                    if (packName.equals(next.getPackageName())) {
                        if (null == next.getIco())
                            next.setIco(info.activityInfo.applicationInfo.loadIcon(pm));
                        hasApp = true;
                        break;
                    }
                }

                if (!hasApp) {
                    iterator.remove();
                    hasChange = true;
                }

                if (next.getAppName().equals("WIFI设置")) {
                    hasDefaultApp = true;
                }
            }
        }

        if (!hasDefaultApp) {
            if (null == whiteList) whiteList = new ArrayList<>();
            addNomalApp(whiteList);
        }

        map.put(DATA, whiteList);
        map.put(HAS_CHANGE, hasChange);
        return map;
    }

    //添加默认系统应用
    private AppInfo getAppInfo(Drawable drawable, String label) {
        AppInfo mInfo = new AppInfo();
        mInfo.setIco(drawable);
        mInfo.setAppName(label);
        //这里是为了后面使用包名做判断时，避免包空异常加的假包名
        mInfo.setPackageName("system.settings");
        return mInfo;
    }

    @SuppressLint("WrongConstant")
    public void addTopView() {
        //屏蔽下拉通知栏设置
        windowManager = ((WindowManager) getApplicationContext().getSystemService("window"));

        int mScreenWidth = windowManager.getDefaultDisplay().getWidth();
        mLayoutParam = new WindowManager.LayoutParams();
        mLayoutParam.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;

        mLayoutParam.format = PixelFormat.RGBA_8888;
        mLayoutParam.windowAnimations = 16973828;
        mLayoutParam.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        mLayoutParam.gravity = Gravity.TOP;
        mLayoutParam.width = mScreenWidth;
        mLayoutParam.height = Utils.getStatusBarHeight(this);

        topView = new View(this);
        windowManager.addView(topView, mLayoutParam);
    }

    public void addRightView() {
        //屏蔽部分手机系统设置页面右上角打开其他设置按钮，否则将做不到完全控制
        if (Build.BRAND.equals("xxxx") && Build.MODEL.equals("xxxx")) {
            mLayoutParam.height = Utils.getStatusBarHeight(this) * 3;

            if (null == rightView) {
                rightView = new View(this);
                rightView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ToastUtil.shortMsg("launcher已屏蔽快捷设置");
                    }
                });
                windowManager.addView(rightView, mLayoutParam);
            } else
                rightView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.right_img:
                showTipPopupWindow();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if (null != windowManager) {
            if (null != topView) windowManager.removeView(topView);
            if (null != rightView) windowManager.removeView(rightView);
        }
        if (hintDialog != null) {
            hintDialog.dismiss();
        }
//       todo 关闭服务
        stopService(new Intent(this, TimerService.class));
        super.onDestroy();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getPackageManager().getPackageInstaller().registerSessionCallback(mSessionCallback);
        }
    }

    public void showTipPopupWindow() {
        final View contentView = LayoutInflater.from(this).inflate(R.layout.popuw_content_top_arrow_layout, null);
        RecyclerView recycler_view = contentView.findViewById(R.id.recycler_view);
        recycler_view.setLayoutManager(new LinearLayoutManager(this));
        menuAdapter = new MenuAdapter(menuList);
        recycler_view.setAdapter(menuAdapter);
        RecyclerViewDivider divider = new RecyclerViewDivider.Builder(this)
                .setStyle(RecyclerViewDivider.Style.BETWEEN)
                .setColor(0xff999999)
                .setOrientation(RecyclerViewDivider.VERTICAL)
                .setSize(1)
                .build();
        recycler_view.addItemDecoration(divider);
        menuAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onItemClick(BaseQuickAdapter adapter1, View view, int position) {
                popupWindow.dismiss();
                switch ((String) adapter1.getData().get(position)) {
                    case "管理者模式":
                        showDialog();
                        break;
                    case "用户模式":
                        menuList.clear();
                        menuList.add("管理者模式");
                        menuAdapter.notifyDataSetChanged();
                        showHintDialog("请长按home键检查并清除最近打开的系统设置任务，进入桌面锁定模式", null, 0);
                        break;
                    case "添加白名单":
                        List<AppInfo> list = adapter.getData();
                        List<String> packageNameList = new ArrayList<>();
                        for (AppInfo appInfo : list) {
                            packageNameList.add(appInfo.getPackageName());
                        }

                        Intent intent = new Intent(MainActivity.this, EditActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(VISIBILITY_PACKAGE_NAME_LIST, (Serializable) packageNameList);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        break;
                    case "系统设置":
                        checkAppList = true;
                        startActivity(new Intent(Settings.ACTION_SETTINGS));
                        break;
                    case "退出":
                        showHintDialog("是否确定退出本桌面？", "确定，并跳转切换主屏幕", 2);
                        break;
                }
            }
        });

        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        popupWindow = new PopupWindow(contentView, contentView.getMeasuredWidth(), contentView.getMeasuredHeight(), false);

        // 如果不设置PopupWindow的背景，有些版本就会出现一个问题：无论是点击外部区域还是Back键都无法dismiss弹框
        popupWindow.setBackgroundDrawable(new ColorDrawable());
        // setOutsideTouchable设置生效的前提是setTouchable(true)和setFocusable(false)
        popupWindow.setOutsideTouchable(true);
        // 设置为true之后，PopupWindow内容区域 才可以响应点击事件
        popupWindow.setTouchable(true);
        // 如果希望showAsDropDown方法能够在下面空间不足时自动在anchorView的上面弹出
        // 必须在创建PopupWindow的时候指定高度，不能用wrap_content
        popupWindow.showAsDropDown(rightImg);
    }

    private void showDialog() {
        final Dialog dialog = new Dialog(this, R.style.ActionSheetStyle);
        final View inflate = LayoutInflater.from(this).inflate(R.layout.layout_check_app_lock_pwd_dialog, null);
        final TextView title = inflate.findViewById(R.id.title);
        final TextView pwd = inflate.findViewById(R.id.et_pwd);
        TextInputLayout confirmLayout = inflate.findViewById(R.id.et_confirm_layout);
        final TextInputEditText confirmPwd = inflate.findViewById(R.id.et_confirm_pwd);

        final String locationPwd = SharedPreferencesUtil.getString(MainActivity.this, SharedPreferencesUtil.APP_LOCK_PASSWORD, null);
        if (TextUtils.isEmpty(locationPwd)) {
            title.setText("设置密码");
            confirmLayout.setVisibility(View.VISIBLE);
        } else {
            title.setText("登录");
            confirmLayout.setVisibility(View.GONE);
        }

        inflate.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.hideSoftInput(MainActivity.this);
                String password = pwd.getText().toString();
                if (TextUtils.isEmpty(password)) {
                    ToastUtil.shortMsg("请输入密码");
                } else if (TextUtils.isEmpty(locationPwd)) {
                    if (password.equals(confirmPwd.getText().toString())) {
                        SharedPreferencesUtil.putString(MainActivity.this, SharedPreferencesUtil.APP_LOCK_PASSWORD, Utils.get32MD5Str(password));
                        openLock(dialog);
                    } else {
                        ToastUtil.shortMsg("密码不一致");
                    }
                } else if (!TextUtils.isEmpty(locationPwd)) {
                    if (Utils.get32MD5Str(password).equals(locationPwd)) {
                        openLock(dialog);
                    } else {
                        ToastUtil.shortMsg("密码错误");
                    }
                }
            }
        });

        dialog.setContentView(inflate);
        dialog.setCanceledOnTouchOutside(true);

        //获取当前Activity所在的窗体
        Window dialogWindow = dialog.getWindow();
        //获得窗体的属性
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display d = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        //将属性设置给窗体
        lp.width = d.getWidth() - Utils.dp2px(50);
        dialogWindow.setAttributes(lp);
        dialog.show();
    }

    private void openLock(Dialog dialog) {
        menuList.set(0, "用户模式");
        menuList.add("添加白名单");
        menuList.add("系统设置");
        menuList.add("退出");
        menuAdapter.notifyDataSetChanged();
        dialog.dismiss();
    }

    private void showHintDialog(String contentStr, String okStr, final int type) {
        if (null != hintDialog) hintDialog.dismiss();
        hintDialog = new Dialog(this, R.style.ActionSheetStyle);
        View inflate = LayoutInflater.from(this).inflate(R.layout.layout_hint_dialog, null);
        TextView content = inflate.findViewById(R.id.content);
        content.setText(contentStr);
        Button ok = inflate.findViewById(R.id.ok);
        if (!TextUtils.isEmpty(okStr)) ok.setText(okStr);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hintDialog.dismiss();
                if (type == 1) {
                    gotoHomeSetting();
                } else if (type == 2) {
                    gotoHomeSetting();
                    finish();
                    System.exit(0);
                }
            }
        });
        if (type == 2) {
            Button cancel = inflate.findViewById(R.id.cancel);
            cancel.setVisibility(View.VISIBLE);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hintDialog.dismiss();
                }
            });
        }

        hintDialog.setContentView(inflate);
        hintDialog.setCancelable(false);
        hintDialog.setCanceledOnTouchOutside(false);

        //获取当前Activity所在的窗体
        Window dialogWindow = hintDialog.getWindow();
        //获得窗体的属性
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display d = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        //将属性设置给窗体
        lp.width = d.getWidth() - Utils.dp2px(50);
        dialogWindow.setAttributes(lp);
        hintDialog.show();
    }


    //跳转主屏幕设置页面
    private void gotoHomeSetting() {
        try {
            startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));
        } catch (Exception e) {
            startActivity(new Intent(Settings.ACTION_SETTINGS));
        }
    }

    //更改白名单
    public void updateWhitelist(List<AppInfo> whiteList) {
        addNomalApp(whiteList);

        adapter.replaceData(whiteList);
        SharedPreferencesUtil.putString(this, VISIBILITY_PACKAGE_NAME_LIST, new Gson().toJson(whiteList));
        pushWhiteList(whiteList);
    }

    private void pushWhiteList(List<AppInfo> list) {
        // TODO 提交服务器更新
    }

    private void addNomalApp(List<AppInfo> whiteList) {
        whiteList.add(getAppInfo(getResources().getDrawable(R.drawable.ic_wifi), "WIFI设置"));
        whiteList.add(getAppInfo(getResources().getDrawable(R.drawable.ic_location), "位置信息"));
        whiteList.add(getAppInfo(getResources().getDrawable(R.drawable.ic_bluetooth), "蓝牙设置"));
    }

    //type 1=安装   2=降级卸载   3=卸载
    public void refreshAdapterList(int type, final String packageName) {
        boolean hasChange = false;
        PackageManager pm = App.getInstance().getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> activities = pm.queryIntentActivities(mainIntent, 0);
        final List<AppInfo> data = adapter.getData();
        int oldIndex = -1;
        if (1 == type) {
            //安装(升级、降级、安装新包)
            Iterator<AppInfo> iterator = data.iterator();
            int i = 0;
            while (iterator.hasNext()) {
                AppInfo next = iterator.next();
                if (packageName.equals(next.getPackageName())) {
                    //升级时删除旧的应用信息
                    iterator.remove();
                    oldIndex = i;

                    for (ResolveInfo info : activities) {
                        String packName = info.activityInfo.packageName;
                        if (packageName.equals(packName)) {
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

                            if (oldIndex > -1)
                                data.add(oldIndex, mInfo);
                            else
                                data.add(mInfo);

                            hasChange = true;
                            break;
                        }
                    }
                    if (hasChange) break;
                }
                i++;
            }

            if (!hasChange) {
                //降级安装和新包安装
                for (ResolveInfo info : activities) {
                    String packName = info.activityInfo.packageName;
                    if (packageName.equals(packName)) {
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

                        //降级则复位
                        Iterator<Map.Entry<Integer, String>> it = indexMap.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry<Integer, String> item = it.next();
                            if (packName.equals(item.getValue())) {
                                oldIndex = item.getKey();
                                it.remove();
                                break;
                            }
                        }

                        if (oldIndex > -1)
                            data.add(oldIndex, mInfo);
                        else
                            data.add(mInfo);

                        hasChange = true;
                    }
                }
            }
        } else {
            //卸载
            try {
                //系统应用列表更新不及时
                SystemClock.sleep(2000);
                activities = pm.queryIntentActivities(mainIntent, 0);
            } catch (Exception e) {
            }
            Iterator<AppInfo> iterator = data.iterator();
            int i = 0;
            while (iterator.hasNext()) {
                AppInfo next = iterator.next();
                if (packageName.equals(next.getPackageName())) {
                    boolean hasApk = false;
                    for (ResolveInfo info : activities) {
                        String packName = info.activityInfo.packageName;

                        if (packName.equals(packageName)) {
                            hasApk = true;
                            break;
                        }
                    }

                    if (!hasApk) {
                        iterator.remove();
                        if (2 == type) {
                            indexMap.put(i, packageName);
                        }
                        hasChange = true;
                        break;
                    }
                }
                i++;
            }
        }

        if (hasChange)
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.replaceData(data);

                    List<AppInfo> whiteList = new ArrayList<>();

                    //添加桌面信息
                    AppInfo app = new AppInfo();
                    app.setPackageName(mainActivity.getPackageName());
                    app.setAppName(PackageUtils.getAppName(mainActivity));
                    app.setVersionCode(PackageUtils.getVersionCode(mainActivity) + "");
                    app.setVersionName(PackageUtils.getVersionName(mainActivity));

                    whiteList.addAll(data);
                    whiteList.add(app);

                    //执行完更新任务后重新提交白名单
                    pushWhiteList(whiteList);
                }
            });
    }

    //屏蔽返回键
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) return true;
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
    }
}

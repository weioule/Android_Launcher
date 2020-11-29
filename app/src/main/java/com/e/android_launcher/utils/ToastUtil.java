package com.e.android_launcher.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

import com.e.android_launcher.App;

/**
 * Created by weioule
 * on 2019/12/12
 */
public class ToastUtil {
    private static Toast toast;

    public ToastUtil() {
    }

    @SuppressLint("WrongConstant")
    public static void shortMsg(String content) {
        if (toast == null) {
            toast = Toast.makeText(App.getInstance().getApplicationContext(), content, 0);
        } else {
            toast.setDuration(0);
            toast.setText(content);
        }

        toast.show();
    }

    @SuppressLint("WrongConstant")
    public static void shortMsg(int resID) {
        Context context = App.getInstance().getApplicationContext();
        String content = context.getResources().getString(resID);
        if (toast == null) {
            toast = Toast.makeText(context, content, 0);
        } else {
            toast.setDuration(0);
            toast.setText(content);
        }

        toast.show();
    }

    @SuppressLint("WrongConstant")
    public static void longMsg(String content) {
        if (toast == null) {
            toast = Toast.makeText(App.getInstance().getApplicationContext(), content, 1);
        } else {
            toast.setDuration(1);
            toast.setText(content);
        }

        toast.show();
    }

    @SuppressLint("WrongConstant")
    public static void longMsg(int resID) {
        Context context = App.getInstance().getApplicationContext();
        String content = context.getResources().getString(resID);
        if (toast == null) {
            toast = Toast.makeText(context, content, 1);
        } else {
            toast.setDuration(1);
            toast.setText(content);
        }

        toast.show();
    }
}


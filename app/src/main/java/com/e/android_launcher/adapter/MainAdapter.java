package com.e.android_launcher.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.e.android_launcher.R;
import com.e.android_launcher.bean.AppInfo;

import java.io.Serializable;
import java.util.List;

/**
 * Created by weioule
 * on 2020/11/21
 */
public class MainAdapter extends BaseQuickAdapter<AppInfo, BaseViewHolder> implements Serializable {

    public MainAdapter(@Nullable List<AppInfo> data) {
        super(R.layout.desktop_gridview_item, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, AppInfo item) {
        helper.setImageDrawable(R.id.logo, item.getIco());
        helper.setText(R.id.name, item.getAppName());
        helper.addOnClickListener(R.id.logo);
    }
}

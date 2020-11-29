package com.e.android_launcher.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.e.android_launcher.R;

import java.util.List;

/**
 * Created by weioule
 * on 2020/11/21
 */
public class MenuAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

    public MenuAdapter(@Nullable List<String> data) {
        super(R.layout.menu_item_layout, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, String item) {
        helper.setText(R.id.menu_name, item);
    }
}

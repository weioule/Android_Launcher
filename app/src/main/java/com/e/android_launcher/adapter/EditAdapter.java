package com.e.android_launcher.adapter;

import android.text.TextPaint;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.e.android_launcher.R;
import com.e.android_launcher.bean.AppInfo;
import com.e.android_launcher.utils.Utils;

import java.util.List;

/**
 * Created by weioule
 * on 2020/11/21
 */
public class EditAdapter extends BaseQuickAdapter<AppInfo, BaseViewHolder> {

    public EditAdapter(@Nullable List<AppInfo> data) {
        super(R.layout.edit_gridview_item, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, final AppInfo item) {
        helper.setImageDrawable(R.id.logo, item.getIco());
        TextView name = helper.getView(R.id.name);
        name.setText(item.getAppName());

        final ImageView img = helper.getView(R.id.img);
        img.setSelected(item.isVisibility());
        helper.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img.setSelected(!img.isSelected());
                item.setVisibility(img.isSelected());
            }
        });

        int width = (Utils.measureScreenWidth(mContext) - Utils.dp2px(25)) / 4;
        TextPaint textPaint = name.getPaint();
        float textPaintWidth = textPaint.measureText(item.getAppName());
        name.setPadding(textPaintWidth >= width - Utils.dp2px(5) ? 0 : Utils.dp2px(5), 0, 0, 0);
    }
}

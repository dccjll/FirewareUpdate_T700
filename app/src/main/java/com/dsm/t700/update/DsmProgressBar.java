package com.dsm.t700.update;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by dessmann on 16/3/30.
 */
public class DsmProgressBar {
    Context context;
    int style;
    String title;
    String message;
    int icon;
    int max;
    ProgressDialog.OnClickListener submitListener;
    boolean cancelable;
    int currentProgress;

    public DsmProgressBar(Context context, int style, String title, String message, int icon, int max, ProgressDialog.OnClickListener submitListener, boolean cancelable, int currentProgress){
        this.context = context;
        this.style = style;
        this.title = title;
        this.message = message;
        this.icon = icon;
        this.max = max;
        this.submitListener = submitListener;
        this.cancelable = cancelable;
        this.currentProgress = currentProgress;
    }

    public ProgressDialog build(){
        ProgressDialog dialog = new ProgressDialog(context);
        //设置进度条风格 ProgressDialog.STYLE_HORIZONTAL
        dialog.setProgressStyle(style);
        //设置ProgressDialog 标题
        dialog.setTitle(title);
        //设置ProgressDialog 提示信息
        dialog.setMessage(message);
        //设置ProgressDialog 标题图标 android.R.drawable.ic_dialog_alert
        dialog.setIcon(icon);
        //设置ProgressDialog的最大进度
        dialog.setMax(max);
        //设置ProgressDialog 的一个Button
        dialog.setButton("确定", submitListener);
        //设置ProgressDialog 是否可以按退回按键取消
        dialog.setCancelable(cancelable);
        //显示
//            dialog.show();
        //设置ProgressDialog的当前进度
        dialog.setProgress(currentProgress);
        return dialog;
    }
}

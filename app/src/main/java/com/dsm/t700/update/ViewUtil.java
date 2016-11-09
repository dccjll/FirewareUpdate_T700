package com.dsm.t700.update;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by dessmann on 16/3/30.
 */
public class ViewUtil {
    private final static String TAG = ViewUtil.class.getSimpleName();

    public static void closeDialog(Dialog dialog){
        if(dialog != null && dialog.isShowing()){
            dialog.dismiss();
        }
    }

    public static void printLogAndTips(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        Log.d(TAG, message);
    }
}

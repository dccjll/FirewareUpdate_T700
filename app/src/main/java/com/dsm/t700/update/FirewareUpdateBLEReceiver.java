package com.dsm.t700.update;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.bluetoothle.BluetoothLeManage;

/**
 * Created by dessmann on 16/8/23.
 * 固件更新蓝牙消息接收器
 */
public class FirewareUpdateBLEReceiver extends BroadcastReceiver{
    private final static String tag = FirewareUpdateBLEReceiver.class.getSimpleName();

    public FirewareUpdateBLEReceiver() {

    }

    private Object responseObj;

    public void setResponseObj(Object responseObj) {
        this.responseObj = responseObj;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(BluetoothLeManage.ACTION_BLE_ERROR.equalsIgnoreCase(intent.getAction())){
            String error = intent.getExtras().getString(BluetoothLeManage.ACTION_BLE_ERROR);
            if(error == null || error.equalsIgnoreCase("null")){
                LogUtil.d(tag, "错误广播消息,没有消息体");
                return;
            }
            LogUtil.d(tag, "error=" + error);
            if(responseObj instanceof OnBLEListener.BLEDataWrittenListener){
                ((OnBLEListener.BLEDataWrittenListener)responseObj).writeFailure(error);
            }
        }else if(BluetoothLeManage.ACTION_DATA_AVAILABLE.equalsIgnoreCase(intent.getAction())){
            byte[] data = intent.getByteArrayExtra(BluetoothLeManage.ACTION_DATA_AVAILABLE);
            LogUtil.e(tag, "广播中接收到的数据:" + data);
            //...
        }else if(BluetoothLeManage.ACTION_WRITTEN_SUCCESS.equalsIgnoreCase(intent.getAction())){
            LogUtil.d(tag, "数据写入完成");
            if(responseObj instanceof OnBLEListener.BLEDataWrittenListener){
                ((OnBLEListener.BLEDataWrittenListener)responseObj).writeSuccess();
            }
        }
    }
}

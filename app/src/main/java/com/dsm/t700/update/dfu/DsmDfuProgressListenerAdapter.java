package com.dsm.t700.update.dfu;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;

import com.dsm.t700.update.DeviceListActivity;
import com.dsm.t700.update.LogUtil;
import com.dsm.t700.update.ViewUtil;

import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;


public class DsmDfuProgressListenerAdapter extends DfuProgressListenerAdapter{
	private String TAG = DsmDfuProgressListenerAdapter.class.getSimpleName();
	private ProgressDialog dsmProgressDialog = null;
	private Context context;
	private Handler progressHandler = null;

	public void setProgressHandler(Handler progressHandler) {
		this.progressHandler = progressHandler;
	}

	public void setDialog(ProgressDialog dsmProgressDialog) {
		this.dsmProgressDialog = dsmProgressDialog;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	@Override
	public void onDeviceConnecting(final String deviceAddress) {
		LogUtil.d(TAG , "onDeviceConnecting");
	}

	@Override
	public void onDfuProcessStarting(final String deviceAddress) {
		LogUtil.d(TAG , "onDfuProcessStarting");
	}

	@Override
	public void onEnablingDfuMode(final String deviceAddress) {
		LogUtil.d(TAG , "onEnablingDfuMode");
	}

	@Override
	public void onFirmwareValidating(final String deviceAddress) {
		LogUtil.d(TAG , "onFirmwareValidating");
	}

	@Override
	public void onDeviceDisconnecting(final String deviceAddress) {
		LogUtil.d(TAG , "onDeviceDisconnecting");
	}

	@Override
	public void onDfuCompleted(final String deviceAddress) {
		LogUtil.d(TAG , "onDfuCompleted");
		progressHandler.obtainMessage(DeviceListActivity.HANDLER_DFU_COMPLETE).sendToTarget();
	}

	@Override
	public void onDfuAborted(final String deviceAddress) {
		LogUtil.d(TAG , "onDfuAborted");
//		ViewUtil.closeDialog(dsmProgressDialog);
//		ViewUtil.printLogAndTips(context, "固件升级失败");
		progressHandler.obtainMessage(DeviceListActivity.HANDLER_PROGRESS_ERROR, "onDfuAborted").sendToTarget();
	}

	@Override
	public void onProgressChanged(final String deviceAddress, final int percent, final float speed, final float avgSpeed, final int currentPart, final int partsTotal) {
		LogUtil.d(TAG , "onProgressChanged\ndeviceAddress=" + deviceAddress + "\npercent=" + percent + "\nspeed=" + speed + "\navgSpeed=" + avgSpeed + "\ncurrentPart=" + currentPart + "\npartsTotal=" + partsTotal);
		progressHandler.obtainMessage(DeviceListActivity.HANDLER_PROGRESS_UPDATE, percent, 0).sendToTarget();
	}

	@Override
	public void onError(final String deviceAddress, final int error, final int errorType, final String message) {
		LogUtil.d(TAG , "onError");
//		ViewUtil.closeDialog(dsmProgressDialog);
//		ViewUtil.printLogAndTips(context, "固件升级失败");
		progressHandler.obtainMessage(DeviceListActivity.HANDLER_PROGRESS_ERROR, "onError").sendToTarget();
	}
}

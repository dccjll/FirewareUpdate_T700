package com.dsm.t700.update;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bluetoothle.BluetoothLeManage;
import com.bluetoothle.BluetoothLeService;
import com.bluetoothle.Request;
import com.dsm.t700.update.assets.AssetsCopyer;
import com.dsm.t700.update.dfu.DfuService;
import com.dsm.t700.update.dfu.DsmDfuProgressListenerAdapter;
import com.yolanda.nohttp.NoHttp;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;

/**
 * Created by dessmann on 16/3/29.
 * 生产固件更新
 */
public class DeviceListActivity extends Activity implements AdapterView.OnItemClickListener {

    private final static String TAG = DeviceListActivity.class.getSimpleName();

    private String filepathWithName = null;

    private String filelocalpath;

    private String assetsFilePath = "fireware";

    private boolean firewareCheck = false;

    private ListView deviceListView;

    private ProgressDialog dsmProgressDialog = null;//进度条

    private Dialog _dialog;
    private FirewareUpdateBLEReceiver firewareUpdateBLEReceiver = new FirewareUpdateBLEReceiver();

    private Spinner spinner;
    private List<String> firewareNameList = new ArrayList<String>();
    private static ArrayAdapter<String> firewareNameAdapter;

    public final static String SERVICE_UUID = "0000ffe5-0000-1000-8000-00805f9b34fb";
    public final static String CHARACTERISTIC_UUID = "0000ffe9-0000-1000-8000-00805f9b34fb";

    public final static String NOTIFICATION_SERVICE_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb";
    public final static String NOTIFICATION_CHARACTERISTIC_UUID = "0000ffe4-0000-1000-8000-00805f9b34fb";
    public final static String BLUETOOTHGATTDESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb";

    public final static String GUARD_SERVICE_UUID = "0000fff5-0000-1000-8000-00805f9b34fb";
    public final static String GUARD_CHARACTERISTIC_UUID = "0000fff9-0000-1000-8000-00805f9b34fb";

//    private final static String FirewareVersioin_T510 = "T510";
//    private final static String FirewareVersioin_T700 = "T700";
//    private final static String FirewareVersioin_T710 = "T710";
//    private final static String FirewareVersioin_T800 = "T800";
//    private String selectedFirewareVersion = null;

    private DsmDfuProgressListenerAdapter dsmDfuProgressListenerAdapter = null;

    private BluetoothDevice mSelectedBluetoothDevice;

    public static DeviceListAdapter mDeviceListAdapter = null;

    private List<FirewareVersion> firewareVersionList = new ArrayList<FirewareVersion>();

    public static List<BluetoothDevice> mBluetoothDeviceList = new ArrayList<>();
    private BluetoothAdapter mBluetoothAdapter = null;
    //扫描目标设备5s超时
    private boolean isRunningScan = false;
    private static final long timeoutScanDevice = 5000;
    private final Handler timeoutScanDeviceHandler = new Handler();
    private Runnable timeoutScanDeviceRunnable = new Runnable() {
        @Override
        public void run() {
            stopScan();
        }
    };

    private final static int HANDLER_FOUND_NEW_DEVICE = 0x0001;
    private final static int HANDLER_FOUND_NEW_FIREWARE = 0x0002;
    private static class DeviceListManageHandler extends  Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_FOUND_NEW_DEVICE:
                    mDeviceListAdapter.notifyDataSetChanged();
                    break;
                case HANDLER_FOUND_NEW_FIREWARE:
                    firewareNameAdapter.notifyDataSetChanged();
                    break;
                default:
                    super.handleMessage(msg);
            }
            super.handleMessage(msg);
        }
    }
    private final DeviceListManageHandler deviceListManageHandler = new DeviceListManageHandler();

    public final static int HANDLER_PROGRESS_UPDATE = 0x0001;
    public final static int HANDLER_DFU_COMPLETE = 0x0002;
    public final static int HANDLER_PROGRESS_ERROR = 0x0003;
    private class UpdateProgressHandler extends Handler{
        private WeakReference<DeviceListActivity> mActivity;
        private ProgressDialog dsmProgressDialog;
        public UpdateProgressHandler(DeviceListActivity activity){
            mActivity = new WeakReference<DeviceListActivity>(activity);
        }
        private void setDsmProgressDialog(ProgressDialog dsmProgressDialog){
            this.dsmProgressDialog = dsmProgressDialog;
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_PROGRESS_UPDATE:
                    if (dsmProgressDialog != null) {
                        dsmProgressDialog.setProgress(msg.arg1);
                    }
                    break;
                case HANDLER_DFU_COMPLETE:
                    ViewUtil.closeDialog(dsmProgressDialog);
                    ViewUtil.printLogAndTips(mActivity.get(), "固件升级成功");
                    startScan();
                    break;
                case HANDLER_PROGRESS_ERROR:
                    ViewUtil.closeDialog(dsmProgressDialog);
                    String error = (String) msg.obj;
                    if(error == null || error.equalsIgnoreCase("null")){
                        ViewUtil.printLogAndTips(mActivity.get(), "固件升级失败");
                    }else{
                        ViewUtil.printLogAndTips(mActivity.get(), "固件升级失败，" + error);
                    }
                    startScan();
                    break;
                default:
                    super.handleMessage(msg);
            }
            super.handleMessage(msg);
        }
    }
    private final UpdateProgressHandler updateProgressHandler = new UpdateProgressHandler(this);

    String firewareversion = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!isBLEEnabled()) {
            Toast.makeText(this, "蓝牙打开失败", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        NoHttp.initialize(getApplication());
        spinner = (Spinner) this.findViewById(R.id.spinner);
        firewareNameAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, firewareNameList);
        firewareNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //第四步：将适配器添加到下拉列表上
        spinner.setAdapter(firewareNameAdapter);
        //第五步：为下拉列表设置各种事件的响应，这个事响应菜单被选中
        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                firewareversion = firewareNameAdapter.getItem(arg2);
                LogUtil.d(TAG, "选择的固件版本为：" + firewareversion);
                filepathWithName = filelocalpath + "/" + firewareversion + ".zip";
                LogUtil.d(TAG, "升级的固件路径为：" + filepathWithName);
//                if(firewareversion.contains("S510_")){
//                    selectedFirewareVersion = FirewareVersioin_T510;
//                }else if(firewareversion.contains("S7_")){
//                    selectedFirewareVersion = FirewareVersioin_T700;
//                }else if(firewareversion.contains("S710_")){
//                    selectedFirewareVersion = FirewareVersioin_T710;
//                }else if(firewareversion.contains("S800_")){
//                    selectedFirewareVersion = FirewareVersioin_T800;
//                }
            }
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
        /*下拉菜单弹出的内容选项触屏事件处理*/
        spinner.setOnTouchListener(new Spinner.OnTouchListener(){
            public boolean onTouch(View v, MotionEvent event) {

                return false;
            }
        });
        /*下拉菜单弹出的内容选项焦点改变事件处理*/
        spinner.setOnFocusChangeListener(new Spinner.OnFocusChangeListener(){
            public void onFocusChange(View v, boolean hasFocus) {

            }
        });


        filelocalpath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/dsmsecondlock";

        deviceListView = (ListView) this.findViewById(R.id.deviceListView);
        mDeviceListAdapter = new DeviceListAdapter(this);
        deviceListView.setAdapter(mDeviceListAdapter);
        deviceListView.setOnItemClickListener(this);

        dsmDfuProgressListenerAdapter = new DsmDfuProgressListenerAdapter();

//        String temp1 = "S800_FPA_app20160704.zip";
//        String temp2 = "GRIDLOCK_app_20160707.zip";
//        String temp3 = "S7_app20160627.zip";
//        String tempfile1 = filelocalpath + "/" + temp1;
//        String tempfile2 = filelocalpath + "/" + temp2;
//        String tempfile3 = filelocalpath + "/" + temp3;
//        if(new File(tempfile1).exists() && new File(tempfile2).exists() && new File(tempfile3).exists()){
//            firewareNameList.clear();;
//            firewareNameList.add(temp1);
//            firewareNameList.add(temp2);
//            firewareNameList.add(temp3);
//            firewareCheck = true;
//            firewareNameAdapter.notifyDataSetChanged();
//        }else{
//            deleteAllFilesAtPath(filelocalpath);
//            firewareCheck = false;
//            firewareNameList.clear();
//            firewareNameAdapter.notifyDataSetChanged();
//            getNearestHardwareVersionFromServer();
//        }
        deleteAllFilesAtPath(filelocalpath);
        firewareCheck = false;
        firewareNameList.clear();
        firewareNameAdapter.notifyDataSetChanged();
        //getNearestHardwareVersionFromServer();
        getFirewareVersionFromAssets();
    }

    private void getFirewareVersionFromAssets() {
        AssetsCopyer.copyFolderFromAssets(this, assetsFilePath, filelocalpath);
        List<String> fileNames = AssetsCopyer.getAllFileFromLocal(filelocalpath);
        if (fileNames != null) {
            firewareNameList.addAll(fileNames);
            firewareCheck = true;
        } else {
            LogUtil.d(TAG, "固件版本列表为空");
        }
        deviceListManageHandler.obtainMessage(HANDLER_FOUND_NEW_FIREWARE).sendToTarget();
        startService(new Intent(this, BluetoothLeService.class));
    }

    //从服务器获取最新固件版本号
//    private void getNearestHardwareVersionFromServer() {
//        RequestQueue requestQueue = NoHttp.newRequestQueue();
//        Request<String> request = NoHttp.createStringRequest(App.statics.url_firewareversion, RequestMethod.POST);
//        requestQueue.add(0, request, new OnResponseListener<String>() {
//            @Override
//            public void onStart(int what) {
//
//            }
//
//            @Override
//            public void onSucceed(int what, Response<String> response) {
//                String strResult = response.get();
//                LogUtil.d(TAG, "OK Response:" + strResult);
//                firewareVersionList = parseHardwareVersion(strResult);
//                if(firewareVersionList == null || firewareVersionList.size() == 0){
//                    LogUtil.d(TAG, "没有获取到版本号");
//                    return;
//                }
//                downloadNearestHardwareFromServer(0);
//            }
//
//            @Override
//            public void onFailed(int what, String url, Object tag, Exception exception, int responseCode, long networkMillis) {
//                LogUtil.d(TAG, "获取最新固件版本号，发生异常");
//            }
//
//            @Override
//            public void onFinish(int what) {
//
//            }
//        });
//    }
//
//    //从服务器下载最新固件
//    private void downloadNearestHardwareFromServer(final int index) {
//            String firewareURL = firewareVersionList.get(index).getAppUrl();
//            String fileUrlPath = firewareURL.substring(0, firewareURL.lastIndexOf("/"));
//            String fileName = firewareURL.substring(firewareURL.lastIndexOf("/") + 1);
//            LogUtil.d(TAG, "url=" + firewareURL + "\nfileUrlPath=" + fileUrlPath + "\nfileName=" + fileName);
//            //检测本地是否有该固件版本
//            File firewareFile = new File(filelocalpath, fileName);
//            if(firewareFile.exists()){
//                LogUtil.d(TAG, "存在固件升级文件夹，尝试清空");
//                deleteAllFilesAtPath(filelocalpath);
//            }
//            LogUtil.d(TAG, "从服务器下载最新固件...");
//            CommonDownLoadUtil.downloadFile(fileUrlPath, fileName, filelocalpath,
//                    new CommonDownLoadUtil.DownloadCallBack() {
//
//                        @Override
//                        public void downloadSuccess(String filepathWithName) {
//                            firewareNameList.add(firewareVersionList.get(index).getAppVersion());
//                            deviceListManageHandler.obtainMessage(HANDLER_FOUND_NEW_FIREWARE).sendToTarget();
//                            firewareCheck = true;
//                            LogUtil.d(TAG, "从服务器下载最新固件成功，固件在本地的完成路径为:" + filepathWithName);
//                            int i = index;
//                            if(index == firewareVersionList.size() - 1){
//                                LogUtil.d(TAG, "固件全部下载完成");
//                                return;
//                            }
//                            LogUtil.d(TAG, "继续下载下一个固件");
//                            downloadNearestHardwareFromServer(++i);
//                        }
//
//                        @Override
//                        public void downloadFailure(String error) {
//                            ViewUtil.printLogAndTips(DeviceListActivity.this, error);
//                            if(index == firewareVersionList.size() - 1){
//                                LogUtil.d(TAG, "固件全部下载完成");
//                                return;
//                            }
//                            LogUtil.d(TAG, "继续下载下一个固件");
//                            downloadNearestHardwareFromServer(index + 1);
//                        }
//                    });
//    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        stopScan();
        if(!firewareCheck){
            ViewUtil.printLogAndTips(this, "本地未发现固件或者网络不好，请退出重新登录后再试");
            return;
        }
//        if(Util.isEmpty(selectedFirewareVersion) || (!selectedFirewareVersion.equalsIgnoreCase(FirewareVersioin_T510) &&
//                !selectedFirewareVersion.equalsIgnoreCase(FirewareVersioin_T700) && !selectedFirewareVersion.equalsIgnoreCase(FirewareVersioin_T710) &&
//                !selectedFirewareVersion.equalsIgnoreCase(FirewareVersioin_T800))){
//            ViewUtil.printLogAndTips(this, "请先选择固件");
//            return;
//        }
        mSelectedBluetoothDevice = mDeviceListAdapter.getBluetoothDeviceList().get(position);
        String deviceName = mSelectedBluetoothDevice.getName();
        if(deviceName.startsWith("l_") || deviceName.startsWith("L_") || deviceName.equalsIgnoreCase("LockDfu")){//是短设备，直接进入固件更新模式
            startUpdateFireware(true, mSelectedBluetoothDevice.getAddress());
            return;
        }
        //是长设备,需要发送固件更新命令进入更新模式，先选择设备类型，目前门禁设备与锁设备进入更新模式的命令不一样
        selectDeviceType();

//        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DeviceListActivity.this);
//        alertDialogBuilder.setTitle("更新提醒");
//        alertDialogBuilder.setMessage("确定更新选中设备固件吗?");
//        alertDialogBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//                startBLEServiceTransferData();
//            }
//        });
//        alertDialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//        alertDialogBuilder.show();
//        FirewareListDialog.showDialog(this,firewareVersionList,new FirewareListDialog.OnItemClickListener(){
//
//            @Override
//            public void onItemClick(FirewareListDialog dialog, String url) {
//                filepathWithName = filelocalpath + "/" + url.substring(url.lastIndexOf("/") + 1);
//                LogUtil.d(TAG, "选中固件的路径为："+ filepathWithName);
//                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DeviceListActivity.this);
//                alertDialogBuilder.setTitle("更新提醒");
//                alertDialogBuilder.setMessage("确定更新选中设备固件吗?");
//                alertDialogBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                        startBLEServiceTransferData();
//                    }
//                });
//                alertDialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                });
//                alertDialogBuilder.show();
//            }
//        });
    }

    private void selectDeviceType(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择固件类型");
        String[] types =  new String[]{"小滴锁", "门禁", "门禁试开"};
        builder.setItems(
                types,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        _dialog = new ProgressDialog(DeviceListActivity.this);
                        _dialog.show();
                        if(which == 0 || which == 1){
                            sendCMDIntoUpdateMode(which);
                            return;
                        }
                        //门禁试开
                        guardTryOpen();
                    }
                });
        builder.show();
    }

    /**
     * 门禁试开
     */
    private void guardTryOpen(){
        firewareUpdateBLEReceiver.setResponseObj(
                new OnBLEListener.BLEDataWrittenListener() {
                    @Override
                    public void writeSuccess() {
                        ViewUtil.closeDialog(_dialog);
                        firewareUpdateBLEReceiver.setResponseObj(null);
                        ViewUtil.printLogAndTips(DeviceListActivity.this, "开门成功");
                    }

                    @Override
                    public void writeFailure(String error) {
                        ViewUtil.closeDialog(_dialog);
                        firewareUpdateBLEReceiver.setResponseObj(null);
                        ViewUtil.printLogAndTips(DeviceListActivity.this, "开门失败");
                    }
                }
        );

        Request request = new Request();
        request.setTargetDeviceMacAddress(mSelectedBluetoothDevice.getAddress());
        request.setReceiveDataFromBLEDevice(false);
        byte[] data = null;
        data = "Open the door".getBytes();
        request.setService_uuid_write(GUARD_SERVICE_UUID);
        request.setCharacteristics_uuid_write(GUARD_CHARACTERISTIC_UUID);
        request.setData(data);
        Intent intent = new Intent();
        intent.setAction("com.bluetoothle.ACTION_BLUETOOTHLESERVICE_REQUEST");
        intent.putExtra("com.bluetoothle.EXTRA_BLUETOOTHLESERVICE_REQUEST", request);
        sendBroadcast(intent);
    }

    /**
     * 发送固件更新命令，使设备进入更新模式 0 小滴锁 1 门禁
     * @param deviceType
     */
    private void sendCMDIntoUpdateMode(int deviceType){
        firewareUpdateBLEReceiver.setResponseObj(
                new OnBLEListener.BLEDataWrittenListener() {
                    @Override
                    public void writeSuccess() {
                        firewareUpdateBLEReceiver.setResponseObj(null);
                        ViewUtil.closeDialog(_dialog);
                        ViewUtil.printLogAndTips(DeviceListActivity.this, "进入更新模式成功");
                        confirmUpdate();
//                        LogUtil.d(TAG, "进入更新模式成功,准备开始更新");
//                        String newmac = caculateTargetAddress();
//                        if(newmac == null){
//                            ViewUtil.printLogAndTips(DeviceListActivity.this, "地址转换失败");
//                            return;
//                        }
//                        startUpdateFireware(true, newmac);
                    }

                    @Override
                    public void writeFailure(String error) {
                        firewareUpdateBLEReceiver.setResponseObj(null);
                        ViewUtil.closeDialog(_dialog);
//                        ViewUtil.printLogAndTips(DeviceListActivity.this, "进入更新模式失败");
                        LogUtil.d(TAG, "进入更新模式成功,但设备可能已经收到了数据并进入了更新模式，尝试更新...");
                        confirmUpdate();
//                        String newmac = caculateTargetAddress();
//                        if(newmac == null){
//                            ViewUtil.printLogAndTips(DeviceListActivity.this, "地址转换失败");
//                            return;
//                        }
//                        startUpdateFireware(true, newmac);
                    }
                }
        );
        Request request = new Request();
        request.setTargetDeviceMacAddress(mSelectedBluetoothDevice.getAddress());
        request.setReceiveDataFromBLEDevice(false);
        byte[] data = null;
        if(deviceType == 0){
            data = new byte[]{(byte) 0xFE, 0x01, (byte) 0xF4, 0x00, 0x00, (byte) 0xF5};
            request.setService_uuid_write(SERVICE_UUID);
            request.setCharacteristics_uuid_write(CHARACTERISTIC_UUID);
            request.setService_uuid_notification(NOTIFICATION_SERVICE_UUID);
            request.setCharacteristics_uuid_notification(NOTIFICATION_CHARACTERISTIC_UUID);
            request.setCharacteristics_descriptor_uuid_notification(BLUETOOTHGATTDESCRIPTOR_UUID);
        }else if(deviceType == 1){
            data = "SW update".getBytes();
            request.setService_uuid_write(GUARD_SERVICE_UUID);
            request.setCharacteristics_uuid_write(GUARD_CHARACTERISTIC_UUID);
        }
        request.setData(data);
        Intent intent = new Intent();
        intent.setAction("com.bluetoothle.ACTION_BLUETOOTHLESERVICE_REQUEST");
        intent.putExtra("com.bluetoothle.EXTRA_BLUETOOTHLESERVICE_REQUEST", request);
        sendBroadcast(intent);
    }

    private void confirmUpdate(){
        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceListActivity.this);
        builder.setTitle("小心操作");
        builder.setMessage("进入更新模式成功,确定更新所选固件吗?选中的固件版本为：" + firewareversion);
        builder.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newmac = caculateTargetAddress();
                        if(newmac == null){
                            ViewUtil.printLogAndTips(DeviceListActivity.this, "地址转换失败");
                            return;
                        }
                        startUpdateFireware(true, newmac);
                    }
                });
        builder.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startScan();
                    }
                });
        builder.show();
    }

    private String caculateTargetAddress() {
        String newtargetmacaddress = null;
        try {
            String targetmacaddress = mSelectedBluetoothDevice.getAddress();
            String foremacaddress = targetmacaddress.substring(0, targetmacaddress.length() - 2);
            String lastsegmentmac = targetmacaddress.split(":")[5];
            int tempmacint = Integer.parseInt(lastsegmentmac, 16);
            int lastsegmentmacint = 0;
            if (tempmacint != 0xFF) {
                lastsegmentmacint = tempmacint + 1;
            }
            String lastsegmentmacupdate = Integer.toHexString(lastsegmentmacint);
            if (lastsegmentmacupdate.length() == 1) {
                lastsegmentmacupdate = "0" + lastsegmentmacupdate;
            }
            lastsegmentmacupdate = lastsegmentmacupdate.toUpperCase(Locale.US);
            newtargetmacaddress = foremacaddress + lastsegmentmacupdate;
            LogUtil.d(TAG, "targetmacaddress=" + targetmacaddress + "\nnewtargetmacaddress=" + newtargetmacaddress);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            newtargetmacaddress = null;
        }
        return newtargetmacaddress;
    }

    private void startUpdateFireware(boolean showAlertDialog, final String newmac){
        if(showAlertDialog){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DeviceListActivity.this);
            alertDialogBuilder.setTitle("更新提醒");
            alertDialogBuilder.setMessage(Html.fromHtml("确定更新选中设备固件吗?<br/>固件版本为:" + firewareversion));
            alertDialogBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    startBLEServiceTransferData(newmac);
                }
            });
            alertDialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alertDialogBuilder.show();
            return;
        }
        startBLEServiceTransferData(newmac);
    }

    private void startBLEServiceTransferData(String mac) {
        dsmProgressDialog = new DsmProgressBar(
                this,
                ProgressDialog.STYLE_HORIZONTAL,
                "进度提醒",
                "",
                0,
                100,
                null,
                false,
                0).build();
        dsmProgressDialog.show();
        updateProgressHandler.setDsmProgressDialog(dsmProgressDialog);
        dsmDfuProgressListenerAdapter.setDialog(dsmProgressDialog);
        dsmDfuProgressListenerAdapter.setProgressHandler(updateProgressHandler);
        dsmDfuProgressListenerAdapter.setContext(this);
        final String extension = "(?i)ZIP"; // (?i) =  case insensitive
        final boolean statusOk = MimeTypeMap.getFileExtensionFromUrl(filepathWithName).matches(extension);
        if (!statusOk) {
            ViewUtil.printLogAndTips(this, "请选择一个zip文件");
            ViewUtil.closeDialog(dsmProgressDialog);
            return;
        }

        final DfuServiceInitiator starter = new DfuServiceInitiator(mac).setDeviceName(mSelectedBluetoothDevice.getName()).setKeepBond(false);
        starter.setZip(null, filepathWithName);
        starter.start(this, DfuService.class);
        LogUtil.d(TAG, "starter started!");
    }

    private boolean isBLEEnabled() {
        final BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();
        return mBluetoothAdapter != null && mBluetoothAdapter.enable();
    }

    @Override
    protected void onResume() {
        super.onResume();
        DfuServiceListenerHelper.registerProgressListener(this, dsmDfuProgressListenerAdapter);
        startScan();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeManage.ACTION_BLE_ERROR);
        intentFilter.addAction(BluetoothLeManage.ACTION_WRITTEN_SUCCESS);
        registerReceiver(firewareUpdateBLEReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DfuServiceListenerHelper.unregisterProgressListener(this, dsmDfuProgressListenerAdapter);
        stopScan();
        unregisterReceiver(firewareUpdateBLEReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings_scandevice:
                startScan();
                break;
            case R.id.action_settings_stopscan:
                stopScan();
                break;
            default:
                super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    private void startScan() {
        if (!isRunningScan) {
            isRunningScan = true;
            mBluetoothDeviceList.clear();
            mDeviceListAdapter.notifyDataSetChanged();
            new Thread(new Runnable() {

                @Override
                public void run() {
                    mBluetoothAdapter.startLeScan(mLeScanCallback);
                    timeoutScanDeviceHandler.postDelayed(timeoutScanDeviceRunnable, timeoutScanDevice);
                }
            }).start();
        }
    }

    private void stopScan() {
        timeoutScanDeviceHandler.removeCallbacks(timeoutScanDeviceRunnable);
        if (isRunningScan) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        isRunningScan = false;
    }

    //扫描到周围的蓝牙设备回调
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {

            new Thread(new Runnable() {

                @Override
                public void run() {
                    LogUtil.d(TAG, "找到附近的设备,mac=" + device.getAddress());
                    boolean containsDevice = false;
                    for (BluetoothDevice _device : mBluetoothDeviceList) {
                        if (_device.getAddress().equals(device.getAddress())) {
                            containsDevice = true;
                        }
                    }
                    if (!containsDevice) {
                        mBluetoothDeviceList.add(device);
                        deviceListManageHandler.obtainMessage(HANDLER_FOUND_NEW_DEVICE).sendToTarget();
                    }
                }
            }).start();
        }
    };

    private void deleteAllFilesAtPath(String delpath){
        LogUtil.d(TAG, "delpath=" + delpath);
        File file = new File(delpath);
        if(!file.exists()){
            LogUtil.d(TAG, "deleteAllFilesAtPath,路径不存在, delpath=" + delpath);
            return;
        }
        // 当且仅当此抽象路径名表示的文件存在且 是一个目录时，返回 true
        if (!file.isDirectory()) {
            LogUtil.d(TAG, "!file.isDirectory()");
            if(file.delete()){
                LogUtil.d(TAG, "file delete success");
            }else{
                LogUtil.d(TAG, "file delete failure");
            }
        } else {
            LogUtil.d(TAG, "file.isDirectory()");
            String[] filelist = file.list();
            for (int i = 0; i < filelist.length; i++) {
                File delfile = new File(delpath + "/" + filelist[i]);
                if (!delfile.isDirectory()) {
                    if(delfile.delete()){
                        LogUtil.d(TAG, delfile.getAbsolutePath() + ",删除文件成功");
                    }else{
                        LogUtil.d(TAG, delfile.getAbsolutePath() + ",删除文件失败");
                    }
                } else if (delfile.isDirectory()) {
                    deleteAllFilesAtPath(delpath + "/" + filelist[i]);
                }
            }
            LogUtil.d(TAG, file.getAbsolutePath()+"删除成功");
        }

    }

//    请求返回示例
//    {
//        "status": 1,
//            "data": [
//        {
//            "id": 2,
//                "item2": null,
//                "appUrl": "http://121.43.225.110:8080/base/softUp/S7_app20160520.zip",
//                "appName": "四月版本",
//                "item1": null,
//                "appRefreshtime": "Thu Apr 14 15:49:42 CST 2016",
//                "remark": "",
//                "appState": 1,
//                "appVersion": "V2.1.5",
//                "appType": 2
//        },
//        {
//            "id": 5,
//                "item2": "",
//                "appUrl": "http://121.43.225.110:8080/base/softUp/S800_FPA_app20160628.zip",
//                "appName": "八月版本",
//                "item1": "",
//                "appRefreshtime": "Thu Apr 14 15:49:42 CST 2016",
//                "remark": "",
//                "appState": 1,
//                "appVersion": "V2.1.6",
//                "appType": 2
//        }
//        ],
//        "msg": "获取软件版本号"
    //    }
//    private List<FirewareVersion> parseHardwareVersion(String hv) {
//        List<FirewareVersion> firewareVersionList = new ArrayList<FirewareVersion>();
//        try {
//            JSONObject json = new JSONObject(hv);
//            int status = json.getInt("status");
//            if(1 != status){
//                return null;
//            }
//            JSONArray jsondataarray = json.getJSONArray("data");
//            if(jsondataarray.length() == 0){
//                return null;
//            }
//            for(int i=0;i<jsondataarray.length();i++){
//                JSONObject jsondata = jsondataarray.getJSONObject(i);
//                String appUrl = jsondata.getString("appUrl");
//                if(Util.isNotEmpty(appUrl)){
//                    FirewareVersion firewareVersion = new FirewareVersion();
//                    firewareVersion.setAppUrl(appUrl);
//                    firewareVersion.setAppName(jsondata.getString("appName"));
//                    firewareVersion.setAppVersion(jsondata.getString("appUrl").substring(jsondata.getString("appUrl").lastIndexOf("/") + 1));
//                    firewareVersionList.add(firewareVersion);
//                }
//            }
//            return firewareVersionList;
//        } catch (JSONException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
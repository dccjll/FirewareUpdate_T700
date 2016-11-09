package com.dsm.t700.update;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class DeviceListAdapter extends BaseAdapter{
	
	private Context context;
	private ViewHolder holder;

	public DeviceListAdapter(Context context){
		this.context = context;
	}

	public List<BluetoothDevice> getBluetoothDeviceList(){
		return DeviceListActivity.mBluetoothDeviceList;
	}

	@Override
	public int getCount() {
		return DeviceListActivity.mBluetoothDeviceList.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = LayoutInflater.from(context).inflate(R.layout.app_mydecice2_lsititem, null);
			holder = new ViewHolder();
			holder.deviceNameTextView = (TextView) convertView.findViewById(R.id.deviceNameTextView);
			holder.deviceMacTextView = (TextView) convertView.findViewById(R.id.deviceMacTextView);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		BluetoothDevice device = DeviceListActivity.mBluetoothDeviceList.get(position);

		holder.deviceNameTextView.setText(device.getName());

		holder.deviceMacTextView.setText(device.getAddress());
		return convertView;
	}
	
	class ViewHolder{
		TextView deviceNameTextView;
		TextView deviceMacTextView;
	}
}

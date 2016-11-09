package com.dsm.t700.update;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by dessmann on 16/6/30.
 */
public class FirewareListDialog extends Dialog implements AdapterView.OnItemClickListener {
    private OnItemClickListener _onItemClickListener = null;

    public interface OnItemClickListener {
        public void onItemClick(FirewareListDialog dialog, String url);
    }

    private FirewareListDialog(Context context, List<FirewareVersion> firewareVersionList,  OnItemClickListener listener) {
        super(context,R.style.CustomDialog);
        setContentView(R.layout.app_hardware);

        _onItemClickListener = listener;

        FirewareListAdapter firewareListAdapter = new FirewareListAdapter(context, firewareVersionList);
        ListView listView = (ListView)findViewById(R.id.listView);
        listView.setAdapter(firewareListAdapter);
        listView.setOnItemClickListener(this);
}

    public static void showDialog(Context context, List<FirewareVersion> firewareVersionList, OnItemClickListener listener){
        FirewareListDialog dialog = new FirewareListDialog(context, firewareVersionList, listener);
        dialog.show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FirewareVersion firewareVersion = (FirewareVersion) ((FirewareListAdapter)parent.getAdapter()).getItem(position);
        if(_onItemClickListener != null){
            _onItemClickListener.onItemClick(this, firewareVersion.getAppUrl());
        }
        this.dismiss();
    }

    private class FirewareListAdapter extends BaseAdapter {

        private Context context;
        private ViewHolder holder;
        private List<FirewareVersion> firewareVersionList;

        public FirewareListAdapter(Context context,List firewareVersionList){
            this.context = context;
            this.firewareVersionList = firewareVersionList;
        }

        @Override
        public int getCount() {
            return firewareVersionList.size();
        }

        @Override
        public Object getItem(int position) {
            return firewareVersionList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = LayoutInflater.from(context).inflate(R.layout.app_hardware_lsititem, null);
                holder = new ViewHolder();
                holder.firewareNameTextView = (TextView) convertView.findViewById(R.id.firewareNameTextView);
                holder.firewareVersionTextView = (TextView) convertView.findViewById(R.id.firewareVersionTextView);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }

            FirewareVersion firewareVersion = firewareVersionList.get(position);

            holder.firewareNameTextView.setText(firewareVersion.getAppName());

            holder.firewareVersionTextView.setText(firewareVersion.getAppVersion());

            return convertView;
        }

        class ViewHolder{
            TextView firewareNameTextView;
            TextView firewareVersionTextView;
        }
    }
}

package com.nuaa.larry.wifiindoorlocation.adapter;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nuaa.larry.wifiindoorlocation.R;
import com.nuaa.larry.wifiindoorlocation.common.Config;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by liang on 23/10/2016.
 */

public class CollectorListAdapter extends RecyclerView.Adapter {
    private Context mContext;
    private List<ScanResult> mWifiList;

    public CollectorListAdapter(Context context, List<ScanResult> wifiList) {
        this.mContext = context;
        this.mWifiList = wifiList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_scanner, null);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (!(viewHolder instanceof ViewHolder)) {
            return;
        }

        ViewHolder holder = (ViewHolder) viewHolder;

        ScanResult result = mWifiList.get(position);

        holder.tv_name.setText(result.SSID);
        holder.tv_mac.setText(result.BSSID);
        holder.tv_level.setText(result.level + "");
    }

    @Override
    public long getItemId(int i) {
        return mWifiList.get(i).hashCode();
    }

    @Override
    public int getItemCount() {
        return mWifiList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_name) TextView tv_name;
        @Bind(R.id.tv_mac) TextView tv_mac;
        @Bind(R.id.tv_level) TextView tv_level;

        public ViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}

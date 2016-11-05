package com.nuaa.larry.wifiindoorlocation.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liang on 22/10/2016.
 */

public class WifiScanner {
    private WifiManager wifiManager;// 声明管理对象

    private WifiInfo wifiInfo;// Wifi信息

    private List<ScanResult> scanResultList; // 扫描出来的网络连接列表

    private List<WifiConfiguration> wifiConfigList;// 网络配置列表

    private Handler mHandler;
    private Runnable mRunnable;

    public WifiScanner(Context context) {
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiInfo = wifiManager.getConnectionInfo();

        scanResultList = new ArrayList<>();
        wifiConfigList = new ArrayList<>();

        mHandler = null;
    }

    public boolean getWifiStatus() {
        return wifiManager.isWifiEnabled();
    }

    // 打开/关闭 wifi
    public boolean openWifi() {
        return !wifiManager.isWifiEnabled() && wifiManager.setWifiEnabled(true);
    }

    public boolean closeWifi() {
        return !wifiManager.isWifiEnabled() || wifiManager.setWifiEnabled(false);
    }

    public void initScanner(final AppCompatActivity activity, final WifiScannerCallback callback) {
        mRunnable = new Runnable() {
            @Override
            public void run() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        wifiManager.startScan();

                        scanResultList = wifiManager.getScanResults(); // 扫描返回结果列表

                        wifiConfigList = wifiManager.getConfiguredNetworks(); // 扫描配置列表

                        callback.success();

                        mHandler.postDelayed(mRunnable, Config.ScanInterval);
                    }
                });
            }
        };
    }

    // 循环扫描
    public void startScan() {
        mHandler = new Handler();
        mHandler.post(mRunnable);
    }

    // 停止扫描
    public void stopScan() {
        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
            mHandler = null;
        }
    }

    // 扫描一次
    public void scanOnce() {
        wifiManager.startScan();

        scanResultList = wifiManager.getScanResults(); // 扫描返回结果列表

        wifiConfigList = wifiManager.getConfiguredNetworks(); // 扫描配置列表
    }

    public List<ScanResult> getWifiList() {
        return scanResultList;
    }

    public List<WifiConfiguration> getWifiConfigList() {
        return wifiConfigList;
    }

    // 获取扫描列表
    public StringBuilder lookUpscan() {
        StringBuilder scanBuilder = new StringBuilder();

        for (int i = 0; i < scanResultList.size(); i++) {
            scanBuilder.append("编号：" + (i + 1));
            scanBuilder.append(scanResultList.get(i).toString());  //所有信息
            scanBuilder.append("\n");
        }

        return scanBuilder;
    }

    // 获取指定信号的强度
    public int getLevel(int NetId) {
        return scanResultList.get(NetId).level;
    }

    // 获取本机Mac地址
    public String getMac() {
        return (wifiInfo == null) ? "" : wifiInfo.getMacAddress();
    }

    public String getBSSID() {
        return (wifiInfo == null) ? null : wifiInfo.getBSSID();
    }

    public String getSSID() {
        return (wifiInfo == null) ? null : wifiInfo.getSSID();
    }

    // 返回当前连接的网络的ID
    public int getCurrentNetId() {
        return (wifiInfo == null) ? null : wifiInfo.getNetworkId();
    }

    // 返回所有信息
    public String getwifiInfo() {
        return (wifiInfo == null) ? null : wifiInfo.toString();
    }

    // 获取IP地址
    public int getIP() {
        return (wifiInfo == null) ? null : wifiInfo.getIpAddress();
    }

    // 添加一个连接
    public boolean addNetWordLink(WifiConfiguration config) {
        int NetId = wifiManager.addNetwork(config);
        return wifiManager.enableNetwork(NetId, true);
    }

    // 禁用一个链接
    public boolean disableNetWordLick(int NetId) {
        wifiManager.disableNetwork(NetId);
        return wifiManager.disconnect();
    }

    // 移除一个链接
    public boolean removeNetworkLink(int NetId) {
        return wifiManager.removeNetwork(NetId);
    }

    // 不显示SSID
    public void hiddenSSID(int NetId) {
        wifiConfigList.get(NetId).hiddenSSID = true;
    }

    // 显示SSID
    public void displaySSID(int NetId) {
        wifiConfigList.get(NetId).hiddenSSID = false;
    }

    public interface WifiScannerCallback {
        void success();
    }
}

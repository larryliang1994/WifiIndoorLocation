package com.nuaa.larry.wifiindoorlocation;

import android.app.Application;
import android.content.SharedPreferences;

import com.avos.avoscloud.AVOSCloud;
import com.nuaa.larry.wifiindoorlocation.common.Config;
import com.nuaa.larry.wifiindoorlocation.common.Constants;
import com.nuaa.larry.wifiindoorlocation.common.MyOkHttpUtil;
import com.nuaa.larry.wifiindoorlocation.common.WifiScanner;

/**
 * Created by liang on 22/10/2016.
 */

public class App extends Application {
    public static SharedPreferences sp;

    @Override
    public void onCreate() {
        super.onCreate();

        MyOkHttpUtil.init();

        AVOSCloud.initialize(this, "WKoWzWri30AHYbbfpM4qPAbC-gzGzoHsz", "8pPCNMcpYohc4tiHzwURQYB3");

        Config.Scanner = new WifiScanner(this);

        sp = getSharedPreferences(Constants.SP_NAME, MODE_PRIVATE);

        Config.GroupCountIndex = sp.getInt(Constants.SP_KEY_GROUP_COUNT, 3);
        Config.CollectIntervalIndex = sp.getInt(Constants.SP_KEY_COLLECT_INTERVAL, 0);
        Config.DependableProbabilityIndex = sp.getInt(Constants.SP_KEY_DEPENDABLE_RATE, 0);
    }
}

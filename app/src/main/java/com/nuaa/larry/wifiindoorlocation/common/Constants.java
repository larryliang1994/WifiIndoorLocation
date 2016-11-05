package com.nuaa.larry.wifiindoorlocation.common;

/**
 * Created by liang on 05/11/2016.
 */

public class Constants {
    public static final int[] GROUP_COUNT = {10, 20, 50, 100};
    public static final int[] COLLECT_INTERVAL = {100, 200, 500, 1000};
    public static final float[] DEPENDABLE_RATE = {0.6f, 0.7f, 0.8f, 0.9f};

    public static final String SP_NAME = "sp";
    public static final String SP_KEY_GROUP_COUNT = "group_count";
    public static final String SP_KEY_COLLECT_INTERVAL = "collect_interval";
    public static final String SP_KEY_DEPENDABLE_RATE = "dependable_rate";
}

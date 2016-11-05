package com.nuaa.larry.wifiindoorlocation.common;

import android.net.wifi.ScanResult;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by liang on 28/10/2016.
 */

public class Calculator {
    private List<List<ApInfo>> dataList;
    private List<String> dimensions;
    private List<List<ApInfo>> dependableProbabilityValues;
    private List<ApInfo> fingerprint;

    private Handler mHandler;
    private Runnable mRunnable;

    private Counter counter;

    public int validCount = 0;

    public Calculator() {
        mHandler = new Handler();

        counter = new Counter();

        dataList = new ArrayList<>();

        dimensions = new ArrayList<>();

        fingerprint = new ArrayList<>();

        dependableProbabilityValues = new ArrayList<>();
    }

    /**
     * 采集一次数据，包含 Config.GroupCount 个数据点
     * <p>
     * 每秒获取一次wifi列表，并将其保存到list中
     */
    public void collect(final CalculatorCollectCallback callback) {
        counter.init();

        mRunnable = new Runnable() {
            @Override
            public void run() {
                Config.Scanner.scanOnce();

                List<ApInfo> availableList = new ArrayList<>();
                for (ScanResult result : Config.Scanner.getWifiList()) {
                    if (Config.APName.equals(result.SSID)) {
                        availableList.add(new ApInfo(result.BSSID, result.level));
                    }
                }

                if (availableList.size() != 0) {
                    dataList.add(availableList);

                    counter.inc();

                    callback.collectProcess(counter.getCount());

                    if (counter.getCount() >= Constants.GROUP_COUNT[Config.GroupCountIndex]) {
                        mHandler.removeCallbacks(mRunnable);

                        callback.doneCollect();
                    } else {
                        mHandler.postDelayed(mRunnable, Constants.COLLECT_INTERVAL[Config.CollectIntervalIndex]);
                    }
                } else {
                    mHandler.removeCallbacks(mRunnable);

                    dataList.remove(dataList.size() - 1);

                    callback.collectFail();
                }
            }
        };

        mHandler.post(mRunnable);
    }

    /**
     * 处理dataList数据中的数据
     * <p>
     * 1、预处理，统一数据维度
     * 2、Config.PointCount组数据中，每组数据通过对数正态分布模型，得到一个可信指纹
     * 3、使用Fisher判别法，建立判别式，得到判别式中的参数列表
     */
    public void process() {
        dataList = preProcess();

        fingerprint = logarithmNormality();
    }

    // 对数模型正态分布模型
    private List<ApInfo> logarithmNormality() {
        List<ApInfo> fingerprint = new ArrayList<>();

        for (int dimension = 0; dimension < dimensions.size(); dimension++) {
            // 均值 3.6
            double average = 0;
            for (int groupIndex = 0; groupIndex < Constants.GROUP_COUNT[Config.GroupCountIndex]; groupIndex++) {
                double rss = dataList.get(groupIndex).get(dimension).getRss();
                average += Math.log1p(Math.abs(rss) - 1);
            }
            average /= Constants.GROUP_COUNT[Config.GroupCountIndex];

            // 标准方差 3.7
            double standardDeviation = 0;
            for (int groupIndex = 0; groupIndex < Constants.GROUP_COUNT[Config.GroupCountIndex]; groupIndex++) {
                double rss = dataList.get(groupIndex).get(dimension).getRss();
                standardDeviation += Math.pow(Math.log1p(Math.abs(rss) - 1) - average, 2);
            }
            standardDeviation /= Constants.GROUP_COUNT[Config.GroupCountIndex];
            standardDeviation = Math.sqrt(standardDeviation);

            // 筛选 3.8
            List<ApInfo> dependableProbabilityValue = new ArrayList<>();
            double under = Math.sqrt(2 * Math.PI * Math.pow(standardDeviation, 2));
            // 把原有公式的u换成e^u
            double left = - (1 / (Math.pow(Math.E, average) * under));
            double right = - (Constants.DEPENDABLE_RATE[Config.DependableProbabilityIndex] / (Math.pow(Math.E, average) * under));
            double rss;
            double exponent;
            double middle;

            for (int groupIndex = 0; groupIndex < Constants.GROUP_COUNT[Config.GroupCountIndex]; groupIndex++) {
                rss = dataList.get(groupIndex).get(dimension).getRss();

                exponent = Math.pow(Math.E,
                        - Math.pow(Math.log1p(Math.abs(rss) - 1) - average, 2)
                                / (2 * Math.pow(standardDeviation, 2)));
                middle = - exponent / (Math.abs(rss) * under);

                // 过滤
                if (left <= middle && middle <= right) {
                    dependableProbabilityValue.add(dataList.get(groupIndex).get(dimension));
                }
            }

            // 新均值 3.6
            double newAverage = 0;
            for (int dpvIndex = 0; dpvIndex < dependableProbabilityValue.size(); dpvIndex++) {
                rss = dependableProbabilityValue.get(dpvIndex).getRss();
                newAverage += Math.log1p(Math.abs(rss) - 1);
            }
            newAverage /= dependableProbabilityValue.size();

            // 处理 3.9
            double newRSS = -(Math.pow(Math.E, newAverage));

            // 保存
            fingerprint.add(new ApInfo(dimensions.get(dimension), newRSS));

            dependableProbabilityValues.add(dependableProbabilityValue);
        }

        Log.i("fingerprint", "-----------------fingerprint-----------------");
        String output = "";
        for (ApInfo apInfo : fingerprint) {
            output += apInfo.getRss() + "   ";
        }
        Log.i("fingerprint", output);

        return fingerprint;
    }

    // 统一数据维度
    private List<List<ApInfo>> preProcess() {
        List<List<ApInfo>> newDataList = new ArrayList<>();

        List<String> dimensions = getDimension();

        for (int groupIndex = 0; groupIndex < Constants.GROUP_COUNT[Config.GroupCountIndex]; groupIndex++) {
            List<ApInfo> newApInfos = new ArrayList<>();
            for (int dimensionIndex = 0; dimensionIndex < dimensions.size(); dimensionIndex++) {
                String macAddress = dimensions.get(dimensionIndex);
                double rss = -99;

                for (ApInfo apInfo : dataList.get(groupIndex)) {
                    if (apInfo.getMacAddress().equals(macAddress)) {
                        rss = apInfo.getRss();
                        break;
                    }
                }

                newApInfos.add(new ApInfo(macAddress, rss));
            }
            newDataList.add(newApInfos);
        }

        String output = "";

        for (List<ApInfo> apInfos : newDataList) {
            for (ApInfo apInfo : apInfos) {
                if (apInfo.getRss() != -99) {
                    validCount++;
                }
                output += apInfo.getRss() + "  ";
            }
            output += "\n";
        }
        output += "---------------------------------------------------------------\n";


        Log.i("info", output);

        return newDataList;
    }

    // 获取当前所有数据包含的维度
    private List<String> getDimension() {
        for (List<ApInfo> apInfos : dataList) {
            for (ApInfo apInfo : apInfos) {
                boolean hasThisDimension = false;

                for (String dimension : dimensions) {
                    if (dimension.equals(apInfo.getMacAddress())) {
                        hasThisDimension = true;
                        break;
                    }
                }

                if (!hasThisDimension) {
                    dimensions.add(apInfo.getMacAddress());
                }
            }
        }

        Log.i("info", "共 " + dimensions.size() + " 个维度");

        for (String dimension : dimensions) {
            Log.i("info", dimension);
        }

        Log.i("info", "-----------------------------------------------");

        return dimensions;
    }

    public void clear() {
        dataList = new ArrayList<>();
        dimensions = new ArrayList<>();
        fingerprint = new ArrayList<>();
        dependableProbabilityValues = new ArrayList<>();
        validCount = 0;
    }

    public List<List<ApInfo>> getDataList() {
        return dataList;
    }

    public List<String> getDimensions() {
        return dimensions;
    }

    public List<ApInfo> getFingerprint() {
        return fingerprint;
    }

    public List<List<ApInfo>> getDependableProbabilityValues() {
        return dependableProbabilityValues;
    }

    public interface CalculatorCollectCallback {
        void collectProcess(int process);

        void collectFail();

        void doneCollect();
    }

    public class Counter {
        private int count = 0;

        public synchronized void init() {
            count = 0;
        }

        public synchronized void inc() {
            count++;
        }

        public int getCount() {
            return count;
        }
    }
}

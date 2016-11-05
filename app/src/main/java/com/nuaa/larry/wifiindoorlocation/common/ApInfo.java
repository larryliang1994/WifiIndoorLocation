package com.nuaa.larry.wifiindoorlocation.common;

/**
 * Created by liang on 29/10/2016.
 */

public class ApInfo {
    private String macAddress;
    private double rss;

    public ApInfo(String macAddress, double rss) {
        this.macAddress = macAddress;
        this.rss = rss;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public double getRss() {
        return rss;
    }

    public void setRss(double rss) {
        this.rss = rss;
    }
}

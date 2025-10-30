package com.spark.h9patches;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.android.dx.stock.ProxyBuilder;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Wifi extends ServiceFacility {
    public Wifi(Context context) {
        super(context);
    }

    @Override
    public void onServiceStart() {
        boolean value = sharedPreferences.getBoolean(getString(R.string.pref_turn_on_wifi_spot), getResourcesBoolean(R.bool.pref_turn_on_wifi_spot));
        toggleWifiHotspot(value ? PatchesIntent.FLAG_STATE_ON : PatchesIntent.FLAG_STATE_OFF);
    }

    @Override
    public void onSharedPreferenceChanged(String key) {
        if (key.equals(getString(R.string.pref_turn_on_wifi_spot))) {
            boolean value = sharedPreferences.getBoolean(key, getResourcesBoolean(R.bool.pref_turn_on_wifi_spot));
            toggleWifiHotspot(value ? PatchesIntent.FLAG_STATE_ON : PatchesIntent.FLAG_STATE_OFF);
        }
    }

    public void toggleWifiHotspot(int state) {
        try {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (state == 0) {
//                WifiApUtils.closeWifiAP();
                stopTethering();
                wifiManager.setWifiEnabled(true);
            } else if (state == 1) {
                //获取wifi开关状态
                if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
                    //wifi打开状态则关闭
                    wifiManager.setWifiEnabled(false);
                }
//                WifiApUtils.openWifiAP();
                startTethering();
            }
        } catch (Exception e) {
            Log.e(TAG, "error open hotspot");
        }
    }

    private void startTethering() {
        changeChannel();
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            @SuppressLint("PrivateApi") Class classOnStartTetheringCallback = Class.forName("android.net.ConnectivityManager$OnStartTetheringCallback");
            Method startTethering = connectivityManager.getClass().getDeclaredMethod("startTethering", int.class, boolean.class, classOnStartTetheringCallback);
//            Object proxy = ProxyBuilder.forClass(classOnStartTetheringCallback).handler(new InvocationHandler() {
//                @Override
//                public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
//                    return null;
//                }
//            }).build();
//            startTethering.invoke(connectivityManager, 0, true, proxy);
        } catch (Exception e) {
            Log.e(TAG, "打开热点失败");
        }
    }

    private void stopTethering() {
        ConnectivityManager connectivityManager = ((ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE));
        try {
            Method stopTethering = connectivityManager.getClass().getDeclaredMethod("stopTethering", int.class);
            stopTethering.invoke(connectivityManager, 0);
        } catch (Exception e) {
            Log.e(TAG, "关闭热点失败");
            e.printStackTrace();
        }
    }

    private void changeChannel() {
        try {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            Method getWifiApConfigurationMethod = null;
            getWifiApConfigurationMethod = wifiManager.getClass().getMethod("getWifiApConfiguration");
            WifiConfiguration netConfig = (WifiConfiguration) getWifiApConfigurationMethod.invoke(wifiManager);
            Log.d(TAG, "Writing HotspotData" + "\nSSID:" + netConfig.SSID + "\nPassword:" + netConfig.preSharedKey + "\n");

            Field wcBand = WifiConfiguration.class.getField("apBand");
            int vb = wcBand.getInt(netConfig);
            Log.d(TAG, "Band was" + "val=" + vb);
            wcBand.setInt(netConfig, 2); // 2Ghz

            // For Channel change
            Field wcFreq = WifiConfiguration.class.getField("apChannel");
            int val = wcFreq.getInt(netConfig);
            Log.d(TAG, "Config was" + "val=" + val);
            wcFreq.setInt(netConfig, 11); // channel 11

            Method setWifiApConfigurationMethod = wifiManager.getClass().getMethod("setWifiApConfiguration", WifiConfiguration.class);
            setWifiApConfigurationMethod.invoke(wifiManager, netConfig);

            // For Saving Data
            wifiManager.saveConfiguration();
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }
}

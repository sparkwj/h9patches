package com.spark.h9patches;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.android.dx.stock.ProxyBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class WifiApUtils {
    private static final String TAG = "WifiApUtils";

    public static void openWifiAP(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//判断android系统版本,高于8.0
            startTethering();
        } else {
            createWiFiAP(createWifiInfo("MyCar", "12345678", 3, "ap"), true);
        }
    }

    public static void closeWifiAP(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//判断android系统版本,高于8.0
            stopTethering();
        } else {
            closeWifiHotPoint();
        }
    }

    private static WifiConfiguration createWifiInfo2(String ssid,
                                                     String password, int paramInt) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();

        config.SSID = ssid;

        if (paramInt == 1) // WIFICIPHER_NOPASS 不加密
        {
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
            return config;
        }
        if (paramInt == 2) // WIFICIPHER_WEP WEP加密
        {
            config.hiddenSSID = true;
            config.wepKeys[0] = password;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
            return config;
        }
        if (paramInt == 3) // WIFICIPHER_WPA wpa加密
        {
            config.preSharedKey = password;
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
            return config;
        }
        return null;// 返回null，创建热点失败
    }

    /**
     * android8.0以上开启手机热点
     */
    private static void startTethering() {
        ConnectivityManager connectivityManager = ((ConnectivityManager) Utils.getContext().getSystemService(Context.CONNECTIVITY_SERVICE));
        try {
            Class classOnStartTetheringCallback = Class.forName("android.net.ConnectivityManager$OnStartTetheringCallback");
            Method startTethering = connectivityManager.getClass().getDeclaredMethod("startTethering", int.class, boolean.class, classOnStartTetheringCallback);
            Object proxy = ProxyBuilder.forClass(classOnStartTetheringCallback).handler(new InvocationHandler() {
                @Override
                public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                    return null;
                }
            }).build();
            startTethering.invoke(connectivityManager, 0, false, proxy);
        } catch (Exception e) {
            Log.e(TAG,"打开热点失败");
            e.printStackTrace();
        }
    }

    /**
     * android8.0以上关闭手机热点
     */
    private static void stopTethering(){
        ConnectivityManager connectivityManager = ((ConnectivityManager) Utils.getContext().getSystemService(Context.CONNECTIVITY_SERVICE));
        try {
            Method stopTethering = connectivityManager.getClass().getDeclaredMethod("stopTethering", int.class);
            stopTethering.invoke(connectivityManager,0);
        } catch (Exception e) {
            Log.e(TAG,"关闭热点失败");
            e.printStackTrace();
        }
    }


    /**
     * 创建WiFi热点
     * @param config WiFi配置信息
     * @param paramBoolean true为开启WiFi热点，false为关闭
     * @return 返回开启成功状态，true为成功，false为失败
     */
    private static boolean createWiFiAP(WifiConfiguration config, boolean paramBoolean) {
        // 开启热点前，如果WiFi可用，先关闭WiFi
        WifiManager wifiManager = (WifiManager) Utils.getContext().getApplicationContext().getSystemService(
                Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
        }
        Log.i(TAG, "into startWifiAp（） 启动一个Wifi 热点！");
        boolean ret = false;
        try {
            Method method = wifiManager.getClass().getMethod("setWifiApEnabled",
                    WifiConfiguration.class, boolean.class);
            ret = (Boolean) method.invoke(wifiManager, config, paramBoolean);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            Log.d(TAG, "stratWifiAp() IllegalArgumentException e");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.d(TAG, "stratWifiAp() IllegalAccessException e");
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            Log.d(TAG, "stratWifiAp() InvocationTargetException e");
        } catch (SecurityException e) {
            e.printStackTrace();
            Log.d(TAG, "stratWifiAp() SecurityException e");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            Log.d(TAG, "stratWifiAp() NoSuchMethodException e");
        }
        Log.i(TAG, "out startWifiAp（） 启动一个Wifi 热点！");
        return ret;
    }


    /**
     * 创建WiFi热点
     * @param wifiManager WiFi管理器
     * @param config WiFi配置信息
     * @param paramBoolean true为开启WiFi热点，false为关闭
     * @return 返回开启成功状态，true为成功，false为失败
     */
    private static boolean createWiFiAP(WifiManager wifiManager, WifiConfiguration config, boolean paramBoolean,String methodName) {
        // 开启热点前，如果WiFi可用，先关闭WiFi
        if (wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
        }
        Log.i(TAG, "into startWifiAp（） 启动一个Wifi 热点！");
        boolean ret = false;
        try {
            Method method = wifiManager.getClass().getMethod("setWifiApEnabled",
                    WifiConfiguration.class, boolean.class);
            ret = (Boolean) method.invoke(wifiManager, config, paramBoolean);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            Log.d(TAG, "stratWifiAp() IllegalArgumentException e");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.d(TAG, "stratWifiAp() IllegalAccessException e");
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            Log.d(TAG, "stratWifiAp() InvocationTargetException e");
        } catch (SecurityException e) {
            e.printStackTrace();
            Log.d(TAG, "stratWifiAp() SecurityException e");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            Log.d(TAG, "stratWifiAp() NoSuchMethodException e");
        }
        Log.i(TAG, "out startWifiAp（） 启动一个Wifi 热点！");
        return ret;
    }

    /**
     * 配置WiFi或者热点信息
     *
     * @param ssid
     *            WiFi名称
     * @param password
     *            WiFi密码
     * @param paramInt
     *            WiFi加密方式 1为不加密，2为WEP加密，3为wpa加密
     * @param wifiType
     *            “wifi”为打开普通WiFi连接，“ap”为创建WiFi热点
     * @return
     */
    private static WifiConfiguration createWifiInfo(String ssid, String password, int paramInt, String wifiType) {
        WifiManager wifiManager = (WifiManager) Utils.getContext().getApplicationContext().getSystemService(
                Context.WIFI_SERVICE);
        WifiConfiguration Config1 = new WifiConfiguration();
        Config1.allowedAuthAlgorithms.clear();
        Config1.allowedGroupCiphers.clear();
        Config1.allowedKeyManagement.clear();
        Config1.allowedPairwiseCiphers.clear();
        Config1.allowedProtocols.clear();
        if ("wifi".equals(wifiType)) {
            Config1.SSID = ("\"" + ssid + "\"");
            WifiConfiguration Config2 = isExsits(wifiManager, ssid);
            if (Config2 != null) {
                if (wifiManager != null) {
                    wifiManager.removeNetwork(Config2.networkId);
                }
            }
            if (paramInt == 1) {
                Config1.wepKeys[0] = "";
                Config1.allowedKeyManagement.set(0);
                Config1.wepTxKeyIndex = 0;
                Config1.hiddenSSID=false;
                Log.i(TAG, "1");

                return Config1;
            } else if (paramInt == 2) {
                Config1.wepKeys[0] = ("\"" + password + "\"");
                Log.i(TAG, "2");
                Config1.hiddenSSID=false;

                return Config1;
            } else {
                Config1.preSharedKey = ("\"" + password + "\"");
                Config1.allowedAuthAlgorithms.set(0);
                Config1.allowedGroupCiphers.set(2);
                Config1.allowedKeyManagement.set(1);
                Config1.allowedPairwiseCiphers.set(1);
                Config1.allowedGroupCiphers.set(3);
                Config1.allowedPairwiseCiphers.set(2);
                Config1.hiddenSSID=false; //不隐藏设备

                Log.i(TAG, "3");

                return Config1;
            }
        } else {
            WifiConfiguration wifiApConfig = new WifiConfiguration();
            wifiApConfig.allowedAuthAlgorithms.clear();
            wifiApConfig.allowedGroupCiphers.clear();
            wifiApConfig.allowedKeyManagement.clear();
            wifiApConfig.allowedPairwiseCiphers.clear();
            wifiApConfig.allowedProtocols.clear();

            wifiApConfig.SSID = ssid;

            if (paramInt == 1)  { // WIFICIPHER_NOPASS 不加密
                wifiApConfig.wepKeys[0] = "";
                wifiApConfig.allowedKeyManagement
                        .set(WifiConfiguration.KeyMgmt.NONE);
                wifiApConfig.wepTxKeyIndex = 0;
                return wifiApConfig;
            }
            if (paramInt == 2){ // WIFICIPHER_WEP WEP加密
                wifiApConfig.hiddenSSID = true;
                wifiApConfig.wepKeys[0] = password;
                wifiApConfig.allowedAuthAlgorithms
                        .set(WifiConfiguration.AuthAlgorithm.SHARED);
                wifiApConfig.allowedGroupCiphers
                        .set(WifiConfiguration.GroupCipher.CCMP);
                wifiApConfig.allowedGroupCiphers
                        .set(WifiConfiguration.GroupCipher.TKIP);
                wifiApConfig.allowedGroupCiphers
                        .set(WifiConfiguration.GroupCipher.WEP40);
                wifiApConfig.allowedGroupCiphers
                        .set(WifiConfiguration.GroupCipher.WEP104);
                wifiApConfig.allowedKeyManagement
                        .set(WifiConfiguration.KeyMgmt.NONE);
                wifiApConfig.wepTxKeyIndex = 0;
                return wifiApConfig;
            }

            if (paramInt == 3) {// WIFICIPHER_WPA wpa加密
                wifiApConfig.preSharedKey = password;
                wifiApConfig.hiddenSSID =false;
                wifiApConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                wifiApConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

                Log.e(TAG,"是否小米:"+isMIUI());
                if(isMIUI()){
                    wifiApConfig.allowedKeyManagement.set(6); //WPA2_PSK 小米系统为6,其它一般为4
                }else {
                    wifiApConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                }


                wifiApConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                wifiApConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                wifiApConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                wifiApConfig.status = WifiConfiguration.Status.ENABLED;
                return wifiApConfig;
            }
        }
        return null;
    }


    private static boolean isMIUI() {
        try {
            String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
            String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
            String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";
            Properties prop = new Properties();
            prop.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
            return prop.getProperty(KEY_MIUI_VERSION_CODE, null) != null
                    || prop.getProperty(KEY_MIUI_VERSION_NAME, null) != null
                    || prop.getProperty(KEY_MIUI_INTERNAL_STORAGE, null) != null;
        } catch (IOException e) {
            return false;
        }
    }


    /**8.0以下
     * 关闭WiFi热点
     *            WiFi管理器
     * @return 返回关闭状态
     */
    private static boolean closeWifiHotPoint() {
        Log.i(TAG, "into closeWifiAp（） 关闭一个Wifi 热点！");
        boolean ret = false;
        WifiManager wifiManager = (WifiManager) Utils.getContext().getApplicationContext().getSystemService(
                Context.WIFI_SERVICE);
        if (isWifiApEnabled()) {
            try {
                Method method = wifiManager.getClass().getMethod(
                        "getWifiApConfiguration");
                method.setAccessible(true);
                WifiConfiguration config = (WifiConfiguration) method
                        .invoke(wifiManager);
                Method method2 = wifiManager.getClass().getMethod(
                        "setWifiApEnabled", WifiConfiguration.class,
                        boolean.class);
                ret = (Boolean) method2.invoke(wifiManager, config, false);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        Log.i(TAG, "out closeWifiAp（） 关闭一个Wifi 热点！");
        return ret;
    }

    /**
     * 检测WiFi热点是否可用
     *
     * @return 是否可用状态
     */
    public static boolean isWifiApEnabled() {
        WifiManager wifiManager = (WifiManager) Utils.getContext().getApplicationContext().getSystemService(
                Context.WIFI_SERVICE);
        try {
            Method method = wifiManager.getClass().getMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifiManager);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 通过反射，获取WiFi热点名称 SSID
     * @return
     */
    public static String getApSSID() {
        WifiManager wifiManager = (WifiManager) Utils.getContext().getApplicationContext().getSystemService(
                Context.WIFI_SERVICE);
        try {
            Method localMethod = wifiManager.getClass().getDeclaredMethod(
                    "getWifiApConfiguration", new Class[0]);
            if (localMethod == null)
                return null;
            Object localObject1 = localMethod
                    .invoke(wifiManager, new Object[0]);
            if (localObject1 == null)
                return null;
            WifiConfiguration localWifiConfiguration = (WifiConfiguration) localObject1;
            if (localWifiConfiguration.SSID != null)
                return localWifiConfiguration.SSID;
            Field localField1 = WifiConfiguration.class
                    .getDeclaredField("mWifiApProfile");
            if (localField1 == null)
                return null;
            localField1.setAccessible(true);
            Object localObject2 = localField1.get(localWifiConfiguration);
            localField1.setAccessible(false);
            if (localObject2 == null)
                return null;
            Field localField2 = localObject2.getClass()
                    .getDeclaredField("SSID");
            localField2.setAccessible(true);
            Object localObject3 = localField2.get(localObject2);
            if (localObject3 == null)
                return null;
            localField2.setAccessible(false);
            String str = (String) localObject3;
            return str;
        } catch (Exception localException) {
        }
        return null;
    }

    /**
     * 获取WiFi热点的状态
     *
     */
    private int getWifiApState() {
        WifiManager wifiManager = (WifiManager) Utils.getContext().getApplicationContext().getSystemService(
                Context.WIFI_SERVICE);
        try {
            int i = ((Integer) wifiManager.getClass()
                    .getMethod("getWifiApState", new Class[0])
                    .invoke(wifiManager, new Object[0])).intValue();
            return i;
        } catch (Exception localException) {
        }
        return 4; // 未知wifi网卡状态
    }



    /**
     * 判断选择的WiFi热点是否可以连接
     *
     * @param ssid
     *            WiFi热点名 SSID
     * @param wifiList
     *            附近的WiFi列表
     * @return true 可以连接 false 不可以连接（不在范围内）
     */
    public static boolean checkCoonectHotIsEnable(String ssid,
                                                  List<ScanResult> wifiList) {
        for (ScanResult result : wifiList) {
            if (result.SSID.equals(ssid)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查wifi列表中是否有以输入参数为名的wifi热点，如果存在，则在开始配置wifi网络之前将其移除，以避免ssid的重复
     *
     * @param wifiManager
     * @param
     * @return
     */
    private static WifiConfiguration isExsits(WifiManager wifiManager,
                                              String paramString) {
        @SuppressLint("MissingPermission") Iterator<WifiConfiguration> localIterator = wifiManager
                .getConfiguredNetworks().iterator();
        WifiConfiguration localWifiConfiguration;
        do {
            if (!localIterator.hasNext())
                return null;
            localWifiConfiguration = (WifiConfiguration) localIterator.next();
        } while (!localWifiConfiguration.SSID.equals("\"" + paramString + "\""));
        return localWifiConfiguration;
    }

    private static List<WifiConfiguration> getWifiConfigurations(
            WifiManager wifiManager) {
        @SuppressLint("MissingPermission") List<WifiConfiguration> existingConfigs = wifiManager
                .getConfiguredNetworks();
        return existingConfigs;
    }
}

//package com.spark.h9patches;
//
//import android.content.ContentResolver;
//import android.content.Context;
//import android.media.AudioManager;
//import android.net.wifi.WifiManager;
//
//public class Utils {
//    private static Context context = null;
//    private static ContentResolver contentResolver;
//    private static AudioManager.OnAudioFocusChangeListener focusChangeListener =
//            new AudioManager.OnAudioFocusChangeListener() {
//                public void onAudioFocusChange(int focusChange) {
//
//                }
//            };
//
//    public static Context getContext() {
//        return Utils.context;
//    }
//
//    public static void setContext(Context context) {
//        Utils.context = context;
//    }
//
//    public static void setContentResolver(ContentResolver contentResolver) {
//        Utils.contentResolver = contentResolver;
//    }
//
//    public static ContentResolver getContentResolver() {
//        return contentResolver;
//    }
//
//    public static void requestAudioFocus() {
//        AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
//        int result = am.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
//
//        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
//            //skip
//        }
//    }
//
//    public static void openWifiHotspot() {
//        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//        //获取wifi开关状态
//        int status=wifiManager.getWifiState();
//        if (status == WifiManager.WIFI_STATE_ENABLED ) {
//            //wifi打开状态则关闭
//            wifiManager.setWifiEnabled(false);
//        }
//        WifiApUtils.openWifiAP();
//    }
//}

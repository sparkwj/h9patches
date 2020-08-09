package com.spark.h9patches;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "BootBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "BootBroadcastReceiver 启动了");
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            context.startForegroundService(new Intent(context, H9PatchesService.class));
//            context.startForegroundService(new Intent(context, H9PatchesKeysService.class));
//        } else {
//            context.startService(new Intent(context, H9PatchesService.class));
//            context.startService(new Intent(context, H9PatchesKeysService.class));
//        }
    }
}

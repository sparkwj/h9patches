package com.spark.h9patches;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.input.InputManager;
import android.util.Log;
import android.widget.Toast;

public class BootBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "BootBroadcastReceiver";
    private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
    private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "BootBroadcastReceiver 启动了");
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
            String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
            Toast.makeText(context,"home key press down", Toast.LENGTH_LONG);
            if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
                // 短按Home键
                Log.i(TAG, "homekey pressed, h9patches");
            }
        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            context.startForegroundService(new Intent(context, H9PatchesService.class));
//            context.startForegroundService(new Intent(context, H9PatchesKeysService.class));
//        } else {
//            context.startService(new Intent(context, H9PatchesService.class));
//            context.startService(new Intent(context, H9PatchesKeysService.class));
//        }

//        IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
//        context.registerReceiver(this, homeFilter);
//        InputManager inputManager = InputManager.InputDeviceListener
    }
}

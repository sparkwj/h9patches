package com.spark.h9patches;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.input.InputManager;
import android.util.Log;
import android.widget.Toast;

public class BootBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "BootBroadcastReceiver";
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "H9 Patches BootBroadcastReceiver onReceive");
//        H9PatchesService theService = H9PatchesService.getInstance();
//        if (theService != null) {
//            theService.notifyOnStart();
//        }
    }
}

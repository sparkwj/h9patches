package com.spark.h9patches;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.BufferedReader;
import java.io.StringReader;

public class StartupScript extends ServiceFacility {
    BroadcastReceiver screenOnReceiver;
    public StartupScript(Context context) {
        super(context);
    }

    @Override
    public void onServiceStart() {
        runStartupScript();
        registerReceiver();
    }

    @Override
    public void onServiceStop() {
        if (screenOnReceiver != null) {
            getContext().unregisterReceiver(screenOnReceiver);
        }
    }

    private void registerReceiver() {
        if (screenOnReceiver != null) {
            return;
        }
        screenOnReceiver = new BroadcastReceiver() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onReceive(Context context, Intent intent) {
                StartupScript.this.runStartupScript();
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        getContext().registerReceiver(screenOnReceiver, filter);
    }

    public void runStartupScript() {
//        Toast.makeText(getApplicationContext(), "run startup script", Toast.LENGTH_LONG).show();
        //com.teslacoilsw.launcher/com.teslacoilsw.launcher.NovaLauncher
//        PackageManager pm = getPackageManager();
//        IntentFilter filter = new IntentFilter();
//        filter.addAction("android.intent.action.MAIN");
//        filter.addCategory("android.intent.category.HOME");
//        filter.addCategory("android.intent.category.DEFAULT");
//        ComponentName component = new ComponentName("com.teslacoilsw.launcher", "com.teslacoilsw.launcher.NovaLauncher");
//        ComponentName[] components = new ComponentName[] {new ComponentName("com.android.launcher", "com.android.launcher.Launcher"), component};
//        pm.clearPackagePreferredActivities("com.android.launcher");
//        pm.addPreferredActivity(filter, IntentFilter.MATCH_CATEGORY_EMPTY, components, component);
        Log.d(TAG, "runStartupScript");
        String script = sharedPreferences.getString(getString(R.string.pref_startup_script), getString(R.string.pref_value_startup_script));
        try {
            BufferedReader br = new BufferedReader(new StringReader(script));
            String command = "";
            Runtime runtime = Runtime.getRuntime();
            while ((command = br.readLine().trim()) != null) {
                if (command.isEmpty() || command.startsWith("#") || command.startsWith("//")) continue;
                try {
//                    Toast.makeText(getContext(), command, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, command);
                    runtime.exec(command);
                } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
//                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            //
        }
//        SystemProperties.set(DEFAULT_HOME, );
    }
}

package com.spark.h9patches;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.desay_svautomotive.svcarsettings.CarSettingsApplication;

public class H9PatchesApplication extends CarSettingsApplication {

    static H9PatchesApplication _instance;
    static SharedPreferences sharedPref;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onCreate() {
        super.onCreate();
        _instance = this;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, H9PatchesService.class));
            startForegroundService(new Intent(this, H9PatchesKeysService.class));
        } else {
            startService(new Intent(this, H9PatchesService.class));
            startService(new Intent(this, H9PatchesKeysService.class));
        }

        sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
    }

    public static H9PatchesApplication getInstance() {
        return _instance;
    }

    public static SharedPreferences getSharedPref() {
        return sharedPref;
    }
}

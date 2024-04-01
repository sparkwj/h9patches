package com.spark.h9patches;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.ServiceManager;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

import com.desay_svautomotive.svcarsettings.CarSettingsApplication;

public class H9PatchesApplication extends CarSettingsApplication {

    static H9PatchesApplication _instance;
    static SharedPreferences _sharedPref;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onCreate() {
        super.onCreate();
        if (_instance == null) {
            _instance = this;
            _sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(this, H9PatchesService.class));
                startForegroundService(new Intent(this, H9PatchesKeysService.class));
            } else {
                startService(new Intent(this, H9PatchesService.class));
                startService(new Intent(this, H9PatchesKeysService.class));
            }
//        HomeWatcher mHomeWatcher = new HomeWatcher(this);
//        mHomeWatcher.setOnHomePressedListener(new OnHomePressedListener() {
//            @Override
//            public void onHomePressed() {
//                // do something here...
//            }
//            @Override
//            public void onHomeLongPressed() {
//            }
//        });
//        mHomeWatcher.startWatch();
        }
    }

    public static H9PatchesApplication getInstance() {
        return _instance;
    }

    public static SharedPreferences getSharedPref() {
        return _sharedPref;
    }
}

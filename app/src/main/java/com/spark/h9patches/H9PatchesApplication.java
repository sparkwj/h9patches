package com.spark.h9patches;

import android.car.hardware.CarSensorManager;
import android.car.hardware.CarVendorExtensionManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.input.IInputManager;
import android.os.Build;
import android.os.IBinder;
import android.os.ServiceManager;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.car.Car;
import android.support.car.CarConnectionCallback;
import android.util.Log;

import com.desay_svautomotive.svcarsettings.CarSettingsApplication;
import com.desay_svautomotive.svcarsettings.manager.CarSettingsManager;

public class H9PatchesApplication extends CarSettingsApplication {
    static String TAG = "H9PatchesApplication";
    static H9PatchesApplication _instance;
    SharedPreferences mSharedPref;
    IInputManager mInputManager;
    CarSettingsManager mCarSettingsManager;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onCreate() {
        super.onCreate();
        if (_instance == null) {
            _instance = this;

            mSharedPref = getApplicationContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

            IBinder imBinder = ServiceManager.getService("input");
            mInputManager = IInputManager.Stub.asInterface(imBinder);
            mCarSettingsManager = new CarSettingsManager();

            Log.d(TAG, "startForegroundService H9PatchesService");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(this, H9PatchesService.class));
//                startForegroundService(new Intent(this, H9PatchesKeysService.class));
            }
//            Utils.setContext(this);

            enableAccessibilityService();

//            CarInputService mInputService = (CarInputService)getSystemService(Context.INPUT_SERVICE);
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

    private void enableAccessibilityService() {
        Settings.Secure.putString(getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, getPackageName() + "/" + H9AccessibilityService.class.getName());
        Settings.Secure.putString(getContentResolver(),
                Settings.Secure.ACCESSIBILITY_ENABLED, "1");
    }

    public static H9PatchesApplication getInstance() {
        return _instance;
    }

    public SharedPreferences getSharedPreferences() {
        return mSharedPref;
    }

    public IInputManager getInputManager() {
        return mInputManager;
    }

    public CarSettingsManager getCarSettingsManager() {
        return mCarSettingsManager;
    }

}

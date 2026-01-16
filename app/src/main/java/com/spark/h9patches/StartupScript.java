package com.spark.h9patches;

import android.car.Car;
import android.car.hardware.CarPropertyValue;
import android.car.hardware.CarSensorManager;
import android.car.hardware.CarVendorExtensionManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.dsp.V1_0.IDspHwDevice;
import android.hardware.rvc.V1_0.IRvc;
import android.hardware.rvc.V1_0.IRvcStatusListener;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.IPowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.desay_svautomotive.svcarsettings.manager.CarSettingsManager;
import com.dsv.personalsettings.Constants;
import com.dsv.personalsettings.SettingsAgent;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Arrays;

public class StartupScript extends ServiceFacility {
    BroadcastReceiver screenOnReceiver;
    Car mCar;
    CarVendorExtensionManager mCarVendorManager;
    public StartupScript(Context context) {
        super(context);
    }
    private static final String ACTION_AVM_DEBUG = "dsv.android.avm.DEBUG";
    private static final String ACTION_RVC_DEBUG = "dsv.android.rvc.DEBUG";
    private static final String CLASSIC_BLUE = "00100000";
    public static IRvc mRvc;
    private class HalRvcStatusListener extends IRvcStatusListener.Stub {
        @Override // android.hardware.rvc.V1_0.IRvcStatusListener
        public void onRvcStatusChange(int i) {
            if (i == 1) {
                setAutoBrightness();
            }
//            Log.d(TAG, "receive rvc status: " + i);
        }
    }


    @Override
    public void onServiceStart() {
        initCar();
        runStartupScript();
        registerReceiver();
    }

    @Override
    public void onServiceStop() {
        if (screenOnReceiver != null) {
            getContext().unregisterReceiver(screenOnReceiver);
        }
        if (mCar != null && mCar.isConnected()) {
            mCar.disconnect();
        }
        if (mCarConnection != null) {
            getContext().unbindService(mCarConnection);
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
        try {
            IDspHwDevice dspHwDevice = IDspHwDevice.getService();
            SettingsAgent settingsAgent = new SettingsAgent(getContext());
            dspHwDevice.setBexStatus(0);
            Settings.Global.putInt(getContext().getContentResolver(), "sv_sound_quality", 0);
            dspHwDevice.setSurroundStatus(1);
            settingsAgent.setLocalSet(Constants.SUPPORT_SETTINGS[15], "1");
            Settings.Global.putInt(getContext().getContentResolver(), "sv_surround", 1);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        if (mRvc != null) {
            try {
                mRvc.setRvcStatusListener(new HalRvcStatusListener());
            } catch (Exception e) {
                Log.e(TAG, "set rvc listener failed", e);
            }
        }

        String v2rayngPackageName = "com.v2ray.ang"; // Replace with the other app's package name
        String v2rayngServiceClassName = "com.v2ray.ang.service.V2RayVpnService"; // Replace with the service's full class name
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(v2rayngPackageName, v2rayngServiceClassName));
//        intent.putExtra("key_data", "some value");
        try {
            getContext().startService(intent);
        } catch (Exception e) {
            Log.e(TAG, "start v2ray service failed", e);
        }

//        String v2ray_start_cmd = "am start-foreground-service com.v2ray.ang/com.v2ray.ang.service.V2RayVpnService";
        String script = sharedPreferences.getString(getString(R.string.pref_startup_script), getString(R.string.pref_value_startup_script));
        try {
            Runtime runtime = Runtime.getRuntime();
//            runtime.exec(v2ray_start_cmd);
//            new Handler().postDelayed(() -> {
//                try {
//                    runtime.exec(v2ray_start_cmd);
//                } catch (Exception e){}
//            }, 10);
//            new Handler().postDelayed(() -> {
//                try {
//                    runtime.exec(v2ray_start_cmd);
//                } catch (Exception e){}
//            }, 20);
//            new Handler().postDelayed(() -> {
//                try {
//                    runtime.exec(v2ray_start_cmd);
//                } catch (Exception e){}
//            }, 30);
            BufferedReader br = new BufferedReader(new StringReader(script));
            String command = "";
            while ((command = br.readLine()) != null) {
                command = command.trim();
                if (command.isEmpty() || command.startsWith("#") || command.startsWith(";") || command.startsWith("//")) continue;
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
            Log.d(TAG, e.getMessage());
        }
//        SystemProperties.set(DEFAULT_HOME, );
    }


//    private final CarVendorExtensionManager.CarVendorExtensionCallback carVendorExtensionCallback = new CarVendorExtensionManager.CarVendorExtensionCallback() {
//        public void onChangeEvent(CarPropertyValue val) {
//            Log.d(TAG, "XXXXXXXXX:" + val.getPropertyId());
//            if (val.getPropertyId() == 557859079) {
//                Object[] objects1 = (Object[]) val.getValue();
//                Log.d(TAG, "AAABBBobjects:" + Arrays.toString(objects1));
//                int type1 = Byte.parseByte(objects1[0].toString());
//            }
//        }
//        public void onErrorEvent(int var1, int var2) {
//        }
//    };
    ServiceConnection mCarConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            try {
                mCarVendorManager = (CarVendorExtensionManager) mCar.getCarManager("vendor_extension");
                StartupScript.this.mCarVendorManager.setProperty(Integer[].class, 557859856, 0, new Integer[]{29523, 4, 1, 1});
//                Object[] objects = mCarVendorManager.getProperty(Object[].class, 557859079, 0);
//                objects = mCarVendorManager.getProperty(Object[].class, 557859856, 0);
//                mCarVendorManager.registerCallback(carVendorExtensionCallback);
                setAutoBrightness();
            } catch (Exception ignored) {
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    private void setAutoBrightness() {
        try {
            IPowerManager mPower = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
            mCarVendorManager.setProperty(Integer[].class, 557859856, 0, new Integer[]{1, 0});
            mPower.setTemporaryScreenBrightnessSettingOverride(7);
            mCarVendorManager.setProperty(Integer[].class, 557859856, 0, new Integer[]{1, 1});
            mPower.setTemporaryScreenBrightnessSettingOverride(1);
            mCarVendorManager.setProperty(Integer[].class, 557859856, 0, new Integer[]{0, 0});
        } catch (Exception e) {
            Log.e(TAG, "set auto brightness failed", e);
        }
    }
    private void initCar() {
        if (mCar != null) {
            return;
        }
        mCar = Car.createCar(getContext(), mCarConnection);
        mCar.connect();

        try {
            mRvc = IRvc.getService();
        } catch (Exception e) {
            Log.e(TAG, "Can not get IRvc service", e);
        }
    }
}

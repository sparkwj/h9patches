package com.spark.h9patches;

import android.car.Car;
import android.car.CarNotConnectedException;
import android.car.hardware.CarPropertyValue;
import android.car.hardware.CarSensorManager;
import android.car.hardware.CarVendorExtensionManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.desay_svautomotive.svcarsettings.manager.CarSettingsManager;

public class LaneKeepAssist extends ServiceFacility {
    static long lastPressTime = 0;
    Car mCar;
    CarSettingsManager mCarSettingsManager;
    CarSensorManager mCarSensorManager;
    CarVendorExtensionManager mCarVendorManager;
    boolean enableLaneKeepAssistToggle = false;

    public LaneKeepAssist(Context context) {
        super(context);
    }

    @Override
    public void onServiceStart() {
        enableLaneKeepAssistToggle = sharedPreferences.getBoolean(getString(R.string.pref_lane_assist), getResourcesBoolean(R.bool.pref_lane_assist));
        mCarSettingsManager = getApplication().getCarSettingsManager();
        initCar();
    }

    @Override
    public void onServiceStop() {
        if (mCar != null && mCar.isConnected()) {
            mCar.disconnect();
        }
        if (mCarConnection != null) {
            getContext().unbindService(mCarConnection);
        }
    }

    @Override
    public void onSharedPreferenceChanged(String key) {
        if (key.equals(getString(R.string.pref_lane_assist))) {
            enableLaneKeepAssistToggle = sharedPreferences.getBoolean(getString(R.string.pref_lane_assist), getResourcesBoolean(R.bool.pref_lane_assist));
        }
    }

    private final CarVendorExtensionManager.CarVendorExtensionCallback carVendorExtensionCallback = new CarVendorExtensionManager.CarVendorExtensionCallback() {
        public void onChangeEvent(CarPropertyValue var1) {
            explainMessage(var1);
        }
        public void onErrorEvent(int var1, int var2) {
        }
    };
    protected void explainMessage(CarPropertyValue carPropertyValue) {
        Object[] params = (Object[]) carPropertyValue.getValue();
        if (params == null || params.length == 0) {
//            Log.e(TAG, "onChangeEvent: carPropertyValue - " + carPropertyValue.toString());
//            Log.e(TAG, "onChangeEvent: Null value SPI message!!!");
            return;
        }
        switch (carPropertyValue.getPropertyId()) {
            case 557903872:
                if (((Integer) params[0]).intValue() == 29523) {
                    int i = 1;
                    while (i < params.length) {
                        int i2 = i + 1;
                        int functionID = ((Integer) params[i]).intValue();
                        int i3 = i2 + 1;
                        int functionCommand = ((Integer) params[i2]).intValue();
                        int i4 = i3 + 1;
                        int dataLen = ((Integer) params[i3]).intValue();
                        int[] data = new int[dataLen];
                        for (int j = 0; j < dataLen; j++) {
                            data[j] = ((Integer) params[j + i4]).intValue();
                        }

                        if (functionID == 10) {
                            doAnalysis(functionCommand, data);
                        } else if (functionID == 254) {
                            doAnalysis(functionCommand, data);
                        } else if (functionID == 255) {
                            //H9PatchesCarSensorService.this.configAnalysis(functionCommand, data);
                        }
                        i = i4 + dataLen;
                    }
//                    Log.d(H9PatchesKeysService.this.TAG, "functionID:" + params[1]);
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void doAnalysis(int functionCommand, int[] data) {
//        Log.d(TAG, "doAnalysis functionCommand: " + functionCommand);
        switch (functionCommand) {
            case 31://车辆设置键
//                Log.d(TAG, "data0:" + data[0]);
                if (data[0] == 1) {
                    if (System.currentTimeMillis() - lastPressTime < 1500) {
//                        Log.d(TAG, "检测到双击，执行切换车道保持辅助");
                        if (enableLaneKeepAssistToggle) {
                            toggleLaneKeepAssistSystem(2);
                        }
//                        Intent pi = new Intent(H9PatchesKeysService.this, H9PatchesService.class);
//                        pi.setAction(com.spark.h9patches.Intent.ACTION_TOGGLE_LANE_KEEP_ASSIST_SYSTEM);
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                            startForegroundService(pi);
//                        } else {
//                            startService(pi);
//                        }
//                        Intent intent = new Intent(H9PatchesService.ACTION_TOGGLE_LANE_KEEP_ASSIST_SYSTEM);
//                        sendBroadcast(intent);
                    }
                    lastPressTime = System.currentTimeMillis();
//                    Log.d(TAG, "lastPressTime:" + lastPressTime);
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void toggleLaneKeepAssistSystem(int state) {
        int accState = getIgnitionState();
//        Log.d(TAG, "toggleLaneKeepAssistSystemValuea cc状态: " + accState);
        if (accState != 4) {
//            Log.d(TAG, "toggleLaneKeepAssistSystemValue acc状态不满足: " + accState);
            return;
        }
        int curState = mCarSettingsManager.getLaneKeepAssistSystemValue();
        if (1 == curState && 0 == state) {
            mCarSettingsManager.setLaneKeepAssistSystemValue(0);
//            Log.d(TAG, "关闭车道保持辅助");
        } else if (0 == curState && 1 == state) {
            mCarSettingsManager.setLaneKeepAssistSystemValue(1);
//            Log.d(TAG, "打开车道保持辅助");
        } else if (2 == state) {
            mCarSettingsManager.setLaneKeepAssistSystemValue(curState == 1 ? 0 : 1);
//            Log.d(TAG, "切换车道保持辅助为: " + (curState == 1 ? 0 : 1));
        }
    }


    public int getIgnitionState() {
        try {
            if (mCarSensorManager != null) {
                return mCarSensorManager.getLatestSensorEvent(22).intValues[0];
            }
        } catch (CarNotConnectedException | NullPointerException e) {
        }
        return 2;
    }


    ServiceConnection mCarConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            try {
                mCarSensorManager = (CarSensorManager) mCar.getCarManager(Car.SENSOR_SERVICE);
                mCarVendorManager = (CarVendorExtensionManager) mCar.getCarManager("vendor_extension");
                mCarVendorManager.registerCallback(carVendorExtensionCallback);
            } catch (Exception ignored) {
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    private void initCar() {
        if (mCar != null) {
            return;
        }
        mCar = Car.createCar(getContext(), mCarConnection);
        mCar.connect();
    }

}

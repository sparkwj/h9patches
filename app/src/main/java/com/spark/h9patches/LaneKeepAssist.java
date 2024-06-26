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
import android.os.RemoteException;
import android.util.Log;

import com.desay_svautomotive.caninfoservice.OnCarAccStateListener;
import com.desay_svautomotive.svcarsettings.manager.CarSettingsManager;

import java.util.Timer;
import java.util.TimerTask;

public class LaneKeepAssist extends ServiceFacility {
    static long lastPressTime = 0;
    Car mCar;
    CarSettingsManager mCarSettingsManager;
    CarSensorManager mCarSensorManager;
    com.desay_svautomotive.carlibs.hardware.CarSensorManager mCarSensorManager2;
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
        StringBuilder sp = new StringBuilder();
        int l =0;
        while (l < params.length) {
            sp.append(" ").append(((Integer) params[l]).intValue());
            l++;
        }
//        Log.d(TAG,"carPropertyValue.getPropertyId(): " + carPropertyValue.getPropertyId()
//                        + "  value: " + sp);

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
                            _toggleLaneKeepAssistSystem(2);
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
        if (enableLaneKeepAssistToggle) {
            _toggleLaneKeepAssistSystem(state);
        }
    }

    Timer laneKeepTimer;
    public void _toggleLaneKeepAssistSystem(int state) {
        stopLaneKeepTimerIfNeeds();
        int accState = getIgnitionState();
//        Log.d(TAG, "toggleLaneKeepAssistSystemValuea cc状态: " + accState);
        if (accState != 4) {
//            Log.d(TAG, "toggleLaneKeepAssistSystemValue acc状态不满足: " + accState);
            return;
        }
        int curState = mCarSettingsManager.getLaneKeepAssistSystemValue();
        if (2 == state) {
            state = curState == 1 ? 0 : 1;
        }
        if (1 == curState && 0 == state) {
            mCarSettingsManager.setLaneKeepAssistSystemValue(0);
//            Log.d(TAG, "关闭车道保持辅助");
//        } else if (0 == curState && 1 == state) {
//            mCarSettingsManager.setLaneKeepAssistSystemValue(1);
//            Log.d(TAG, "打开车道保持辅助");
        } else if (1 == state) {
            mCarSettingsManager.setLaneKeepAssistSystemValue(1);
//            startLaneKeepTimer();
        }
    }

    private void startLaneKeepTimer() {
        stopLaneKeepTimerIfNeeds();
        laneKeepTimer = new Timer();
        int period = 2 * 1000;
        laneKeepTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
//                        int direction = (int) Math.round(Math.random());
                    int direction = 1;
                    LaneKeepAssist.this.mCarVendorManager.setGlobalProperty(Integer[].class, 557903872, new Integer[]{29523, 4, 1, 1, direction});
                    LaneKeepAssist.this.mCarVendorManager.setGlobalProperty(Integer[].class, 557903872, new Integer[]{29523, 4, 0, 2, 0, 11});
                    LaneKeepAssist.this.mCarVendorManager.setGlobalProperty(Integer[].class, 557903872, new Integer[]{29523, 4, 1, 1, 0});
                    LaneKeepAssist.this.mCarVendorManager.setGlobalProperty(Integer[].class, 557903872, new Integer[]{29523, 4, 2, 2, 0, 0});
                    LaneKeepAssist.this.mCarVendorManager.setGlobalProperty(Integer[].class, 557903872, new Integer[]{29523, 4, 3, 2, 0, 0});
//                    Log.d(TAG, "SENDCAN");
                } catch (CarNotConnectedException e) {
                    //
                }
            }
        }, 0, period);
    }

    private void stopLaneKeepTimerIfNeeds() {
        if (laneKeepTimer != null) {
            laneKeepTimer.cancel();
            laneKeepTimer = null;
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

    OnCarAccStateListener.Stub accStateListener = new OnCarAccStateListener.Stub() {
        @Override
        public void onAccStateCallback(int i) throws RemoteException {
            Log.d(TAG, "onAccStateCallback: " + i);
        }
    };

    ServiceConnection mCarConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            try {
                mCarSensorManager = (CarSensorManager) mCar.getCarManager(Car.SENSOR_SERVICE);
                mCarSensorManager2 = (com.desay_svautomotive.carlibs.hardware.CarSensorManager)H9PatchesApplication.getInstance().getCarPropertyManager(2);
                mCarVendorManager = (CarVendorExtensionManager) mCar.getCarManager("vendor_extension");
                mCarSensorManager2.registerCarAccStateListener(accStateListener);
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

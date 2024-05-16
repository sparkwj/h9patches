package com.spark.h9patches;

import android.car.Car;
import android.car.CarNotConnectedException;
import android.car.hardware.CarSensorEvent;
import android.car.hardware.CarSensorManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.desay_svautomotive.caninfoservice.OnCarSensorListener;
import com.desay_svautomotive.carlibs.hardware.VehicleDoor;
import com.desay_svautomotive.svcarsettings.manager.CarSettingsManager;

public class PreCollisionSystem extends ServiceFacility {
    int ignitionState = 0;
    boolean isCarSensorReady = false;
    Car mCar;
    CarSettingsManager mCarSettingsManager;
    CarSensorManager mCarSensorManager;

    public PreCollisionSystem(Context context) {
        super(context);
        mCarSettingsManager = getApplication().getCarSettingsManager();
    }

    @Override
    public void onServiceStart() {
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

    private void checkPrefCollisionSystem() {
        Log.d(TAG, "checkPrefCollisionSystem");
        boolean value = sharedPreferences.getBoolean(getString(R.string.pref_turn_off_aeb), getResourcesBoolean(R.bool.pref_turn_off_aeb));
        int state = value ? PatchesIntent.FLAG_STATE_OFF : PatchesIntent.FLAG_STATE_ON;
        toggleCollisionSystem(state);
    }

    @Override
    public void onSharedPreferenceChanged(String key) {
        Log.d(TAG, "onSharedPreferenceChanged: ");
        if (key.equals(getString(R.string.pref_turn_off_aeb))) {
            checkPrefCollisionSystem();
        }
    }

    public void toggleCollisionSystem(int state) {
        if (mCarSensorManager == null) {
            Log.d(TAG, "CarSensorManager为空");
            return;
        }
        int accState = 2;
        try {
            accState = mCarSensorManager.getLatestSensorEvent(22).intValues[0];
        }
        catch (CarNotConnectedException | NullPointerException e) {
        }
        if (accState != 4) {
            Log.d(TAG, "_togglePreCollisionSystem acc状态不满足: " + accState);
//            return;
        }
        Log.d(TAG, "_togglePreCollisionSystem state: " + state);
        int curState = mCarSettingsManager.getPreCollisionSystemValue(VehicleDoor.DOOR_HOOD);
        if (1 == curState && 0 == state) {
            mCarSettingsManager.setPreCollisionSystemValue(VehicleDoor.DOOR_HOOD, state);
            Log.d(TAG, "关闭碰撞设置");
        } else if (0 == curState && 1 == state) {
            mCarSettingsManager.setPreCollisionSystemValue(VehicleDoor.DOOR_HOOD, state);
            Log.d(TAG, "打开碰撞设置");
        }
    }

    public OnCarSensorListener.Stub carSensorListener = new OnCarSensorListener.Stub() {
        public void onChangeEvent(int id, int zone, int value) {
            if (id == 10) {
                Log.d(TAG, "发动机状态改变:10");
                PreCollisionSystem.this.checkPrefCollisionSystem();
            }
        }
    };

    CarSensorManager.OnSensorChangedListener ignitionStateListener = new CarSensorManager.OnSensorChangedListener() {
        public void onSensorChanged(CarSensorEvent carSensorEvent) {
            Log.d(TAG, "ignitionStateListener:" + carSensorEvent.intValues[0]);
            int state = carSensorEvent.intValues[0];
            if (PreCollisionSystem.this.ignitionState == 5 && state == 4) {
//                PreCollisionSystem.this.checkRadioPlay();
                PreCollisionSystem.this.checkPrefCollisionSystem();
            } else if (PreCollisionSystem.this.ignitionState == 2 && state == 3) {
//                PreCollisionSystem.this.checkRadioPlay();
            }
            PreCollisionSystem.this.ignitionState = state;
        }
    };

    ServiceConnection mCarConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            try {
                Log.d(TAG, "car connected!");
                mCarSensorManager = (CarSensorManager) mCar.getCarManager(Car.SENSOR_SERVICE);
                mCarSensorManager.registerListener(ignitionStateListener, 22, 0);
                mCarSettingsManager.registerCarSensorCallback(carSensorListener);
                checkPrefCollisionSystem();
                Log.d(TAG, "car con!!!");
            } catch (Exception e) {
                Log.d(TAG, e.getLocalizedMessage());
            }
        };

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

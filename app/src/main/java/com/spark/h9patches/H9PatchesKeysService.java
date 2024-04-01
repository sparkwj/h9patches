package com.spark.h9patches;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.car.CarNotConnectedException;
import android.car.hardware.CarPropertyValue;
import android.car.hardware.CarVendorExtensionManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.car.Car;
import android.support.car.CarConnectionCallback;
import android.util.Log;

public class H9PatchesKeysService extends Service {
    static long lastPressTime = 0;
    private static final int ONGOING_NOTIFICATION_ID = 2;
    private final String TAG = H9PatchesKeysService.class.getSimpleName();
    private Car mCarApiClient;
    private H9PatchesService h9PatchesService;
    private CarVendorExtensionManager mCarVendorManager;
    private CarVendorExtensionManager.CarVendorExtensionCallback callback = new CarVendorExtensionManager.CarVendorExtensionCallback() {
        public void onChangeEvent(CarPropertyValue var1) {
            H9PatchesKeysService.this.explainMessage(var1);
        }

        public void onErrorEvent(int var1, int var2) {
        }
    };
    ServiceConnection mServerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            h9PatchesService = ((H9PatchesService.H9PatchesServiceBinder) iBinder).getPatchesService();
            isServiceBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isServiceBound = false;
        }
    };
    private boolean isServiceBound;

    public H9PatchesKeysService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(this.TAG, "H9PatchesKeysService onCreate: ");
        if (Build.VERSION.SDK_INT >= 26) {
            setForegroundService();
        }
        this.initCar();
        android.content.Intent intent = new android.content.Intent(this, H9PatchesService.class);
        bindService(intent, mServerConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    private void initCar() {
        this.mCarApiClient = Car.createCar(this, new CarConnectionCallback() {
            public void onConnected(Car var1) {
                Log.d(H9PatchesKeysService.this.TAG, "CarConnectionCallback onConnected");

                Object var5;
                try {
                    H9PatchesKeysService.this.mCarVendorManager = (CarVendorExtensionManager)var1.getCarManager("vendor_extension");
                    H9PatchesKeysService.this.mCarVendorManager.registerCallback(H9PatchesKeysService.this.callback);
                    return;
                } catch (CarNotConnectedException var3) {
                    var5 = var3;
                } catch (android.support.car.CarNotConnectedException var4) {
                    var5 = var4;
                }

                ((Exception)var5).printStackTrace();
            }

            public void onDisconnected(Car var1) {
                Log.d(H9PatchesKeysService.this.TAG, "CarConnectionCallback onDisconnected");
            }
        });
        this.mCarApiClient.connect();
    }


    protected void explainMessage(CarPropertyValue carPropertyValue) {
        Object[] params = (Object[]) carPropertyValue.getValue();
        if (params == null || params.length == 0) {
            Log.e(H9PatchesKeysService.this.TAG, "onChangeEvent: carPropertyValue - " + carPropertyValue.toString());
            Log.e(H9PatchesKeysService.this.TAG, "onChangeEvent: Null value SPI message!!!");
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
                            H9PatchesKeysService.this.doAnalysis(functionCommand, data);
                        } else if (functionID == 254) {
                            H9PatchesKeysService.this.doAnalysis(functionCommand, data);
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
//        Log.i(TAG, "doAnalysis functionCommand: " + functionCommand);
        switch (functionCommand) {
            case 31://车辆设置键
                Log.d(TAG, "data0:" + data[0]);
                if (data[0] == 1) {
                    if (System.currentTimeMillis() - lastPressTime < 1500) {
                        Log.i(TAG, "检测到双击，执行切换车道保持辅助");
                        h9PatchesService.toggleLaneKeepAssistSystem(2);
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


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void  setForegroundService() {
        NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel serviceChannel = new NotificationChannel(
                getString(R.string.key_notification_channel),
                getString(R.string.text_notification_title),
                NotificationManager.IMPORTANCE_HIGH);
        notifManager.createNotificationChannel(serviceChannel);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification =
                new Notification.Builder(this, getString(R.string.key_notification_channel))
                        .setContentTitle(getString(R.string.text_notification_title))
                        .setContentText(getString(R.string.text_notification_message))
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentIntent(pendingIntent)
                        .setTicker(getString(R.string.text_ticker_tips))
                        .build();

        startForeground(ONGOING_NOTIFICATION_ID, notification);
    }
}

package com.spark.h9patches;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.car.Car;
import android.car.CarNotConnectedException;
import android.car.hardware.CarSensorEvent;
import android.car.hardware.CarSensorManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.desay_svautomotive.caninfoservice.OnCarSensorListener;
import com.desay_svautomotive.carlibs.hardware.VehicleDoor;
import com.desay_svautomotive.svcarsettings.CarSettingsApplication;
import com.desay_svautomotive.svcarsettings.manager.CarSettingsManager;

public class H9PatchesService extends Service {

    private static final String TAG = "H9PatchesService";
    public static final String ACTION_DISABLE_PRE_COLLISION_SYSTEM = "h9patches.intent.action.DISABLE_PRE_COLLISION_SYSTEM";
    public static final String ACTION_ENABLE_PRE_COLLISION_SYSTEM = "h9patches.intent.action.ENABLE_COLLISION_SYSTEM";
    public static final String ACTION_ENABLE_LANE_KEEP_ASSIST_SYSTEM = "h9patches.intent.action.ENABLE_LANE_KEEP_ASSIST_SYSTEM";
    public static final String ACTION_DISABLE_LANE_KEEP_ASSIST_SYSTEM = "h9patches.intent.action.DISABLE_LANE_KEEP_ASSIST_SYSTEM";
    public static final String ACTION_TOGGLE_LANE_KEEP_ASSIST_SYSTEM = "h9patches.intent.action.TOGGLE_LANE_KEEP_ASSIST_SYSTEM";

    AudioManager mAudioManager;

    SharedPreferences sharedPref;

    public Car mCarApiClient;
    public boolean isProvisional = false;
    public CarSettingsManager carSettingsManager;
    public CarSensorManager mCarSensorManager;
    public int mConfiger = -1;
    private boolean isCarSensorReady = false;
    private int ignitionState = 0;
    public int ONGOING_NOTIFICATION_ID = 1;

    public com.desay_svautomotive.carlibs.hardware.CarSensorManager desayCarSensorManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Log.d(TAG, "receive intent:" + intent.getAction());
        }
        if (intent != null && intent.getAction() == ACTION_TOGGLE_LANE_KEEP_ASSIST_SYSTEM) {
            toggleLaneKeepAssistSystemValue(2);
        } else if (intent != null && intent.getAction() == ACTION_ENABLE_LANE_KEEP_ASSIST_SYSTEM) {
            toggleLaneKeepAssistSystemValue(1);
        } else if (intent != null && intent.getAction() == ACTION_DISABLE_LANE_KEEP_ASSIST_SYSTEM) {
            toggleLaneKeepAssistSystemValue(0);
        } else if (intent != null && intent.getAction() == ACTION_DISABLE_PRE_COLLISION_SYSTEM) {
            togglePreCollisionSystem(false);
        } else if (intent != null && intent.getAction() == ACTION_ENABLE_PRE_COLLISION_SYSTEM) {
            togglePreCollisionSystem(true);
        } else {
            checkRadioPlay();
            togglePreCollisionSystem(false);
            openWifiHotspot();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setForegroundService();
        }

        this.carSettingsManager = new CarSettingsManager();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        sharedPref = H9PatchesApplication.getSharedPref();

        mHandler.removeMessages(99);
        mHandler.sendEmptyMessageDelayed(99, 1000);

        mHandler.removeMessages(101);
        mHandler.sendEmptyMessageDelayed(101, 1000 * 60 * 5);
    }

    public OnCarSensorListener.Stub carSensorListener = new OnCarSensorListener.Stub() {
        public void onChangeEvent(int id, int zone, int value) {
            Log.d(H9PatchesService.TAG, "car sensor listener: " + id + ":" + zone + ":" + value);
            if (id == 10) {
                H9PatchesService.this.mHandler.removeMessages(99);
                Log.d(H9PatchesService.TAG, "发动机状态改变:10");
                H9PatchesService.this.togglePreCollisionSystem(false);
            }
        }
    };

    CarSensorManager.OnSensorChangedListener ignitionStateListener = new CarSensorManager.OnSensorChangedListener() {
        public void onSensorChanged(CarSensorEvent carSensorEvent) {
            Log.d(H9PatchesService.TAG, "ignitionStateListener:" + carSensorEvent.intValues[0]);
            int state = carSensorEvent.intValues[0];
            if (H9PatchesService.this.ignitionState == 5 && state == 4) {
                H9PatchesService.this.mHandler.removeMessages(99);
                H9PatchesService.this.checkRadioPlay();
                H9PatchesService.this.togglePreCollisionSystem(false);
            } else if (H9PatchesService.this.ignitionState == 2 && state == 3) {
                H9PatchesService.this.checkRadioPlay();
            }
            H9PatchesService.this.ignitionState = state;
        }
    };

    @SuppressLint({"HandlerLeak"})
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 99) {
                if (!isCarSensorReady) {
                    isCarSensorReady = H9PatchesService.this.carSettingsManager.isCarSensorReady();
                    if (isCarSensorReady) {
                        H9PatchesService.this.carSettingsManager.registerCarSensorCallback(H9PatchesService.this.carSensorListener);
                    }
                }

                if (!H9PatchesService.this.carSettingsManager.isCarSensorReady() || !H9PatchesService.this.carSettingsManager.isDriverDailyAssistReady() || !H9PatchesService.this.carSettingsManager.isDriverAssistReady() || !H9PatchesService.this.carSettingsManager.isReverseAssistReady() || !H9PatchesService.this.carSettingsManager.isCarEnergyReady() || !CarSettingsApplication.get().isMoreTwoSecond().booleanValue()) {
                    removeMessages(99);
                    sendEmptyMessageDelayed(99, 1000);
                    return;
                } else {
                    removeMessages(99);
                    //H9PatchesService.this.disablePreCollisionSystem();
                }
            } else if (msg.what == 101) {

                mHandler.removeMessages(101);
                mHandler.sendEmptyMessageDelayed(101, 1000 * 60 * 5);
            }
        }
    };


    public void initCar() {
        this.mCarApiClient = Car.createCar(this, new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                try {
                    CarSensorManager unused = H9PatchesService.this.mCarSensorManager = (CarSensorManager) H9PatchesService.this.mCarApiClient.getCarManager(android.support.car.Car.SENSOR_SERVICE);//android.support.car.Car.SENSOR_SERVICE);
                    H9PatchesService.this.mCarSensorManager.registerListener(H9PatchesService.this.ignitionStateListener, 22, 0);
                } catch (CarNotConnectedException e) {
                    e.printStackTrace();
                }
            }

            public void onServiceDisconnected(ComponentName name) {
                if (H9PatchesService.this.mCarSensorManager != null) {
                    H9PatchesService.this.mCarSensorManager.unregisterListener(H9PatchesService.this.ignitionStateListener);
                }
            }
        });
        this.mCarApiClient.connect();
    }

    public void checkIgnitionState() {
        try {
            int accState = getIgnitionState();
            Log.d(TAG, "checkIgnitionState: " + accState);
            if (accState == 2 || accState == 3) {
//                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getIgnitionState() {
        try {
            if (this.mCarSensorManager != null) {
                return this.mCarSensorManager.getLatestSensorEvent(22).intValues[0];
            }
        } catch (CarNotConnectedException | NullPointerException e) {
            e.printStackTrace();
        }
        return 2;
    }

    public int getConfigValue(String name) {
        Cursor cursor = getContentResolver().query(Uri.parse("content://com.desay_svautomotive.provider.DesayProvider/config"), (String[]) null, "name=?", new String[]{name}, (String) null);
        int value = -1;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                value = cursor.getInt(cursor.getColumnIndex("value"));
                Log.i(TAG, "query: name - " + cursor.getString(cursor.getColumnIndex("name")));
                Log.i(TAG, "query: value - " + value);
            }
            cursor.close();
        } else {
            Log.e(TAG, "query: cursor == null");
        }
        this.mConfiger = value;
        return value;
    }

    private void toggleLaneKeepAssistSystemValue(int command) {
        boolean b = sharedPref.getBoolean(getString(R.string.preference_saved_AEB_key), false);
        if (!b) {
            return;
        }

        if (this.carSettingsManager == null) {
            this.carSettingsManager = new CarSettingsManager();
        }
        if (this.mCarApiClient == null) {
            initCar();
        }

        int accState = getIgnitionState();
        Log.d(TAG, "toggleLaneKeepAssistSystemValuea cc状态: " + accState);
        if (accState != 4) {
            Log.d(TAG, "toggleLaneKeepAssistSystemValue acc状态不满足: " + accState);
            return;
        }

        int curState = this.carSettingsManager.getLaneKeepAssistSystemValue();
        if (1 == curState && 0 == command) {
            this.carSettingsManager.setLaneKeepAssistSystemValue(0);
            Log.d(TAG, "关闭车道保持辅助");
        } else if (0 == curState && 1 == command) {
            this.carSettingsManager.setLaneKeepAssistSystemValue(1);
            Log.d(TAG, "打开车道保持辅助");
        } else if (2 == command) {
            this.carSettingsManager.setLaneKeepAssistSystemValue(curState == 1 ? 0 : 1);
            Log.d(TAG, "切换车道保持辅助为: " + (curState == 1 ? 0 : 1));
        }
    }

    public void togglePreCollisionSystem(final boolean on) {
        boolean b = sharedPref.getBoolean(getString(R.string.preference_saved_LANE_key), false);
        Log.d(TAG, "------------------------b:" + b + "---on:" + on);
        if (!b) {
            return;
        }

        H9PatchesService.this.mHandler.removeMessages(99);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                H9PatchesService.this._togglePreCollisionSystem(on ? 1 : 0);
            }
        }, 5000);
    }

    private void _togglePreCollisionSystem(int state) {
        if (this.carSettingsManager == null) {
            this.carSettingsManager = new CarSettingsManager();
        }
        if (this.mCarApiClient == null) {
            initCar();
        }

        int accState = getIgnitionState();
        if (accState != 4) {
            Log.d(TAG, "_togglePreCollisionSystem acc状态不满足: " + accState);
//            return;
        }

        Log.d(TAG, "_togglePreCollisionSystem state: " + state);

        int curState = this.carSettingsManager.getPreCollisionSystemValue(VehicleDoor.DOOR_HOOD);
        if (1 == curState && 0 == state) {
            this.carSettingsManager.setPreCollisionSystemValue(VehicleDoor.DOOR_HOOD, state);
            Log.d(TAG, "关闭碰撞设置");
        } else if (0 == curState && 1 == state) {
            this.carSettingsManager.setPreCollisionSystemValue(VehicleDoor.DOOR_HOOD, state);
            Log.d(TAG, "打开碰撞设置");
        }
    }

    public void checkRadioPlay() {
//        AudioManager mAudioManager = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
//
//        if(mode == Config.MUSIC_NEXT) {
//            KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT);
//            mAudioManager.dispatchMediaKeyEvent(event);
//        }else if(mode == Config.MUSIC_PLAY){
//            KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY);
//            mAudioManager.dispatchMediaKeyEvent(event);
//        }
//        else if(mode == Config.MUSIC_PREV){
//            KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS);
//            mAudioManager.dispatchMediaKeyEvent(event);
//        }


//        KeyEvent ke = new KeyEvent(KeyEvent.ACTION_DOWN,
//                KeyEvent.KEYCODE_MEDIA_PAUSE);
//        Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
//        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
//
//        // construct a PendingIntent for the media button and unregister it
//        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
//        PendingIntent pi = PendingIntent.getBroadcast(context,
//                0/*requestCode, ignored*/, mediaButtonIntent, 0/*flags*/);
//        intent.putExtra(Intent.EXTRA_KEY_EVENT, ke);
//        sendKeyEvent(pi,context, intent);
//
//        ke = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE);
//        intent.putExtra(Intent.EXTRA_KEY_EVENT, ke);
//        sendKeyEvent(pi, context, intent);
//

//        AudioManager mAudioManager = (AudioManager) getApplicationContext().getSystemService(AUDIO_SERVICE);
//        if(mAudioManager.isMusicActive())
//        {
//            int result = mAudioManager.requestAudioFocus(AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
//        }

        // pause
//        Intent i = new Intent("com.android.music.musicservicecommand");
//        i.putExtra("command", "pause");
//        sendBroadcast(i);


//        AudioManager mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
//
//        if (mAudioManager.isMusicActive()) {
//
//            Intent i = new Intent("com.android.music.musicservicecommand");
//
//            i.putExtra("command", "pause");
//            YourApplicationClass.this.sendBroadcast(i);
//        }

        boolean b1 = sharedPref.getBoolean(getString(R.string.preference_saved_RADIO_key), false);
        if (!b1) {
            return;
        }
        Log.d(TAG, "执行关闭收音机 ");
        Utils.requestAudioFocus();
//        new Handler().postDelayed(new Runnable() {
//            public void run() {
//                Intent intent = new Intent("pause");
//                String packageName = "com.desay_svautomotive.svradio";
//                String className = "com.desay_svautomotive.svradio.service.RadioService";
//                intent.setClassName(packageName, className);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    startForegroundService(intent);
//                } else {
//                    startService(intent);
//                }
//            }
//        }, 1000);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void  setForegroundService() {
        NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel serviceChannel = new NotificationChannel(
                getString(R.string.notification_channel),
                getString(R.string.notification_title),
                NotificationManager.IMPORTANCE_HIGH);
        notifManager.createNotificationChannel(serviceChannel);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification =
                new Notification.Builder(this, getString(R.string.notification_channel))
                        .setContentTitle(getString(R.string.notification_title))
                        .setContentText(getString(R.string.notification_message))
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentIntent(pendingIntent)
                        .setTicker(getString(R.string.ticker_text))
                        .build();

        startForeground(ONGOING_NOTIFICATION_ID, notification);
    }

    private void openWifiHotspot() {
        boolean b = sharedPref.getBoolean(getString(R.string.preference_saved_HOTSPOT_key), false);
        if (!b) {
            return;
        }
        Utils.openWifiHotspot();
    }

}

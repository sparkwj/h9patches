package com.spark.h9patches;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.telecom.TelecomManager;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.widget.Toast;

import java.io.IOException;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.N)
public class KeyPolicy extends ServiceFacility implements ActivityWatcher.OnTopActivityChangedListener {
    final static int H9_KEYCODE_VOLUME_UP = 24;
    final static int H9_KEYCODE_VOLUME_DOWN = 25;
    final static int H9_KEYCODE_POWER = 26;
    final static int H9_KEYCODE_MODE = 296;
    final static int H9_KEYCODE_TUNE = 297;
    final static int H9_KEYCODE_TUNE_RIGHT = 106;
    final static int H9_KEYCODE_TUNE_LEFT = 107;
    final static int H9_KEYCODE_NEXT = 87;
    final static int H9_KEYCODE_PREV = 88;
    final static int H9_KEYCODE_SET = 287;
    final static int H9_KEYCODE_NAV = 286;
    final static int H9_KEYCODE_MUTE = 164;
    final static int H9_KEYCODE_TEL = 285;
    final static int H9_KEYCODE_TEL_OFF = 300;
    final static int H9_KEYCODE_VOICE_ASSISTANT = 291;
    final static int H9_DEVICEID_STEERING_WHEEL = 1;
    final static int H9_DEVICEID_CENTRAL_CONTROL = 6;
    long LONG_PRESS_TIME = 500;
    ActivityWatcher watcher;
    TelecomManager mTelecomManager;
    String mapPackageName = "";
    int lastRemappedKeyCode = 0;
    boolean prefEnableKeyRemapping = false;
    String activeActivityPackageName = "";
    Handler longPressHandler;
    Runnable previousSendKeyCode;
    AudioManager audioManager;

    public KeyPolicy(Context context, ActivityWatcher watcher) {
        super(context);
        this.watcher = watcher;
        this.watcher.registerOnTopActivityChanged(this);
        mTelecomManager = (TelecomManager) getContext().getSystemService(Context.TELECOM_SERVICE);
        audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        mapPackageName = sharedPreferences.getString(getString(R.string.pref_default_map), "");
        prefEnableKeyRemapping = sharedPreferences.getBoolean(getString(R.string.pref_key_remapping), getResourcesBoolean(R.bool.pref_key_remapping));
    }

    @Override
    public void onSharedPreferenceChanged(String key) {
        if (key.equals(getString(R.string.pref_default_map))) {
            mapPackageName = sharedPreferences.getString(key, "");
        }
        if (key.equals(getString(R.string.pref_key_remapping))) {
            prefEnableKeyRemapping = sharedPreferences.getBoolean(getString(R.string.pref_key_remapping), getResourcesBoolean(R.bool.pref_key_remapping));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean onKeyEvent(KeyEvent event) {
        if (prefEnableKeyRemapping || (event.getKeyCode() == H9_KEYCODE_NAV)) {
            return _onKeyEvent(event);
        }
        return false;
    }


    long lastDownTime = 0;
    @RequiresApi(api = Build.VERSION_CODES.O)
    private boolean _onKeyEvent(KeyEvent event) {
        Log.d(TAG, "keycode: " + event.getKeyCode() + " keyaction: " + event.getAction() + "deviceid: " + event.getDeviceId());
        Log.d(TAG, "lasthookedkeycode: " + lastRemappedKeyCode);
        int eventAction = event.getAction();
        int eventKeyCode = event.getKeyCode();
        int eventDeviceId = event.getDeviceId();

        if (eventAction == KeyEvent.ACTION_DOWN) {
            lastDownTime = SystemClock.elapsedRealtime();
        } else if (eventAction == KeyEvent.ACTION_UP) {
            Log.d(TAG, "elapsed time: " + (SystemClock.elapsedRealtime() - lastDownTime));
        }

        if (longPressHandler != null) {
            longPressHandler.removeCallbacksAndMessages(null);
            longPressHandler = null;
        }

        if (eventAction == KeyEvent.ACTION_UP && eventKeyCode == lastRemappedKeyCode) {
            lastRemappedKeyCode = 0;
            return true;
        }

        if (eventKeyCode == H9_KEYCODE_NAV) {
            if (mapPackageName != null && !mapPackageName.isEmpty() && !mapPackageName.equals("-")) {
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(mapPackageName);
                startActivity(launchIntent);
                lastRemappedKeyCode = eventKeyCode;
                return true;
            }
        }

        if (eventKeyCode == H9_KEYCODE_TEL_OFF) {
            boolean granted = getContext().checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
            if (granted && mTelecomManager.isInCall()) {
                return false;
            }
            return sendKeyCode(KeyEvent.KEYCODE_BACK);
        }

        if (eventDeviceId == H9_DEVICEID_STEERING_WHEEL && !ActivityWatcher.isTvApp(activeActivityPackageName) && eventKeyCode == H9_KEYCODE_MODE) {
            if (eventAction == KeyEvent.ACTION_DOWN) {
                longPressHandler = new Handler();
                longPressHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        LaneKeepAssist laneKeepAssist = (LaneKeepAssist)getFacility(LaneKeepAssist.class);
                        laneKeepAssist.toggleLaneKeepAssistSystem(2);
                        lastRemappedKeyCode = eventKeyCode;
                    }
                }, LONG_PRESS_TIME);
                lastRemappedKeyCode = 0;
                return true;
            } else if (eventAction == KeyEvent.ACTION_UP) {
//                lastRemappedKeyCode = eventKeyCode;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        launchFunctionalApp();
                    }
                }, 0);
                lastRemappedKeyCode = 0;
                return true;
            }
        }

        if (eventDeviceId == H9_DEVICEID_STEERING_WHEEL && (ActivityWatcher.isTvApp(activeActivityPackageName))) {
            int mapToDPadKeyCode = 0;
            switch (eventKeyCode) {
                case H9_KEYCODE_PREV:
                    mapToDPadKeyCode = KeyEvent.KEYCODE_DPAD_LEFT;
                    break;
                case H9_KEYCODE_NEXT:
                    mapToDPadKeyCode = KeyEvent.KEYCODE_DPAD_RIGHT;
                    break;
                case H9_KEYCODE_VOLUME_DOWN:
                    mapToDPadKeyCode = KeyEvent.KEYCODE_DPAD_DOWN;
                    break;
                case H9_KEYCODE_VOLUME_UP:
                    mapToDPadKeyCode = KeyEvent.KEYCODE_DPAD_UP;
                    break;
                case H9_KEYCODE_MODE:
                    mapToDPadKeyCode = KeyEvent.KEYCODE_DPAD_CENTER;
                    break;
            }
            if (mapToDPadKeyCode > 0 && eventAction == KeyEvent.ACTION_DOWN) {
                int finalMapToDPadKeyCode = mapToDPadKeyCode;
                longPressHandler = new Handler();
                longPressHandler.postDelayed(() -> {
                    int reverseKeyCode = finalMapToDPadKeyCode;
                    switch (finalMapToDPadKeyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            reverseKeyCode = H9_KEYCODE_PREV;
                            break;
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            reverseKeyCode = H9_KEYCODE_NEXT;
                            break;
//                        case KeyEvent.KEYCODE_DPAD_CENTER:
//                            reverseKeyCode = 0;
//                            launchFunctionalApp();
//                            break;
                    }
                    KeyPolicy.this.sendKeyCode(reverseKeyCode);
                    lastRemappedKeyCode = eventKeyCode;
                }, LONG_PRESS_TIME);
                return true;
            } else if (mapToDPadKeyCode > 0 && eventAction == KeyEvent.ACTION_UP) {
                sendKeyCode(mapToDPadKeyCode);
                lastRemappedKeyCode = 0;
                return true;
            }
        }

        if (eventDeviceId == H9_DEVICEID_CENTRAL_CONTROL && ActivityWatcher.isTvApp(activeActivityPackageName)) {
            switch (eventKeyCode) {
                case H9_KEYCODE_VOLUME_UP:
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                    lastRemappedKeyCode = eventKeyCode;
                    return true;
                case H9_KEYCODE_VOLUME_DOWN:
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                    lastRemappedKeyCode = eventKeyCode;
                    return true;
            }
        }

        if (eventKeyCode == H9_KEYCODE_TUNE) {
            Radio radio = (Radio)getFacility(Radio.class);
            if (watcher.getTopActivityPackageName().equals("com.desay_svautomotive.svradio")) {
                radio.stop();
                sendKeyCode(KeyEvent.KEYCODE_BACK);
                lastRemappedKeyCode = eventKeyCode;
                return true;
            } else {
                radio.start();
                return false;
            }
        }

        Log.d(TAG, "final");
        lastRemappedKeyCode = 0;
        return false;
    }

    boolean refreshPlayListFlag = true;
    private void launchFunctionalApp(){
        String packageName = sharedPreferences.getString(getString(R.string.pref_function_app), "");
        if (packageName == null || packageName.isEmpty() || "-".equals(packageName)) {
            return;
        }
        String YOUTUBE_MUSIC = "app.revanced.android.apps.youtube.music|app.rvx.android.apps.youtube.music|com.google.android.youtube.music";
        if (YOUTUBE_MUSIC.contains(packageName) || packageName.contains("youtube.music")) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setPackage(packageName);
                intent.setData(Uri.parse("https://music.youtube.com"));
                startActivity(intent);
                String waitScript = "r=%s; while ! dumpsys activity top|grep -zq \"%s\"; do sleep 1; ((--r))||break; done";
                Runtime.getRuntime().exec(new String[]{"sh", "-c", String.format(waitScript, 15, "youtube.music.activities.MusicActivity.*avatar")}).waitFor();
                intent.setData(Uri.parse("https://music.youtube.com/playlist?list=RDTMAK5uy_kset8DisdE7LSD4TNjEVvrKRTmG7a56sY"));
                startActivity(intent);
                Runtime.getRuntime().exec(new String[]{"sh", "-c", String.format(waitScript, 5, "youtube.music.activities.MusicActivity.*music_play_button")}).waitFor();
                intent.setData(Uri.parse("https://music.youtube.com/watch?list=RDTMAK5uy_kset8DisdE7LSD4TNjEVvrKRTmG7a56sY"));
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            }
        } else {
            PackageManager pm = getPackageManager();
            Intent intent = pm.getLaunchIntentForPackage(packageName);
            if (intent == null) {
                return;
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public void onTopActivityChanged(String packageName) {
        activeActivityPackageName = packageName;
    }

    private boolean sendKeyCode(int keycode) {
        long now = System.currentTimeMillis();
        try {
            getInputManager().injectInputEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, keycode, 0, 0, -1, 0, 0,
                    InputDevice.SOURCE_DPAD), 0);
            getInputManager().injectInputEvent(new KeyEvent(now + 1, now + 1, KeyEvent.ACTION_UP, keycode, 0, 0, -1, 0, 0,
                    InputDevice.SOURCE_DPAD), 0);
            return true;
        } catch (RemoteException ignored) {
            return false;
        }
    }
}

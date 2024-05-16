package com.spark.h9patches;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.RequiresApi;

import com.android.car.radio.service.IRadioManager;
import com.android.car.radio.service.RadioStation;

@RequiresApi(api = Build.VERSION_CODES.O)
public class Radio extends ServiceFacility {
    AudioManager mAudioManager;
    IRadioManager mRadioManager;
    ComponentName radioService = new ComponentName("com.desay_svautomotive.svradio", "com.desay_svautomotive.svradio.service.RadioService");

    AudioManager.OnAudioFocusChangeListener focusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
//                    AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
//                    attrBuilder.setUsage(AudioAttributes.USAGE_MEDIA);
//                    attrBuilder.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC);
//                    AudioFocusRequest.Builder builder = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN);
//                    builder.setAudioAttributes(attrBuilder.build());
//                    builder.setAcceptsDelayedFocusGain(true);
//                    builder.setOnAudioFocusChangeListener(Radio.this.focusChangeListener);
//                    mAudioManager.requestAudioFocus(builder.build());
                    break;
            }
        }
    };
    ServiceConnection radioServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            try {
                mRadioManager = IRadioManager.Stub.asInterface(iBinder);
                pauseRadioIfNeeds();
                new Handler().postDelayed(() -> checkRadio(), 60000);
            } catch (Exception ignored) {
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };
    
    public Radio(Context context) {
        super(context);
        mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public void onServiceStart() {
        bindRadioService();
    }

    @Override
    public void onServiceStop() {
        if (radioServiceConnection != null) {
            getContext().unbindService(radioServiceConnection);
        }
    }

    @Override
    public void onSharedPreferenceChanged(String key) {
        if (key.equals(getString(R.string.pref_turn_off_radio))) {
            checkRadio();
        }
    }

    private void pauseRadio() {
        getContext().startService(new Intent().setComponent(radioService).setAction("pause"));
    }

    private void pauseRadioIfNeeds() {
        boolean prefValue = sharedPreferences.getBoolean(getString(R.string.pref_turn_off_radio), getResourcesBoolean(R.bool.pref_turn_off_radio));
        if (prefValue) {
            pauseRadio();
        }
    }

    private void requireAudioFocus() {
        if (mAudioManager != null) {
            try {
                mAudioManager.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            } catch (Exception ignored) {
            }
        }
    }

    private void bindRadioService() {
        Intent intent = new Intent();
        intent.setComponent(radioService);
        intent.setAction("pause");
        getContext().bindService(intent, radioServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void checkRadio() {
        boolean prefValue = sharedPreferences.getBoolean(getString(R.string.pref_turn_off_radio), getResourcesBoolean(R.bool.pref_turn_off_radio));
        if (prefValue) {
            try {
                pauseRadio();
                requireAudioFocus();
            } catch (Exception ignored) {
            }
        }
    }

    public void start() {
        if (mRadioManager != null) {
            try {
                RadioStation station = mRadioManager.getCurrentRadioStation();
                mRadioManager.openRadioBand(1);
                mRadioManager.openRadioBand(0);
                mRadioManager.openRadioBand(station.getRadioBand());
//                mRadioManager.tune(station);
                new Handler().postDelayed(() -> {
                    getContext().startService(new Intent().setComponent(radioService).setAction("play"));
                }, 2000);
            } catch (RemoteException ignored) {
            }
        }
//        new Handler().postDelayed(() -> {
//            ComponentName radioComponent = new ComponentName("com.desay_svautomotive.svradio", "com.desay_svautomotive.svradio.service.RadioService");
//            getContext().startService(new Intent().setComponent(radioComponent).setAction("pause"));
//            new Handler().postDelayed(() -> {
//                getContext().startService(new Intent().setComponent(radioComponent).setAction("play"));
//            }, 1000);
//        }, 1000);
    }
    public void stop() {
        pauseRadio();
        requireAudioFocus();
    }
}

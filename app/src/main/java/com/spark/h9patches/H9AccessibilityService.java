package com.spark.h9patches;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import java.util.LinkedHashMap;
import java.util.Objects;

public class H9AccessibilityService extends AccessibilityService {
    private final static String TAG = H9AccessibilityService.class.getName();
    static H9AccessibilityService instance;

    private KeyPolicy mKeyPolicy;
//    private final LinkedHashMap<Class<?>, IFacility> mFacilities = new LinkedHashMap<>();
//    BroadcastReceiver mReceiver;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        if (mKeyPolicy == null) {
            H9PatchesService service = H9PatchesService.getInstance();
            if (service != null) {
                mKeyPolicy = (KeyPolicy) service.getFacility(KeyPolicy.class);
            }
        }
        if (mKeyPolicy != null) {
            return mKeyPolicy.onKeyEvent(event);
        }
        return super.onKeyEvent(event);
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        registerReceiver();
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onServiceConnected() {
        Log.d(TAG, "onServiceConnected");
        super.onServiceConnected();
        instance = this;
        AccessibilityServiceInfo serviceInfo = getServiceInfo();
        setServiceInfo(serviceInfo);
//        createFacilities();
//        notifyOnStart();
    }

//    private void registerReceiver() {
//        mReceiver = new BroadcastReceiver() {
//            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                H9AccessibilityService.this.onReceiver(intent);
//            }
//        };
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(Intent.ACTION_BOOT_COMPLETED);
//        registerReceiver(mReceiver, filter);
//    }

//    private void onReceiver(Intent intent) {
//        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
//            notifyOnStart();
//        }
//    }

//    @RequiresApi(api = Build.VERSION_CODES.O)
//    private void createFacilities() {
//        ActivityWatcher watcher = new ActivityWatcher(this);
//        addFacility(watcher);
//        mKeyPolicy = new KeyPolicy(this, watcher);
//        addFacility(mKeyPolicy);
//        addFacility(new Launcher(this));
//        addFacility(new NavigationBar(this));
//        addFacility(new DPadSimulator(this, watcher));
//        addFacility(new NavigationButton(this));
//        addFacility(new LaneKeepAssist(this));
//        addFacility(new PreCollisionSystem(this));
//        addFacility(new Wifi(this));
//        addFacility(new StartupScript(this));
//        addFacility(new ActionReceiver(this));
//        addFacility(new Radio(this));
//        addFacility(new Accounts(this));
//    }
//
//    public void notifyOnStart() {
//        Log.d(TAG, "notifyOnStart");
//        for (IFacility facility : mFacilities.values()) {
//            try {
//                facility.onServiceStart();
//            } catch (Exception e) {
//                Log.e(TAG, Objects.requireNonNull(e.getMessage()));
//            }
//        }
//    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
    }

    @Override
    public void onInterrupt() {
    }

//    private void addFacility(IFacility facility) {
//        mFacilities.put(facility.getClass(), facility);
//    }
//
//    public IFacility getFacility(Class<?> cls) {
//        return mFacilities.get(cls);
//    }
//
//    public interface IFacility {
//        void onServiceStart();
//    }
//
//    public static H9AccessibilityService getInstance() {
//        return instance;
//    }
}

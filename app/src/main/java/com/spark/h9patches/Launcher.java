package com.spark.h9patches;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.widget.Toast;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Launcher extends ServiceFacility {
    ActivityWatcher mWatcher;
    String prefLauncherPackageName = "";
    Timer mTimer;
    BroadcastReceiver shutdownReceiver;
    public Launcher(Context context, ActivityWatcher watcher) {
        super(context);
        mWatcher = watcher;
        mTimer = new Timer();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onServiceStart() {
        checkDefaultLauncher(true);
        registerReceiver();
    }

    @Override
    public void onServiceStop() {
        if (shutdownReceiver != null) {
            getContext().unregisterReceiver(shutdownReceiver);
        }
    }

    private void registerReceiver() {
        if (shutdownReceiver != null) {
            return;
        }
        shutdownReceiver = new BroadcastReceiver() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onReceive(Context context, Intent intent) {
                Launcher.this.onShutdown();
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SHUTDOWN);
        getContext().registerReceiver(shutdownReceiver, filter);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void onShutdown() {
        ActivityManager am = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.AppTask> tasks = am.getAppTasks();
        for (ActivityManager.AppTask task : tasks) {
            try {
                task.finishAndRemoveTask();
            } catch (Exception ignored) {
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onSharedPreferenceChanged(String key) {
        checkDefaultLauncher(false);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void checkDefaultLauncher(boolean launch_it) {
//        filter.addCategory(Intent.CATEGORY_HOME);
//        filter.addCategory(Intent.CATEGORY_DEFAULT);
//// Set the activity as the preferred option for the device.
//        ComponentName activity = new ComponentName(context, KioskModeActivity.class);
//        DevicePolicyManager dpm =
//                (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
//
//        devicePolicyManager.clearPackagePersistentPreferredActivities(adminComponent, getPackageName());
//        dpm.addPersistentPreferredActivity(null, filter, activity);
//        usage: dpm [subcommand] [options]
//        usage: dpm set-device-owner <COMPONENT>
//                usage: dpm set-profile-owner <COMPONENT> <USER_ID>
//
//                dpm set-device-owner: Sets the given component as active admin, and its package as device owner.
//                dpm set-profile-owner: Sets the given component as active admin and profile owner for an existing user.
//
//                Runtime.getRuntime().exec("dpm set-device-owner com.foo.deviceowner/.DeviceAdminRcvr");
//        adb shell dpm set-device-owner "com.afwsamples.testdpc/.DeviceAdminReceiver"
//            google play???
//        https://source.android.com/docs/devices/admin/implement?hl=zh-cn
//        https://developer.android.com/work/device-admin
        String systemPackage = getString(R.string.str_system_launcher_package);
        String systemActivity = getString(R.string.str_system_launcher_activity);
        String packageName = sharedPreferences.getString(getString(R.string.pref_default_launcher), systemPackage);
        packageName = (packageName == null || packageName.isEmpty() || packageName.equals("-")) ? systemPackage : packageName ;

        PackageManager pm = getPackageManager();
        Intent intent1 = new Intent(Intent.ACTION_MAIN);
        intent1.addCategory(Intent.CATEGORY_HOME);
        intent1.addCategory(Intent.CATEGORY_DEFAULT);
        intent1.setPackage(packageName);
        List<ResolveInfo> lst = pm.queryIntentActivities(intent1, PackageManager.MATCH_DEFAULT_ONLY | PackageManager.MATCH_DISABLED_COMPONENTS);
        if (lst.isEmpty()) {
            String info = "Missing launcher: " + packageName;
            Toast.makeText(getContext(), info, Toast.LENGTH_LONG).show();
            Log.e(TAG, info);
            return;
        }
        ResolveInfo resolveInfo = lst.get(0);
        String activityName = resolveInfo.activityInfo.name;

//        pm.setComponentEnabledSetting(new ComponentName(packageName, activityName), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        ComponentName launcherComponent = new ComponentName(packageName, activityName);
        pm.setComponentEnabledSetting(launcherComponent, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
        setDefaultLauncher(packageName, activityName);
        try {
            Runtime.getRuntime().exec("cmd package set-home-activity " + packageName + "/" + activityName);
            Runtime.getRuntime().exec("appwidget grantbind --package " + packageName + " --user 0");
        } catch (Exception e) {
            Log.e(TAG, "error: cmd package set-home-activity " + packageName + "/" + activityName);
        }
        //disable previous launcher here
        if (!packageName.equals(systemPackage)) {
            pm.setComponentEnabledSetting(new ComponentName(systemPackage, systemActivity), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
        prefLauncherPackageName = packageName;
//        Intent current_intent = new Intent("android.intent.action.MAIN");
//        current_intent.addCategory("android.intent.category.HOME");
//        ResolveInfo current_launcher_info = pm.resolveActivity(current_intent, PackageManager.MATCH_DEFAULT_ONLY);
//        if (current_launcher_info != null) {
//            if (!packageName.equals(current_launcher_info.activityInfo.packageName)) {
//                pm.setComponentEnabledSetting(new ComponentName(current_launcher_info.activityInfo.packageName, current_launcher_info.activityInfo.name), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
//            }
//        }

//        IntentFilter filter = new IntentFilter();
//        filter.addAction("android.intent.action.MAIN");
//        filter.addCategory("android.intent.category.DEFAULT");

//        Log.d(TAG, "000000000000000" + preferred_launcher.toString());
//        ComponentName launcher_g6 = new ComponentName("com.android.launcher", "com.desaysv.hmi.launcher.Launcher2Activity");
//        Intent intent;
//        intent = pm.getLaunchIntentForPackage(preferred_launcher);
//        intent = intent != null ? intent : (new Intent()).setComponent(launcher_g6);
//        ComponentName new_launcher = intent.getComponent();
//        Log.d(TAG, "2222222222222222" + new_launcher.toString());
//        ComponentName[] components = new ComponentName[] {launcher_g6, new_launcher};
//        pm.clearPackagePreferredActivities("com.android.launcher");
//        pm.addPreferredActivity(filter, IntentFilter.MATCH_CATEGORY_EMPTY, components, new_launcher);
//        try {
//            Runtime.getRuntime().exec("cmd package set-home-activity " + new_launcher.getPackageName() + "/" + new_launcher.getClassName());
//        } catch (Exception e) {
//            //
//        }

//        Intent current_intent = new Intent("android.intent.action.MAIN");
//        current_intent.addCategory("android.intent.category.HOME");
//        String current_launcher = pm.resolveActivity(current_intent, PackageManager.MATCH_DEFAULT_ONLY).resolveInfo.packageName;
//        pm.setComponentEnabledSetting(new_launcher, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        if (launch_it) {
            sendKeyCode(KeyEvent.KEYCODE_BACK);
            sendKeyCode(KeyEvent.KEYCODE_BACK);
            sendKeyCode(KeyEvent.KEYCODE_BACK);
            Log.d(TAG, "start launcher: " + resolveInfo.activityInfo.packageName);
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(resolveInfo.activityInfo.packageName); // Replace with the target package name
            if (launchIntent != null) {
                startActivity(launchIntent);
            }
//            sendKeyCode(KeyEvent.KEYCODE_HOME);
            scheduleRunStartupApp();
        }
    }

    private void scheduleRunStartupApp() {
        if (prefLauncherPackageName == null || prefLauncherPackageName.isEmpty() || prefLauncherPackageName.equals("-")) {
            return;
        }
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (prefLauncherPackageName.equals(mWatcher.getTopActivityPackageName())) {
                    new Handler(Looper.getMainLooper()).postDelayed(Launcher.this::runStartupApp, 15000);
                    mTimer.cancel();
                }
            }
        }, 0, 5000);
    }

    //add
    private void setDefaultLauncher(String packageName, String className) {
        try {
            final PackageManager pm = getPackageManager();

            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.MAIN");
            filter.addCategory("android.intent.category.HOME");
            filter.addCategory("android.intent.category.DEFAULT");

            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            List<ResolveInfo> lst = pm.queryIntentActivities(intent, 0);
            final int N = lst.size();
            ComponentName[] components = new ComponentName[N];
            int bestMatch = 0;
            for (int i = 0; i < N; i++) {
                ResolveInfo r = lst.get(i);
                components[i] = new ComponentName(r.activityInfo.packageName, r.activityInfo.name);
                if (r.match > bestMatch) bestMatch = r.match;
            }
            ComponentName preActivity = new ComponentName(packageName, className);
//            pm.clearPackagePreferredActivities("com.android.launcher");
            pm.addPreferredActivity(filter, bestMatch, components, preActivity);

        } catch (Exception e) {
        }
    }

    public void runStartupApp() {
        String startup_app = sharedPreferences.getString(getString(R.string.pref_startup_app), "");
        if (startup_app != null && !startup_app.isEmpty() && !startup_app.equals("-")) {
            PackageManager pm = getPackageManager();
            Intent intent = pm.getLaunchIntentForPackage(startup_app);
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }

    private void sendKeyCode(int keycode) {
        long now = System.currentTimeMillis();
        try {
            getInputManager().injectInputEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, keycode, 0, 0, -1, 0, 0,
                    InputDevice.SOURCE_DPAD), 0);
            getInputManager().injectInputEvent(new KeyEvent(now + 1, now + 1, KeyEvent.ACTION_UP, keycode, 0, 0, -1, 0, 0,
                    InputDevice.SOURCE_DPAD), 0);
        } catch (RemoteException ignored) {
        }
    }
}

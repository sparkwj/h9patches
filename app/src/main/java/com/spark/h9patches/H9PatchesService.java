//package com.spark.h9patches;
//
//import android.annotation.SuppressLint;
//import android.app.ActivityManager;
//import android.app.Notification;
//import android.app.Service;
//import android.car.Car;
//import android.car.CarNotConnectedException;
//import android.car.hardware.CarSensorEvent;
//import android.car.hardware.CarSensorManager;
//import android.content.BroadcastReceiver;
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.content.ServiceConnection;
//import android.content.SharedPreferences;
//import android.content.pm.PackageManager;
//import android.content.pm.ResolveInfo;
//import android.graphics.PixelFormat;
//import android.hardware.input.IInputManager;
//import android.net.wifi.WifiManager;
//import android.os.Binder;
//import android.os.Build;
//import android.os.Handler;
//import android.os.IBinder;
//import android.os.Message;
//import android.os.RemoteException;
//import android.os.ServiceManager;
//import android.os.SystemClock;
//import android.provider.Settings;
//import android.support.annotation.RequiresApi;
//import android.support.car.input.CarInputManager;
//import android.support.v4.app.NotificationCompat;
//import android.text.TextUtils;
//import android.util.DisplayMetrics;
//import android.util.Log;
//import android.view.Gravity;
//import android.view.IKeyPolicy;
//import android.view.IKeyPolicyCallBack;
//import android.view.InputEvent;
//import android.view.KeyEvent;
//import android.view.LayoutInflater;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.WindowManager;
//import android.widget.FrameLayout;
//import android.widget.ImageButton;
//import android.widget.Toast;
//
////import com.android.car.radio.service.IRadioManager;
//import com.desay_svautomotive.caninfoservice.OnCarSensorListener;
//import com.desay_svautomotive.carlibs.hardware.VehicleDoor;
//import com.desay_svautomotive.svcarsettings.CarSettingsApplication;
//import com.desay_svautomotive.svcarsettings.manager.CarSettingsManager;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.StringReader;
//import java.util.Collections;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//public class H9PatchesService extends Service {
//    static final String TAG = "H9PatchesService";
//    //    private AudioManager mAudioManager;
//    private SharedPreferences sharedPref;
//    private Car mCar;
//    //    private boolean isProvisional = false;
//    private CarSettingsManager carSettingsManager;
//    private CarSensorManager mCarSensorManager;
//    //    private int mConfiger = -1;
//    private boolean isCarSensorReady = false;
//    private int ignitionState = 0;
//    private final int ONGOING_NOTIFICATION_ID = 1;
//    private ImageButton floatingButton;
//    private Runtime runtime = Runtime.getRuntime();
//    private BroadcastReceiver receiver;
//    private BroadcastReceiver receiver2;
//    private IKeyPolicy mKeyPolicyService;
////    private IKeyPolicy.Stub key_policy;
//    private IKeyPolicyCallBack mIKeyPolicyCallBackDispatcher;
//    private CarInputManager mCarInputManager;
//    IKeyPolicy aa;
//    private FrameLayout directionPad;
//    private IInputManager inputManager;
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return new H9PatchesServiceBinder();
//    }
//
//    public class H9PatchesServiceBinder extends Binder {
//        public H9PatchesService getPatchesService() {
//            return H9PatchesService.this;
//        }
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.N)
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            setForegroundService();
//        }
//
//        IBinder imBinder = ServiceManager.getService("input");
//        inputManager = IInputManager.Stub.asInterface(imBinder);
//
//        this.carSettingsManager = new CarSettingsManager();
////        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//        sharedPref = ((H9PatchesApplication)getApplication()).getSharedPreferences();
//        mHandler.removeMessages(99);
//        mHandler.sendEmptyMessageDelayed(99, 1000);
//        mHandler.removeMessages(101);
//        mHandler.sendEmptyMessageDelayed(101, 1000 * 60 * 5);
//
//        enableAccessibility(this);
//        checkFunctionStates();
//        registerReceiver();
////
////        Settings.Secure.putString(getContentResolver(),
////                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, getPackageName() + "/" + H9PatchesAccessibilityService.class.getName());
////        Settings.Secure.putString(getContentResolver(),
////                Settings.Secure.ACCESSIBILITY_ENABLED, "1");
//
//
////        mKeyPolicyService = IKeyPolicy.Stub.asInterface(ServiceManager.getService("key_policy"));
//////        mKeyPolicyService = IKeyPolicy.Stub.asInterface(binderKeyPolicy);
////        mIKeyPolicyCallBackDispatcher = new IKeyPolicyCallBack.Stub() {
////            @Override
////            public boolean callbackEvent(KeyEvent keyEvent, String s) throws RemoteException {
////                Toast.makeText(H9PatchesService.this, "hhhhhaaaaaa", Toast.LENGTH_SHORT).show();
////                return false;
////            }
////        };
////
////        try {
////            mKeyPolicyService.registerKeyCallBack(mIKeyPolicyCallBackDispatcher, "xxxxx", 10000000, "");
////        } catch (RemoteException e) {
////            throw new RuntimeException(e);
////        }
////        try {
////            Toast.makeText(H9PatchesService.this, String.valueOf(mKeyPolicyService.isHomeKeyToLauncher()), Toast.LENGTH_SHORT).show();
////        } catch (RemoteException e) {
////            throw new RuntimeException(e);
////        }
//
////        android.content.Intent intent = new android.content.Intent(this, "android.view.IKeyPolicy");
////        bindService(intent, mServerConnection, Context.BIND_AUTO_CREATE);
//    }
//
////    ServiceConnection mServerConnection = new ServiceConnection() {
////        @Override
////        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
////            Toast.makeText(H9PatchesService.this, "service connected", Toast.LENGTH_LONG).show();
////            mKeyPolicyService = IKeyPolicy.Stub.asInterface(iBinder);
////            try {
////                mKeyPolicyService.registerKeyCallBack(mIKeyPolicyCallBackDispatcher, "pre_handle", 3, "");
////            } catch (RemoteException e) {
////                throw new RuntimeException(e);
////            }
////            try {
////                Toast.makeText(H9PatchesService.this, String.valueOf(mKeyPolicyService.isHomeKeyToLauncher()), Toast.LENGTH_SHORT).show();
////            } catch (RemoteException e) {
////                throw new RuntimeException(e);
////            }
////        }
////
////        @Override
////        public void onServiceDisconnected(ComponentName componentName) {
////            //
////        }
////    };
//
//    @SuppressLint("UnspecifiedRegisterReceiverFlag")
//    private void registerReceiver() {
//        receiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                H9PatchesService.this.doActions(intent);
//            }
//        };
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(PatchesIntent.ACTION_COMMAND);
//        filter.addAction(PatchesIntent.ACTION_HIDE_NAVIGATION_BAR);
//        filter.addAction(PatchesIntent.ACTION_WIFI_HOTSPOT);
//        filter.addAction(PatchesIntent.ACTION_TOGGLE_LANE_KEEP_ASSIST_SYSTEM);
//        filter.addAction(PatchesIntent.ACTION_TURN_OFF_PRE_COLLISION_SYSTEM);
//        filter.addAction(PatchesIntent.ACTION_LAUNCHER);
//        filter.addAction(Intent.ACTION_SHUTDOWN);
//        registerReceiver(receiver, filter);
//
////        receiver2 = new BroadcastReceiver() {
////            @Override
////            public void onReceive(Context context, Intent intent) {
////                Toast.makeText(context, "hahaha", Toast.LENGTH_SHORT).show();
////            }
////        };
////        IntentFilter filter2 = new IntentFilter();
////        filter2.addAction("com.desay_svautomotive_start_navi");
////        registerReceiver(receiver2, filteri2);
////
//    }
//
//    private void doActions(Intent intent) {
////        Toast.makeText(this, intent == null ? "" : intent.getDataString(), Toast.LENGTH_LONG).show();
//        if (intent == null) {
//            return;
//        }
//        Log.d(TAG, "receive intent:" + intent.getAction());
//        String action = intent.getAction();
//        if (action != null) {
//            int state = intent.getIntExtra(PatchesIntent.EXTRA_FLAG_STATE, 1);
//            switch (action) {
//                case PatchesIntent.ACTION_TURN_OFF_PRE_COLLISION_SYSTEM:
//                    toggleCollisionSystem(state == 1 ? 0 : 1);
//                    break;
//                case PatchesIntent.ACTION_TOGGLE_LANE_KEEP_ASSIST_SYSTEM:
//                    toggleLaneKeepAssistSystem(state);
//                    break;
//                case PatchesIntent.ACTION_WIFI_HOTSPOT:
//                    toggleWifiHotspot(state);
//                    break;
//                case PatchesIntent.ACTION_HIDE_NAVIGATION_BAR:
//                    toggleNavigationBar(state == 1 ? 0 : 1);
//                    break;
//                case PatchesIntent.ACTION_COMMAND:
//                    try {
//                        String command = intent.getStringExtra("cmd");
//                        Toast.makeText(this, command, Toast.LENGTH_LONG).show();
//                        if (command != null && !command.isEmpty()) {
//                            Process proc = Runtime.getRuntime().exec(command);
//                            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
//                            BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
//                            StringBuilder msg = new StringBuilder();
//                            String s = null;
//                            while ((s = stdInput.readLine()) != null) {
//                                msg.append(s).append("\n");
//                            }
//                            if (!msg.toString().trim().isEmpty()) {
//                                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
//                            }
//                            msg = new StringBuilder();
//                            while ((s = stdError.readLine()) != null) {
//                                msg.append(s).append("\n");
//                            }
//                            if (!msg.toString().trim().isEmpty()) {
//                                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
//                            }
//                        }
//                    } catch (Exception e) {
//                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
//                    }
//                    break;
//                case PatchesIntent.ACTION_LAUNCHER:
//                    Toast.makeText(this, "action launcher", Toast.LENGTH_LONG).show();
//                    break;
//                case Intent.ACTION_SHUTDOWN:
//                    break;
//            }
//        }
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.N)
//    public void checkFunctionStates() {
//        checkDefaultLauncher(true);
//        runStartupApp();
//        runStartupScript();
//        checkNavigationBar();
//        checkAccessibilityDock();
//        checkRadioPlay();
//        checkWifiHotspot();
//        checkNavigationBar();
//        checkCollisionSystem();
//        checkLaneKeepAssistSystem();
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.N)
//    public void checkDefaultLauncher(boolean launch_it) {
////        filter.addCategory(Intent.CATEGORY_HOME);
////        filter.addCategory(Intent.CATEGORY_DEFAULT);
////// Set the activity as the preferred option for the device.
////        ComponentName activity = new ComponentName(context, KioskModeActivity.class);
////        DevicePolicyManager dpm =
////                (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
////
////        devicePolicyManager.clearPackagePersistentPreferredActivities(adminComponent, getPackageName());
////        dpm.addPersistentPreferredActivity(null, filter, activity);
////        usage: dpm [subcommand] [options]
////        usage: dpm set-device-owner <COMPONENT>
////                usage: dpm set-profile-owner <COMPONENT> <USER_ID>
////
////                dpm set-device-owner: Sets the given component as active admin, and its package as device owner.
////                dpm set-profile-owner: Sets the given component as active admin and profile owner for an existing user.
////
////                Runtime.getRuntime().exec("dpm set-device-owner com.foo.deviceowner/.DeviceAdminRcvr");
////        adb shell dpm set-device-owner "com.afwsamples.testdpc/.DeviceAdminReceiver"
////            google play???
////        https://source.android.com/docs/devices/admin/implement?hl=zh-cn
////        https://developer.android.com/work/device-admin
//        String systemPackage = getString(R.string.str_system_launcher_package);
//        String systemActivity = getString(R.string.str_system_launcher_activity);
//        String packageName = sharedPref.getString(getString(R.string.pref_default_launcher), systemPackage);
//        packageName = (packageName == null || packageName.isEmpty() || packageName.equals("-")) ? systemPackage : packageName ;
//
//        PackageManager pm = getPackageManager();
//        Intent intent1 = new Intent(Intent.ACTION_MAIN);
//        intent1.addCategory(Intent.CATEGORY_HOME);
//        intent1.addCategory(Intent.CATEGORY_DEFAULT);
//        intent1.setPackage(packageName);
//        List<ResolveInfo> lst = pm.queryIntentActivities(intent1, PackageManager.MATCH_DEFAULT_ONLY | PackageManager.MATCH_DISABLED_COMPONENTS);
//        if (lst.isEmpty()) {
//            String info = "Missing launcher: " + packageName;
//            Toast.makeText(this, info, Toast.LENGTH_LONG).show();
//            Log.e(TAG, info);
//            return;
//        }
//        ResolveInfo resolveInfo = lst.get(0);
//        String activityName = resolveInfo.activityInfo.name;
//
////        pm.setComponentEnabledSetting(new ComponentName(packageName, activityName), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
//        pm.setComponentEnabledSetting(new ComponentName(packageName, activityName), PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
//        setDefaultLauncher(packageName, activityName);
//        try {
//            runtime.exec("cmd package set-home-activity " + packageName + "/" + activityName);
//        } catch (Exception e) {
//            Log.e(TAG, "error: cmd package set-home-activity " + packageName + "/" + activityName);
//        }
//        //disable previous launcher here
//        if (!packageName.equals(systemPackage)) {
//            pm.setComponentEnabledSetting(new ComponentName(systemPackage, systemActivity), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
//        }
////        Intent current_intent = new Intent("android.intent.action.MAIN");
////        current_intent.addCategory("android.intent.category.HOME");
////        ResolveInfo current_launcher_info = pm.resolveActivity(current_intent, PackageManager.MATCH_DEFAULT_ONLY);
////        if (current_launcher_info != null) {
////            if (!packageName.equals(current_launcher_info.activityInfo.packageName)) {
////                pm.setComponentEnabledSetting(new ComponentName(current_launcher_info.activityInfo.packageName, current_launcher_info.activityInfo.name), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
////            }
////        }
//
////        IntentFilter filter = new IntentFilter();
////        filter.addAction("android.intent.action.MAIN");
////        filter.addCategory("android.intent.category.DEFAULT");
//
////        Log.d(TAG, "000000000000000" + preferred_launcher.toString());
////        ComponentName launcher_g6 = new ComponentName("com.android.launcher", "com.desaysv.hmi.launcher.Launcher2Activity");
////        Intent intent;
////        intent = pm.getLaunchIntentForPackage(preferred_launcher);
////        intent = intent != null ? intent : (new Intent()).setComponent(launcher_g6);
////        ComponentName new_launcher = intent.getComponent();
////        Log.d(TAG, "2222222222222222" + new_launcher.toString());
////        ComponentName[] components = new ComponentName[] {launcher_g6, new_launcher};
////        pm.clearPackagePreferredActivities("com.android.launcher");
////        pm.addPreferredActivity(filter, IntentFilter.MATCH_CATEGORY_EMPTY, components, new_launcher);
////        try {
////            Runtime.getRuntime().exec("cmd package set-home-activity " + new_launcher.getPackageName() + "/" + new_launcher.getClassName());
////        } catch (Exception e) {
////            //
////        }
//
////        Intent current_intent = new Intent("android.intent.action.MAIN");
////        current_intent.addCategory("android.intent.category.HOME");
////        String current_launcher = pm.resolveActivity(current_intent, PackageManager.MATCH_DEFAULT_ONLY).resolveInfo.packageName;
////        pm.setComponentEnabledSetting(new_launcher, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
//
//        if (launch_it) {
//            Log.d(TAG, "start launcher: " + resolveInfo.activityInfo.packageName);
////            Intent intent2 = new Intent();
////            intent2.setClassName(resolveInfo.activityInfo.packageName,
////                    resolveInfo.activityInfo.name);
////            intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////            startActivity(intent2);
//            Intent intent2 = new Intent(Intent.ACTION_MAIN);
//            intent2.addCategory(Intent.CATEGORY_HOME);
////            intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent2);
//        }
//    }
//
//    public void runStartupApp() {
//        String startup_app = sharedPref.getString(getString(R.string.pref_startup_app), "");
//        if (startup_app != null && !startup_app.isEmpty() && !startup_app.equals("-")) {
//            PackageManager pm = getPackageManager();
//            Intent intent = pm.getLaunchIntentForPackage(startup_app);
//            if (intent != null) {
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
//            }
//        }
//    }
//
//    public void runStartupScript() {
////        Toast.makeText(getApplicationContext(), "run startup script", Toast.LENGTH_LONG).show();
//        //com.teslacoilsw.launcher/com.teslacoilsw.launcher.NovaLauncher
////        PackageManager pm = getPackageManager();
////        IntentFilter filter = new IntentFilter();
////        filter.addAction("android.intent.action.MAIN");
////        filter.addCategory("android.intent.category.HOME");
////        filter.addCategory("android.intent.category.DEFAULT");
////        ComponentName component = new ComponentName("com.teslacoilsw.launcher", "com.teslacoilsw.launcher.NovaLauncher");
////        ComponentName[] components = new ComponentName[] {new ComponentName("com.android.launcher", "com.android.launcher.Launcher"), component};
////        pm.clearPackagePreferredActivities("com.android.launcher");
////        pm.addPreferredActivity(filter, IntentFilter.MATCH_CATEGORY_EMPTY, components, component);
//        String script = sharedPref.getString(getString(R.string.pref_startup_script), getString(R.string.pref_value_startup_script));
//        try {
//            BufferedReader br = new BufferedReader(new StringReader(script));
//            String command = "";
//            while ((command = br.readLine().trim()) != null) {
//                if (command.isEmpty() || command.startsWith("#") || command.startsWith("//")) continue;
//                try {
//                    runtime.exec(command);
//                } catch (Exception e) {
////                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            }
//        } catch (Exception e) {
//            //
//        }
////        SystemProperties.set(DEFAULT_HOME, );
//    }
//    
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        doActions(intent);
//        return super.onStartCommand(intent, flags, startId);
//    }
//
//    public void checkNavigationBar() {
//        boolean state = sharedPref.getBoolean(getString(R.string.pref_hide_navigation_bar), getResources().getBoolean(R.bool.pref_hide_navigation_bar));
//        if (state) {
//            toggleNavigationBar(PatchesIntent.FLAG_STATE_OFF);
//        } else {
//            toggleNavigationBar(PatchesIntent.FLAG_STATE_ON);
//        }
//    }
//
//    public void toggleNavigationBar(int state) {
//        String hideCommand = "settings put global policy_control immersive.full=apps,-com.desaysv.fotaui,-com.android.launcher";
//        String restoreCommand = "settings put global policy_control null*";
////        "adb shell settings put global policy_control immersive.navigation=*"
//        String command = state == 1 ? restoreCommand : hideCommand;
//        try {
//            runtime.exec(command);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public void checkLaneKeepAssistSystem() {
//        boolean state = sharedPref.getBoolean(getString(R.string.pref_lane_assist), getResources().getBoolean(R.bool.pref_lane_assist));
//        if (state) {
//            toggleLaneKeepAssistSystem(PatchesIntent.FLAG_STATE_OFF);
//        }
//    }
//
//    public void toggleLaneKeepAssistSystem(int state) {
//        if (this.carSettingsManager == null) {
//            this.carSettingsManager = new CarSettingsManager();
//        }
//        if (this.mCar == null) {
//            initCar();
//        }
//        int accState = getIgnitionState();
//        Log.d(TAG, "toggleLaneKeepAssistSystemValuea cc状态: " + accState);
//        if (accState != 4) {
//            Log.d(TAG, "toggleLaneKeepAssistSystemValue acc状态不满足: " + accState);
//            return;
//        }
//
//        int curState = this.carSettingsManager.getLaneKeepAssistSystemValue();
//        if (1 == curState && 0 == state) {
//            this.carSettingsManager.setLaneKeepAssistSystemValue(0);
//            Log.d(TAG, "关闭车道保持辅助");
//        } else if (0 == curState && 1 == state) {
//            this.carSettingsManager.setLaneKeepAssistSystemValue(1);
//            Log.d(TAG, "打开车道保持辅助");
//        } else if (2 == state) {
//            this.carSettingsManager.setLaneKeepAssistSystemValue(curState == 1 ? 0 : 1);
//            Log.d(TAG, "切换车道保持辅助为: " + (curState == 1 ? 0 : 1));
//        }
//    }
//
//    public void checkCollisionSystem() {
//        boolean state = sharedPref.getBoolean(getString(R.string.pref_turn_off_aeb), getResources().getBoolean(R.bool.pref_turn_off_aeb));
//        if (state) {
//            toggleCollisionSystem(PatchesIntent.FLAG_STATE_OFF);
//        }
//    }
//
//    public void toggleCollisionSystem(int state) {
//        H9PatchesService.this.mHandler.removeMessages(99);
//        new Handler().postDelayed(new Runnable() {
//            public void run() {
//                H9PatchesService.this._toggleCollisionSystem(state);
//            }
//        }, 5000);
//    }
//
//    private void _toggleCollisionSystem(int state) {
//        if (this.carSettingsManager == null) {
//            this.carSettingsManager = new CarSettingsManager();
//        }
//        if (this.mCar == null) {
//            initCar();
//        }
//        int accState = getIgnitionState();
//        if (accState != 4) {
//            Log.d(TAG, "_togglePreCollisionSystem acc状态不满足: " + accState);
////            return;
//        }
//        Log.d(TAG, "_togglePreCollisionSystem state: " + state);
//        int curState = this.carSettingsManager.getPreCollisionSystemValue(VehicleDoor.DOOR_HOOD);
//        if (1 == curState && 0 == state) {
//            this.carSettingsManager.setPreCollisionSystemValue(VehicleDoor.DOOR_HOOD, state);
//            Log.d(TAG, "关闭碰撞设置");
//        } else if (0 == curState && 1 == state) {
//            this.carSettingsManager.setPreCollisionSystemValue(VehicleDoor.DOOR_HOOD, state);
//            Log.d(TAG, "打开碰撞设置");
//        }
//    }
//
//    public void checkRadioPlay() {
//        boolean state = sharedPref.getBoolean(getString(R.string.pref_turn_off_radio), getResources().getBoolean(R.bool.pref_turn_off_radio));
//        if (state) {
//            turnOffRadio();
//        }
//    }
//
//    public void turnOffRadio() {
//        Log.d(TAG, "执行关闭收音机 ");
////        AudioManager am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
////        am.requestAudioFocus(focusChange -> {
////        }, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
////        Utils.requestAudioFocus();
////        Intent moniterServiceIntent = new Intent();
////        moniterServiceIntent.setComponent(new ComponentName("com.desay_svautomotive.svradio", "com.desay_svautomotive.svradio.service.RadioService"));
////        moniterServiceIntent.setAction("pause");
//
//
////        IBinder binder = ServiceManager.getService("radio");
////        IRadioManager radioService = IRadioManager.Stub.asInterface(binder);
////        try {
////            radioService.close();
////        } catch (RemoteException e) {
////            throw new RuntimeException(e);
////        }
////        ComponentName raidoService = new ComponentName("com.desay_svautomotive.svradio", "com.desay_svautomotive.svradio.service.RadioService");
////        startService(new Intent().setComponent(raidoService).setAction("pause"));
//
////        android.content.Intent intent = new android.content.Intent(this, "android.view.IKeyPolicy");
////        bindService(intent, mServerConnection, Context.BIND_AUTO_CREATE);
//
//
////        new Handler().postDelayed(new Runnable() {
////            public void run() {
////                Intent intent = new Intent("pause");
////                String packageName = "com.desay_svautomotive.svradio";
////                String className = "com.desay_svautomotive.svradio.service.RadioService";
////                intent.setClassName(packageName, className);
////                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
////                    startForegroundService(intent);
////                } else {
////                    startService(intent);
////                }
////            }
////        }, 1000);
//    }
//
//
////    private boolean checkDrawOverlayPermission() {
////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
////            /** check if we already  have permission to draw over other apps */
////            if (!Settings.canDrawOverlays(this)) {
////                /** if not construct intent to request permission */
////                android.content.Intent intent = new android.content.Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
////                        Uri.parse("package:" + getPackageName()));
////                /** request permission via start activity for result */
////                startActivity(intent, 1);
////                return false;
////            } else {
////            }
////        }
////        return true;
////    }
//
//    public void checkAccessibilityDock() {
//        boolean state = sharedPref.getBoolean(getString(R.string.pref_accessibility_dock), getResources().getBoolean(R.bool.pref_accessibility_dock));
//        toggleAccessibilityDock(state ? 1 : 0);
//    }
//
//    @SuppressLint("ClickableViewAccessibility")
//    private void toggleAccessibilityDock(int state) {
//        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
//        if (state != 1 && floatingButton != null) {
//            windowManager.removeView(floatingButton);
//            floatingButton = null;
//            return;
//        }
//        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
//        floatingButton = (ImageButton) layoutInflater.inflate(R.layout.button_floating, null);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (Settings.canDrawOverlays(this)) {
//                ImageButton button = floatingButton; //new ImageButton(getApplicationContext());
////                button.setAlpha(0.5f);
//                WindowManager.LayoutParams params = new WindowManager.LayoutParams();
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
//                    params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
//                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
//                            | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
//                            | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
//                    ;
//                } else {
//                    params.type = WindowManager.LayoutParams.TYPE_PHONE;
//                }
//                params.format = PixelFormat.RGBA_8888;
//                params.gravity = Gravity.TOP | Gravity.START;
////                params.horizontalMargin = 10f;
////                params.verticalMargin = 10f;
//                DisplayMetrics displayMetrics = new DisplayMetrics();
//                windowManager.getDefaultDisplay().getMetrics(displayMetrics);
//                params.x = 10;
//                params.y = displayMetrics.heightPixels + 24;
//                params.width  = 80;
//                params.height = 80;
//                windowManager.addView(button, params);
//                button.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        try {
//                            long now = SystemClock.uptimeMillis();
//                            inputManager.injectInputEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK, 0), 0);
//                            inputManager.injectInputEvent(new KeyEvent(now, now, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK, 0), 0);
//                        } catch (Exception ignored) {
//                        }
//                    }
//                });
//                button.setOnLongClickListener(new View.OnLongClickListener() {
//                    @Override
//                    public boolean onLongClick(View view) {
//                        try {
//                            inputManager.injectInputEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HOME), 0);
//                            inputManager.injectInputEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HOME), 0);
//                        } catch (RemoteException ignored) {}
//                        return true;
//                    }
//                });
//                button.setOnTouchListener(new View.OnTouchListener() {
//                    private int initialX;
//                    private int initialY;
//                    private float initialTouchX;
//                    private float initialTouchY;
//                    @Override
//                    public boolean onTouch(View v, MotionEvent event) {
//                        switch (event.getAction()) {
//                            case MotionEvent.ACTION_DOWN:
//                                initialX = params.x;
//                                initialY = params.y;
//                                initialTouchX = event.getRawX();
//                                initialTouchY = event.getRawY();
//                                break;
//                            case MotionEvent.ACTION_MOVE:
//                                params.x = initialX + (int) (event.getRawX() - initialTouchX);
//                                params.y = initialY + (int) (event.getRawY() - initialTouchY);
//                                windowManager.updateViewLayout(button, params);
//                                break;
//                            case MotionEvent.ACTION_UP:
//                                if (Math.abs(event.getRawX() - initialTouchX) < 30 && Math.abs(event.getRawY() - initialTouchY) < 30) {
//                                    params.x = initialX;
//                                    params.y = initialY;
//                                    windowManager.updateViewLayout(button, params);
//                                }
//                                return params.x != initialX || params.y != initialY;
//                        }
//                        return false;
//                    }
//                });
//            }
//        }
//    }
//
//    private String getTopActivityClassName() {
//        String topActivityClass = null;
//        ActivityManager activityManager = (ActivityManager) (getSystemService(Context.ACTIVITY_SERVICE));
//        try {
//            List<ActivityManager.RunningTaskInfo> runningTaskInfos = activityManager.getRunningTasks(1);
//            if (runningTaskInfos != null && !runningTaskInfos.isEmpty()) {
//                ComponentName f = runningTaskInfos.get(0).topActivity;
//                topActivityClass = f.getClassName();
//                Toast.makeText(this, "Top activity: " + topActivityClass, Toast.LENGTH_SHORT).show();
//            }
//        } catch (Exception ignored) {
//        }
//        return topActivityClass;
//    }
//
//    private void injectEvent(InputEvent event) {
//        new Thread(new Runnable() {
//            public void run() {
//                try {
//                    inputManager.injectInputEvent(event, 0);
//                } catch (RemoteException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        }).start();
//    }
//
//
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    public void setForegroundService() {
//        Notification notification = new NotificationCompat.Builder(this).build();
//        startForeground(ONGOING_NOTIFICATION_ID, notification);
//    }
//
//    public void checkWifiHotspot() {
//        boolean state = sharedPref.getBoolean(getString(R.string.pref_turn_on_wifi_spot), getResources().getBoolean(R.bool.pref_turn_on_wifi_spot));
//        toggleWifiHotspot(state ? PatchesIntent.FLAG_STATE_ON : PatchesIntent.FLAG_STATE_OFF );
//    }
//
//    private void toggleWifiHotspot(int state) {
//        try {
//            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//            if (state == 0) {
//                WifiApUtils.closeWifiAP();
//            } else if (state == 1) {
//                //获取wifi开关状态
//                if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
//                    //wifi打开状态则关闭
//                    wifiManager.setWifiEnabled(false);
//                }
//                WifiApUtils.openWifiAP();
//            }
//        } catch (Exception e) {
//            Log.e(TAG, "error open hotspot");
//        }
//    }
//
//
//    private Set<ComponentName> getEnabledServices(Context context) {
//        final String enabledServicesSetting = Settings.Secure.getString(
//                context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
//        if (enabledServicesSetting == null) {
//            return Collections.emptySet();
//        }
//        final Set<ComponentName> enabledServices = new HashSet<>();
//        final TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter(':');
//        colonSplitter.setString(enabledServicesSetting);
//
//        while (colonSplitter.hasNext()) {
//            final String componentNameString = colonSplitter.next();
//            final ComponentName enabledService = ComponentName.unflattenFromString(
//                    componentNameString);
//            if (enabledService != null) {
//                enabledServices.add(enabledService);
//            }
//        }
//        return enabledServices;
//    }
//
//    private void enableAccessibility(Context context) {
//        try {
//            Set<ComponentName> enabledServices = getEnabledServices(context);
//            if (enabledServices == (Set<?>) Collections.emptySet()) {
//                enabledServices = new HashSet<ComponentName>();
//            }
//            ComponentName toggledService = ComponentName.unflattenFromString("包名/类名");
//            enabledServices.add(toggledService);
//            StringBuilder enabledServicesBuilder = new StringBuilder();
//            for (ComponentName enabledService : enabledServices) {
//                enabledServicesBuilder.append(enabledService.flattenToString());
//                enabledServicesBuilder.append(':');
//            }
//            final int enabledServicesBuilderLength = enabledServicesBuilder.length();
//            if (enabledServicesBuilderLength > 0) {
//                enabledServicesBuilder.deleteCharAt(enabledServicesBuilderLength - 1);
//            }
//            android.provider.Settings.Secure.putString(context.getContentResolver(),
//                    android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, enabledServicesBuilder.toString());
//            android.provider.Settings.Secure.putInt(context.getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, 1);
//        } catch (Exception e) {
//            //
//        }
//    }
//
//    public void initCar() {
////        Toast.makeText(getApplicationContext(),"init car", Toast.LENGTH_LONG).show();
//        mCar = Car.createCar(this, new ServiceConnection() {
//            public void onServiceConnected(ComponentName name, IBinder service) {
//                try {
////                    H9PatchesService.this.mCarInputManager =
////                            (CarInputManager) mCar.getCarManager()
//                    CarSensorManager unused = H9PatchesService.this.mCarSensorManager = (CarSensorManager) H9PatchesService.this.mCar.getCarManager(android.support.car.Car.SENSOR_SERVICE);//android.support.car.Car.SENSOR_SERVICE);
//                    H9PatchesService.this.mCarSensorManager.registerListener(H9PatchesService.this.ignitionStateListener, 22, 0);
//                } catch (CarNotConnectedException e) {
////                    e.printStackTrace();
//                }
//            }
//
//            public void onServiceDisconnected(ComponentName name) {
//                if (H9PatchesService.this.mCarSensorManager != null) {
//                    H9PatchesService.this.mCarSensorManager.unregisterListener(H9PatchesService.this.ignitionStateListener);
//                }
//            }
//        });
//        this.mCar.connect();
//    }
//
//    public OnCarSensorListener.Stub carSensorListener = new OnCarSensorListener.Stub() {
//        public void onChangeEvent(int id, int zone, int value) {
//            Log.d(TAG, "car sensor listener: " + id + ":" + zone + ":" + value);
//            if (id == 10) {
//                H9PatchesService.this.mHandler.removeMessages(99);
//                Log.d(TAG, "发动机状态改变:10");
//                H9PatchesService.this.checkCollisionSystem();
//            }
//        }
//    };
//
//    CarSensorManager.OnSensorChangedListener ignitionStateListener = new CarSensorManager.OnSensorChangedListener() {
//        public void onSensorChanged(CarSensorEvent carSensorEvent) {
//            Log.d(TAG, "ignitionStateListener:" + carSensorEvent.intValues[0]);
//            int state = carSensorEvent.intValues[0];
//            if (H9PatchesService.this.ignitionState == 5 && state == 4) {
//                H9PatchesService.this.mHandler.removeMessages(99);
//                H9PatchesService.this.checkRadioPlay();
//                H9PatchesService.this.checkCollisionSystem();
//            } else if (H9PatchesService.this.ignitionState == 2 && state == 3) {
//                H9PatchesService.this.checkRadioPlay();
//            }
//            H9PatchesService.this.ignitionState = state;
//        }
//    };
//
//    @SuppressLint({"HandlerLeak"})
//    public Handler mHandler = new Handler() {
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            if (msg.what == 99) {
//                if (!isCarSensorReady) {
//                    isCarSensorReady = H9PatchesService.this.carSettingsManager.isCarSensorReady();
//                    if (isCarSensorReady) {
//                        H9PatchesService.this.carSettingsManager.registerCarSensorCallback(H9PatchesService.this.carSensorListener);
//                    }
//                }
//
//                if (!H9PatchesService.this.carSettingsManager.isCarSensorReady() || !H9PatchesService.this.carSettingsManager.isDriverDailyAssistReady() || !H9PatchesService.this.carSettingsManager.isDriverAssistReady() || !H9PatchesService.this.carSettingsManager.isReverseAssistReady() || !H9PatchesService.this.carSettingsManager.isCarEnergyReady() || !CarSettingsApplication.get().isMoreTwoSecond().booleanValue()) {
//                    removeMessages(99);
//                    sendEmptyMessageDelayed(99, 1000);
//                } else {
//                    removeMessages(99);
//                    //H9PatchesService.this.disablePreCollisionSystem();
//                }
//            } else if (msg.what == 101) {
//
//                mHandler.removeMessages(101);
//                mHandler.sendEmptyMessageDelayed(101, 1000 * 60 * 5);
//            }
//        }
//    };
//
//    public void checkIgnitionState() {
//        try {
//            int accState = getIgnitionState();
//            Log.d(TAG, "checkIgnitionState: " + accState);
//            if (accState == 2 || accState == 3) {
////                return true;
//            }
//        } catch (Exception e) {
////            e.printStackTrace();
//        }
//    }
//
//    public int getIgnitionState() {
//        try {
//            if (this.mCarSensorManager != null) {
//                return this.mCarSensorManager.getLatestSensorEvent(22).intValues[0];
//            }
//        } catch (CarNotConnectedException | NullPointerException e) {
////            e.printStackTrace();
//        }
//        return 2;
//    }
//
//    //add
//    private void setDefaultLauncher(String packageName, String className) {
//        try {
//            final PackageManager pm = getPackageManager();
//
//            IntentFilter filter = new IntentFilter();
//            filter.addAction("android.intent.action.MAIN");
//            filter.addCategory("android.intent.category.HOME");
//            filter.addCategory("android.intent.category.DEFAULT");
//
//            Intent intent = new Intent(Intent.ACTION_MAIN);
//            intent.addCategory(Intent.CATEGORY_HOME);
//            List<ResolveInfo> lst = pm.queryIntentActivities(intent, 0);
//            final int N = lst.size();
//            ComponentName[] components = new ComponentName[N];
//            int bestMatch = 0;
//            for (int i = 0; i < N; i++) {
//                ResolveInfo r = lst.get(i);
//                components[i] = new ComponentName(r.activityInfo.packageName, r.activityInfo.name);
//                if (r.match > bestMatch) bestMatch = r.match;
//            }
//            ComponentName preActivity = new ComponentName(packageName, className);
////            pm.clearPackagePreferredActivities("com.android.launcher");
//            pm.addPreferredActivity(filter, bestMatch, components, preActivity);
//
//        } catch (Exception e) {
////            e.printStackTrace();
//        }
//    }
//
//    private String getLaunchActivityForPackage(String packageName){
//        PackageManager pm = getPackageManager();
//        Intent intentToResolve = new Intent(Intent.ACTION_MAIN);
//        intentToResolve.addCategory(Intent.CATEGORY_INFO);
//        intentToResolve.setPackage(packageName);
//        List<ResolveInfo> ris = pm.queryIntentActivities(intentToResolve, 0);
//        // Otherwise, try to find a main launcher activity.
//        if (ris == null || ris.size() <= 0) {
//            // reuse the intent instance
//            intentToResolve.removeCategory(Intent.CATEGORY_INFO);
//            intentToResolve.addCategory(Intent.CATEGORY_LAUNCHER);
//            intentToResolve.setPackage(packageName);
//            ris = pm.queryIntentActivities(intentToResolve, 0);
//        }
//        if (ris == null || ris.size() <= 0) {
//            return null;
//        }
//        return ris.get(0).activityInfo.name;
//    }
//
////    public int getConfigValue(String name) {
////        Cursor cursor = getContentResolver().query(Uri.parse("content://com.desay_svautomotive.provider.DesayProvider/config"), null, "name=?", new String[]{name}, null);
////        int value = -1;
////        if (cursor != null) {
////            while (cursor.moveToNext()) {
////                value = cursor.getInt(cursor.getColumnIndex("value"));
////                Log.d(TAG, "query: name - " + cursor.getString(cursor.getColumnIndex("name")));
////                Log.d(TAG, "query: value - " + value);
////            }
////            cursor.close();
////        } else {
////            Log.e(TAG, "query: cursor == null");
////        }
////        this.mConfiger = value;
////        return value;
////    }
//
//    public IInputManager getInputManager() {
//        return inputManager;
//    }
//
//}

package com.spark.h9patches;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.LinkedHashMap;
import java.util.Objects;

public class H9PatchesService extends Service {
    private final int ONGOING_NOTIFICATION_ID = 1;
    private final static String TAG = H9PatchesService.class.getName();
    static H9PatchesService instance;
    private final LinkedHashMap<Class<?>, H9PatchesService.IFacility> mFacilities = new LinkedHashMap<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        createFacilities();
        instance = this;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        setForegroundService();
        new Handler(Looper.getMainLooper()).postDelayed(H9PatchesService.this::notifyOnStart, 5000);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        notifyOnStop();
        super.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createFacilities() {
        ActivityWatcher watcher = new ActivityWatcher(this);
        addFacility(watcher);
        addFacility(new Launcher(this, watcher));
        addFacility(new NavigationBar(this));
        addFacility(new NavigationButton(this));
        addFacility(new KeyPolicy(this, watcher));
        addFacility(new DPadSimulator(this, watcher));
        addFacility(new LaneKeepAssist(this));
        addFacility(new PreCollisionSystem(this));
        addFacility(new Wifi(this));
        addFacility(new ActionReceiver(this));
        addFacility(new Radio(this));
        addFacility(new StartupScript(this));
//        addFacility(new Accounts(this));
//        notifyOnStart();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setForegroundService() {
//        Notification notification = new NotificationCompat.Builder(this).build();
//        startForeground(ONGOING_NOTIFICATION_ID, notification);
        NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel serviceChannel = new NotificationChannel(
                getString(R.string.key_notification_channel),
                getString(R.string.text_notification_title),
                NotificationManager.IMPORTANCE_HIGH);
        notifManager.createNotificationChannel(serviceChannel);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification =
                new Notification.Builder(this, getString(R.string.key_notification_channel))
                        .setContentTitle(getString(R.string.text_notification_title))
                        .setContentText(getString(R.string.text_notification_message))
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentIntent(pendingIntent)
                        .setTicker(getString(R.string.text_ticker_tips))
                        .build();

        startForeground(ONGOING_NOTIFICATION_ID, notification);
//        Toast.makeText(H9PatchesService.this.getApplicationContext(),"Patches Service started",Toast.LENGTH_LONG).show();
    }

    public void notifyOnStart() {
        Log.d(TAG, "notifyOnStart");
        for (H9PatchesService.IFacility facility : mFacilities.values()) {
            Log.d(TAG, "start facility " + facility.getClass().getSimpleName());
            try {
                facility.onServiceStart();
            } catch (Exception e) {
                Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            }
        }
    }
    public void notifyOnStop() {
        Log.d(TAG, "notifyOnStart");
        for (H9PatchesService.IFacility facility : mFacilities.values()) {
            try {
                facility.onServiceStop();
            } catch (Exception e) {
                Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            }
        }
    }

    private void addFacility(H9PatchesService.IFacility facility) {
        mFacilities.put(facility.getClass(), facility);
    }

    public H9PatchesService.IFacility getFacility(Class<?> cls) {
        return mFacilities.get(cls);
    }

    public interface IFacility {
        void onServiceStart();
        void onServiceStop();
    }

    public static H9PatchesService getInstance() {
        return instance;
    }


}
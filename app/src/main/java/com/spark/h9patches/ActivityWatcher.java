package com.spark.h9patches;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ActivityWatcher extends ServiceFacility {
    String currentTopPackageName = "";
    boolean needsTouchAccessibility = false;
    Timer mTimer;

    public ActivityWatcher(Context context) {
        super(context);
        instance = this;
    }

    @Override
    public void onServiceStart() {
        watchTopActivity();
    }

    @SuppressLint("StaticFieldLeak")
    static ActivityWatcher instance = null;

    Handler handler = new Handler(msg -> {
        if(msg.arg1==1) {
            onTopActivityChanged(currentTopPackageName);
        }
        return false;
    });
    private void watchTopActivity() {
        if (mTimer == null) {
            mTimer = new Timer();
            TimerTask mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    try {
                        String packageName = getTopActivityPackageName();
                        if (!currentTopPackageName.equals(packageName)) {
                            currentTopPackageName = packageName;
                            Message msg = new Message();
                            msg.arg1 = 1;
                            handler.sendMessage(msg);
                        }
                    } catch (Exception ignored) {
                    }
                }
            };
            mTimer.schedule(mTimerTask, 0,1000);
        }
    }

    private void onTopActivityChanged(String packageName) {
        for (OnTopActivityChangedListener callback : callbacks) {
            callback.onTopActivityChanged(packageName);
        }
    }

    public String getTopActivityPackageName() {
        String topActivityPackageName = null;
        ActivityManager activityManager = (ActivityManager) (getContext().getSystemService(Context.ACTIVITY_SERVICE));
        try {
            List<ActivityManager.RunningTaskInfo> runningTaskInfos = activityManager.getRunningTasks(1);
            if (runningTaskInfos != null && !runningTaskInfos.isEmpty()) {
                ComponentName f = runningTaskInfos.get(0).topActivity;
                if (f != null) {
                    topActivityPackageName = f.getPackageName();
                }
            }
        } catch (Exception ignored) {
        }
        return topActivityPackageName;
    }


    List<OnTopActivityChangedListener> callbacks = new ArrayList<>();
    public void registerOnTopActivityChanged(OnTopActivityChangedListener listener) {
        callbacks.add(listener);
    }

    public void unRegisterTopActivityChangedListener(OnTopActivityChangedListener listener) {
        callbacks.remove(listener);
    }

    public static boolean isTvApp(String packageName) {
        return "com.amazon.firetv.youtube".equals(packageName)
                || "com.teamsmart.videomanager.tv".equals(packageName);
    }

    public interface OnTopActivityChangedListener {
        public void onTopActivityChanged(String packageName);
    }

}

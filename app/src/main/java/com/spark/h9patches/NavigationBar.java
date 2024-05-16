package com.spark.h9patches;

import android.content.Context;
import android.widget.Toast;

import java.io.IOException;

public class NavigationBar extends ServiceFacility {
    public NavigationBar(Context context) {
        super(context);
    }
    String SYSTEM_LAUNCHER_PACKAGE_NAME = "com.android.launcher";

    @Override
    public void onServiceStart() {
        checkNavigationBar();
    }

    @Override
    public void onSharedPreferenceChanged(String key) {
        if (key.equals(getString(R.string.pref_hide_navigation_bar))) {
            checkNavigationBar();
        }
    }

    private void checkNavigationBar() {
        boolean prefValue = sharedPreferences.getBoolean(getString(R.string.pref_hide_navigation_bar), getResourcesBoolean(R.bool.pref_hide_navigation_bar));
        toggleNavigationBar(prefValue ? 0 : 1);
    }

    public void toggleNavigationBar(int state) {
        String launcherPackageName = sharedPreferences.getString(getString(R.string.pref_default_launcher), SYSTEM_LAUNCHER_PACKAGE_NAME);
        String[] hideCommands = {
                "settings put global policy_control immersive.navigation=*",
                "wm overscan 0,0,0,-116",
                "settings put global policy_control immersive.status=*,-" + launcherPackageName
        };
        String[] restoreCommands = {
                "settings put global policy_control null*",
                "wm overscan 0,0,0,0"
        };
//        "adb shell settings put global policy_control immersive.navigation=*"
        String[] commands = state == 1 || SYSTEM_LAUNCHER_PACKAGE_NAME.equals(launcherPackageName) ? restoreCommands : hideCommands;
        for (String command : commands) {
            try {
                Runtime.getRuntime().exec(command);
            } catch (Exception ignored) {
//                Toast.makeText(getContext(), ignored.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}

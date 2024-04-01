package com.spark.h9patches;

public class Intent extends android.content.Intent {
    public static final String ACTION_TURN_OFF_PRE_COLLISION_SYSTEM = "h9patches.intent.action.DISABLE_PRE_COLLISION_SYSTEM";
    public static final String ACTION_TOGGLE_LANE_KEEP_ASSIST_SYSTEM = "h9patches.intent.action.LANE_KEEP_ASSIST_SYSTEM";
    public static final String ACTION_TURN_OFF_RADIO_ON_STARTUP = "h9patches.intent.action.DISABLE_STARTUP_RADIO";
    public static final String ACTION_WIFI_HOTSPOT = "h9patches.intent.action.ENABLE_WIFI_HOTSPOT";
    public static final String ACTION_HIDE_NAVIGATION_BAR = "h9patches.intent.action.DISABLE_NAVIGATION_BAR";
    public static final String ACTION_COMMAND = "h9patches.intent.action.COMMAND";
    public static final String ACTION_LAUNCHER = "h9patches.intent.action.LAUNCHER";

    public static final String EXTRA_FLAG_STATE = "state";

    public static final int FLAG_STATE_OFF = 0;
    public static final int FLAG_STATE_ON = 1;
    public static final int FLAG_STATE_TOGGLE = 2;
}

package com.spark.h9patches;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ActionReceiver extends ServiceFacility {
    public ActionReceiver(Context context) {
        super(context);
    }

    @Override
    public void onServiceStart() {
        registerReceiver();
    }

    @Override
    public void onServiceStop() {
        if (mReceiver != null) {
            getContext().unregisterReceiver(mReceiver);
        }
    }

    BroadcastReceiver mReceiver;
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void registerReceiver() {
        if (mReceiver == null) {
            mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    ActionReceiver.this.doActions(intent);
                }
            };
            IntentFilter filter = getFilter();
            getContext().registerReceiver(mReceiver, filter);
        }
    }

    private static IntentFilter getFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(PatchesIntent.ACTION_COMMAND);
        filter.addAction(PatchesIntent.ACTION_HIDE_NAVIGATION_BAR);
        filter.addAction(PatchesIntent.ACTION_WIFI_HOTSPOT);
        filter.addAction(PatchesIntent.ACTION_TOGGLE_LANE_KEEP_ASSIST_SYSTEM);
        filter.addAction(PatchesIntent.ACTION_TURN_OFF_PRE_COLLISION_SYSTEM);
        filter.addAction(PatchesIntent.ACTION_LAUNCHER);
        filter.addAction(Intent.ACTION_SHUTDOWN);
        return filter;
    }

    private void doActions(Intent intent) {
        if (intent == null) {
            return;
        }
        Log.d(TAG, "receive intent:" + intent.getAction());
        String action = intent.getAction();
        H9PatchesService service = (H9PatchesService) getContext();
        if (action != null) {
            int state = intent.getIntExtra(PatchesIntent.EXTRA_FLAG_STATE, 1);
            switch (action) {
                case PatchesIntent.ACTION_TURN_OFF_PRE_COLLISION_SYSTEM:
                    ((PreCollisionSystem)service.getFacility(PreCollisionSystem.class)).toggleCollisionSystem(state == 1 ? 0 : 1);
                    break;
                case PatchesIntent.ACTION_TOGGLE_LANE_KEEP_ASSIST_SYSTEM:
                    ((LaneKeepAssist)service.getFacility(LaneKeepAssist.class)).toggleLaneKeepAssistSystem(state);
                    break;
                case PatchesIntent.ACTION_WIFI_HOTSPOT:
                    ((Wifi)service.getFacility(Wifi.class)).toggleWifiHotspot(state);
                    break;
                case PatchesIntent.ACTION_HIDE_NAVIGATION_BAR:
                    ((NavigationBar)service.getFacility(NavigationBar.class)).toggleNavigationBar(state == 1 ? 0 : 1);
                    break;
                case PatchesIntent.ACTION_COMMAND:
                    try {
                        String command = intent.getStringExtra("cmd");
                        Toast.makeText(service, command, Toast.LENGTH_LONG).show();
                        if (command != null && !command.isEmpty()) {
                            Process proc = Runtime.getRuntime().exec(command);
                            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                            BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                            StringBuilder msg = new StringBuilder();
                            String s = null;
                            while ((s = stdInput.readLine()) != null) {
                                msg.append(s).append("\n");
                            }
                            if (!msg.toString().trim().isEmpty()) {
                                Toast.makeText(service, msg, Toast.LENGTH_LONG).show();
                                Log.d(TAG, String.valueOf(msg));
                            }
                            msg = new StringBuilder();
                            while ((s = stdError.readLine()) != null) {
                                msg.append(s).append("\n");
                            }
                            if (!msg.toString().trim().isEmpty()) {
                                Toast.makeText(service, msg, Toast.LENGTH_LONG).show();
                                Log.d(TAG, String.valueOf(msg));
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(service, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    break;
                case Intent.ACTION_SHUTDOWN:
                    break;
            }
        }
    }
}

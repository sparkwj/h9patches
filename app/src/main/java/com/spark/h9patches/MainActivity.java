package com.spark.h9patches;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemProperties;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.Settings;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private int a = 0;
    private int b = 0;
    private int clicks = 0;

    Switch swAEB_Veh;
    Switch swRADIO;
    Switch swLANE;
    Switch swHOTSPOT;
    SeekBar sbNIGHTBRIGHT;
    TextView txtH9PatchesTitle;
    TextView textView2;

    SharedPreferences sharedPref;

    private String developtag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "MainActivity started!");

        this.developtag = SystemProperties.get("persist.sv.debug.adb_enable");
        sharedPref = H9PatchesApplication.getSharedPref();
        initViews();

//        Log.d(TAG, "Utils.getScreenBrightness: " + Utils.getSystemBrightness());
//        Utils.setSystemBrightness(5);
//        Utils.stopAutoBrightness();
//        Utils.setSystemBrightness(5);
//        Log.d(TAG, "Utils.getScreenBrightness: " + Utils.getSystemBrightness());
//        Utils.requestAudioFocus();

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(new Intent(this, H9PatchesService.class));
//            startForegroundService(new Intent(this, H9PatchesKeysService.class));
//        } else {
//            startService(new Intent(this, H9PatchesService.class));
//            startService(new Intent(this, H9PatchesKeysService.class));
//        }
    }

    private void initViews() {
        this.swAEB_Veh = findViewById(R.id.swAEB);
        this.swAEB_Veh.setChecked(sharedPref.getBoolean(getString(R.string.preference_saved_AEB_key), false));
        this.swAEB_Veh.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(getString(R.string.preference_saved_AEB_key), b);
                editor.commit();
            }
        });

        this.swRADIO = findViewById(R.id.swRADIO);
        this.swRADIO.setChecked(sharedPref.getBoolean(getString(R.string.preference_saved_RADIO_key), false));
        this.swRADIO.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(getString(R.string.preference_saved_RADIO_key), b);
                editor.commit();
            }
        });
        this.swLANE = findViewById(R.id.swLANE);
        this.swLANE.setChecked(sharedPref.getBoolean(getString(R.string.preference_saved_LANE_key), false));
        this.swLANE.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(getString(R.string.preference_saved_LANE_key), b);
                editor.commit();
            }
        });
        this.swHOTSPOT = findViewById(R.id.swHOTSPOT);
        this.swHOTSPOT.setChecked(sharedPref.getBoolean(getString(R.string.preference_saved_HOTSPOT_key), false));
        this.swHOTSPOT.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(getString(R.string.preference_saved_HOTSPOT_key), b);
                editor.commit();
                if (b) {
                    Utils.openWifiHotspot();
                }
            }
        });
        this.txtH9PatchesTitle = findViewById(R.id.txtH9PatchesTitle);
        this.txtH9PatchesTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {//codes copy from dsv
                if (MainActivity.this.developtag.equals("0") || MainActivity.this.developtag.equals("")) {
                    MainActivity.this.a++;
                    if (MainActivity.this.a == 6) {
                        Toast toast=Toast.makeText(MainActivity.this, getString(R.string.txt_adb_enabled), Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        SystemProperties.set("persist.sv.debug.adb_enable", "1");
                        MainActivity.this.a = 0;
                        MainActivity.this.developtag = "1";
                    }
                } else if (MainActivity.this.developtag.equals("1")) {
                    MainActivity.this.b++;
                    if (MainActivity.this.b == 3) {
                        Toast toast=Toast.makeText(MainActivity.this, getString(R.string.txt_adb_disabled), Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        SystemProperties.set("persist.sv.debug.adb_enable", "0");
                        MainActivity.this.b = 0;
                        MainActivity.this.developtag = "0";
                    }
                }
            }
        });
        this.textView2 = findViewById(R.id.textView2);
        this.textView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.clicks++;
                if (MainActivity.this.clicks == 3) {
                    MainActivity.this.clicks = 0;
                    Intent it = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
                    startActivity(it);
                }
            }
        });
    }
}
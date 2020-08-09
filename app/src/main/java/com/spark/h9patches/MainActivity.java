package com.spark.h9patches;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    Switch swAEB_Veh;
    Switch swRADIO;
    Switch swLANE;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "MainActivity started!");

        sharedPref = H9PatchesApplication.getSharedPref();
        initViews();

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
    }
}
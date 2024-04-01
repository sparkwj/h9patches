package com.spark.h9patches;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, AdapterView.OnItemSelectedListener {

    private static final String TAG = "MainActivity";
    private LinearLayout layoutScriptEditor;
    private EditText scriptEditor;
    private final SharedPreferences sharedPref = H9PatchesApplication.getSharedPref();
    private H9PatchesService h9PatchesService;
    private boolean isServiceBound = false;
    LinkedHashMap<String, String> launcher_packages = new LinkedHashMap<>();
    LinkedHashMap<String, String> installed_packages = new LinkedHashMap<>();

    ServiceConnection mServerConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            h9PatchesService = ((H9PatchesService.H9PatchesServiceBinder) iBinder).getPatchesService();
            isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isServiceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onStart() {
        super.onStart();
        android.content.Intent intent = new android.content.Intent(this, H9PatchesService.class);
        bindService(intent, mServerConnection, Context.BIND_AUTO_CREATE);
        initViews();
    }

    @Override
    protected void onStop() {
        if (isServiceBound) {
            unbindService(mServerConnection);
            isServiceBound = false;
        }
        super.onStop();
    }

    private List<View> getAllChildViews(View view) {
        List<View> children = new ArrayList<>();
        if (view instanceof ViewGroup) {
            ViewGroup vp = (ViewGroup) view;
            for (int i = 0; i < vp.getChildCount(); i++) {
                View child = vp.getChildAt(i);
                children.add(child);
                children.addAll(getAllChildViews(child));
            }
        }
        return children;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initViews() {
        init_switches();
        init_launcher_spinner();
        init_startup_app_spinner();
        init_script_editor();
    }

    private void init_script_editor() {
        ImageButton btnEditScript = findViewById(R.id.btnEditScript);
        this.layoutScriptEditor = findViewById(R.id.layoutScriptEditor);
        this.scriptEditor = findViewById(R.id.scriptEditor);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnRun = findViewById(R.id.btnRun);
        Button btnCancel = findViewById(R.id.btnCancel);

        String script = sharedPref.getString(getString(R.string.pref_startup_script), getString(R.string.pref_value_startup_script));
        scriptEditor.setText(script);
        btnEditScript.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.layoutScriptEditor.setVisibility(View.VISIBLE);
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String script = String.valueOf(MainActivity.this.scriptEditor.getText()).trim();
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(getString(R.string.pref_startup_script), script);
                editor.apply();
                MainActivity.this.layoutScriptEditor.setVisibility(View.GONE);
            }
        });
        btnRun.setOnClickListener(view -> {
            String script1 = String.valueOf(MainActivity.this.scriptEditor.getText());
            try {
                BufferedReader br = new BufferedReader(new StringReader(script1));
                String command = "";
                Runtime runtime = Runtime.getRuntime();
                while ((command = br.readLine()) != null) {
                    if (command.isEmpty() || command.startsWith("#") || command.startsWith("//")) continue;
                    try {
                        Process process = runtime.exec(command);
                        BufferedReader bufferedReader = new BufferedReader(
                                new InputStreamReader(process.getInputStream()));
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            Log.i(TAG, "H9PatchesCommand info: " + line);
                        }
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
                Toast.makeText(MainActivity.this, "Done!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                //
            }
        });
        btnCancel.setOnClickListener(view -> MainActivity.this.layoutScriptEditor.setVisibility(View.GONE));
    }

    private void init_switches() {
        List<View> children = getAllChildViews(this.getWindow().getDecorView());
        for (View v : children) {
            if (v instanceof Switch) {
                Switch sw = (Switch)v;
//                try {
                int pref_key_Id = this.getResources().getIdentifier(sw.getTag().toString(), "string", this.getPackageName());
                String pref_key = getString(pref_key_Id);
                int resourceId = this.getResources().getIdentifier(pref_key, "bool", this.getPackageName());
                sw.setOnCheckedChangeListener(null);
                sw.setChecked(sharedPref.getBoolean(pref_key, getResources().getBoolean(resourceId)));
                sw.setOnCheckedChangeListener(this);
            }
        }
    }

    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        SharedPreferences.Editor editor = sharedPref.edit();
        String pref_key = compoundButton.getTag().toString();
        editor.putBoolean(pref_key, b);
        editor.apply();

        if (pref_key.equals(getString(R.string.pref_turn_on_wifi_spot))) {
            h9PatchesService.checkWifiHotspot();
        }
        if (pref_key.equals(getString(R.string.pref_turn_off_radio))) {
            h9PatchesService.checkRadioPlay();
        }
        if (pref_key.equals(getString(R.string.pref_hide_navigation_bar))) {
            h9PatchesService.checkNavigationBar();
        }
        if (pref_key.equals(getString(R.string.pref_accessibility_dock))) {
            h9PatchesService.checkAccessibilityDock();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void init_launcher_spinner() {
        PackageManager pm = getPackageManager();
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        List<ResolveInfo> lst = pm.queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY);
//        launcher_packages.put("-", "");
        launcher_packages.clear();
        for (ResolveInfo resolveInfo : lst) {
            String name = resolveInfo.loadLabel(pm).toString().trim();
            if (!name.trim().isEmpty()) {
                launcher_packages.put(resolveInfo.loadLabel(pm).toString(), resolveInfo.activityInfo.packageName);
            }
        }
        Spinner spinner_launcher = findViewById(R.id.spinner_default_launcher);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new ArrayList<>(launcher_packages.keySet()));
        spinner_launcher.setAdapter(adapter);

        Intent current_intent = new Intent(Intent.ACTION_MAIN);
        current_intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo launcher_info = pm.resolveActivity(current_intent, PackageManager.MATCH_DEFAULT_ONLY);

        if (launcher_info != null) {
            String current_launcher = launcher_info.loadLabel(pm).toString();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.pref_default_launcher), current_launcher);
            editor.apply();
            int pos = adapter.getPosition(current_launcher);
            spinner_launcher.setOnItemSelectedListener(null);
            spinner_launcher.setSelection(pos == -1 ? 0 : pos, false);
        }
        spinner_launcher.setOnItemSelectedListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (adapterView.getId() == R.id.spinner_default_launcher) {
            String selected_launcher = adapterView.getSelectedItem().toString();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.pref_default_launcher), launcher_packages.get(selected_launcher));
            editor.apply();
            MainActivity.this.h9PatchesService.checkDefaultLauncher(false);
        } else if (adapterView.getId() == R.id.spinner_startup_app) {
            String selected_app = adapterView.getSelectedItem().toString();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.pref_startup_app), installed_packages.get(selected_app));
            editor.apply();
        }
    }


    private void init_startup_app_spinner() {
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> lst = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        installed_packages.clear();
        installed_packages.put("-", "");
        for (ApplicationInfo packageInfo : lst) {
            if ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
                installed_packages.put(packageInfo.loadLabel(pm).toString().trim(), packageInfo.packageName);
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new ArrayList<>(installed_packages.keySet()));
        Spinner spinner_startup_app = findViewById(R.id.spinner_startup_app);
        spinner_startup_app.setAdapter(adapter);
        String startup_app = sharedPref.getString(getString(R.string.pref_startup_app), "");
        for (Map.Entry<String, String> entry : installed_packages.entrySet()) {
            if (entry.getValue().equals(startup_app)) {
                spinner_startup_app.setOnItemSelectedListener(null);
                spinner_startup_app.setSelection(adapter.getPosition(entry.getKey().trim()), false);
                break;
            }
        }
        spinner_startup_app.setOnItemSelectedListener(this);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
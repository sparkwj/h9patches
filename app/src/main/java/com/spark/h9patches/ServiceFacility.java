package com.spark.h9patches;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.input.IInputManager;

public class ServiceFacility implements H9PatchesService.IFacility {
    String TAG = this.getClass().getName();;
    private Context mContext = null;
    protected SharedPreferences sharedPreferences;
    protected SharedPreferences.OnSharedPreferenceChangeListener prefListener;
    public ServiceFacility(Context context) {
        mContext = context;
        sharedPreferences = getApplication().getSharedPreferences();
        prefListener = (sharedPreferences, s) -> ServiceFacility.this._onSharedPreferenceChanged(s);
        sharedPreferences.registerOnSharedPreferenceChangeListener(prefListener);
    }

    protected Context getContext() {
        return mContext;
    }

    public void onServiceStart() {
    }

    public void onServiceStop() {
    }

    private void _onSharedPreferenceChanged(String s) {
        onSharedPreferenceChanged(s);
    }

    public void onSharedPreferenceChanged(String key) {
    }

    protected PackageManager getPackageManager() {
        return mContext.getPackageManager();
    }

    protected void startActivity(Intent intent) {
        mContext.startActivity(intent);
    }

    protected IInputManager getInputManager() {
        return getApplication().getInputManager();
    }

    protected String getString(int resId) {
        return mContext.getString(resId);
    }
    protected boolean getResourcesBoolean(int resId) {
        return mContext.getResources().getBoolean(resId);
    }

    protected Object getFacility(Class<?> cls) {
        return H9PatchesService.getInstance().getFacility(cls);
    }

    protected Context getApplicationContext() {
        return mContext.getApplicationContext();
    }

    protected H9PatchesApplication getApplication() {
        return H9PatchesApplication.getInstance();
    }
}

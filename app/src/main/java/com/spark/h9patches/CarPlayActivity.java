package com.spark.h9patches;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;

public class CarPlayActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            PackageManager packageManager = this.getPackageManager();
            Intent launchIntent = packageManager.getLaunchIntentForPackage("com.desaysv.vehicle.carplayapp");
            this.startActivity(launchIntent);
            launchIntent = packageManager.getLaunchIntentForPackage("com.desaysv.vehicle.carplayapp");
            this.startActivity(launchIntent);
        } catch (Exception e) {
        }
        finish();
    }
}

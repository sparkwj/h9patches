package com.spark.h9patches;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

public class Test extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startH9PatchesService();
    }

    private void startH9PatchesService() {
        try {
            Toast.makeText(this, "Hello world!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            //
        }
    }
}

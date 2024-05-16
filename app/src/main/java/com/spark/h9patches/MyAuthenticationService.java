package com.spark.h9patches;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MyAuthenticationService extends Service {

    MyAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
//        LogUtil.debug("");
        mAuthenticator = new MyAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}


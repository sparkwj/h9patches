package com.spark.h9patches;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class Accounts extends ServiceFacility implements OnAccountsUpdateListener {
    AccountManager mAccountManager;

    public Accounts(Context context) {
        super(context);
        mAccountManager = AccountManager.get(getApplicationContext());
        mAccountManager.addOnAccountsUpdatedListener(this, null, true);
        getAccounts();
    }

    private void getAccounts() {
        Account[] accounts = mAccountManager.getAccounts();
        if (accounts.length == 0) {
            Log.d(TAG, "accounts is empty");
        }
        for (Account account : accounts) {
            Log.d(TAG, "h9 get account: " + account.type + "/" + account.name + "/" + account.toString());
        }
    }

    @Override
    public void onServiceStart() {
        Log.d(TAG, "com.spark.h9: accounts");
        getAccounts();
    }

    @Override
    public void onAccountsUpdated(Account[] accounts) {

    }
}
